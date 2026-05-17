import json
import logging
import time
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from src.models import QueryRequest, QueryResponse, RetrievedChunk, FilterMode, ChunkMetadata
from src.filters import build_query_filter, filter_to_dict
from src.embedding import get_embedder
from src.generation import get_generator
from src.cache import get_cache
from src.qdrant_wrapper import get_qdrant
from src.config import get_parent_level, settings
import src.metrics as metrics

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ai/v1", tags=["query"])


@router.post("/query", response_model=QueryResponse)
async def query_documents(request: QueryRequest):
    start_time = time.time()
    embedder = get_embedder()
    cache = get_cache()
    qdrant = get_qdrant()

    metrics.REQUEST_COUNT.labels(endpoint="query", status="started").inc()

    filter_dict = {
        "domain": request.domain,
        "target_lang": request.target_lang,
        "level": request.level,
    }
    if request.content_type:
        filter_dict["content_type"] = request.content_type

    cached_response = await cache.get(request.query, filter_dict)
    if cached_response:
        try:
            data = json.loads(cached_response)
            metrics.CACHE_HIT.inc()
            return QueryResponse(**data, cached=True)
        except Exception:
            pass

    metrics.CACHE_MISS.inc()

    query_vector = await embedder.embed(request.query)

    filter_obj = build_query_filter(
        domain=request.domain,
        target_lang=request.target_lang,
        level=request.level,
        content_type=request.content_type,
        filter_mode=request.filter_mode,
    )

    results = await qdrant.search(
        query_vector=query_vector,
        filter=filter_obj,
        limit=request.limit
    )

    if not results and request.widen_fallback and request.filter_mode == FilterMode.STRICT:
        parent_level = get_parent_level(request.level)
        if parent_level:
            logger.info(f"Falling back to parent level: {parent_level}")
            fallback_filter = build_query_filter(
                domain=request.domain,
                target_lang=request.target_lang,
                level=parent_level,
                content_type=request.content_type,
                filter_mode=FilterMode.PARENT,
            )
            results = await qdrant.search(
                query_vector=query_vector,
                filter=fallback_filter,
                limit=request.limit
            )

    if not results:
        return QueryResponse(
            answer="I couldn't find any relevant information for your query. Try adjusting your filters or level.",
            citations=[],
            metadata={"filter": filter_to_dict(filter_obj)},
            cached=False
        )

    retrieved_chunks = [
        RetrievedChunk(
            id=r["id"],
            text=r["text"],
            metadata=ChunkMetadata(
                domain=r["domain"],
                target_lang=r["target_lang"],
                level=r["level"],
                content_type=r["content_type"],
                tenant_id=r.get("tenant_id"),
                source_url=r.get("source_url"),
                page_num=r.get("page_num"),
                slide_num=r.get("slide_num"),
                timestamp_start=r.get("timestamp_start"),
                timestamp_end=r.get("timestamp_end"),
            ),
            score=r["score"],
            source_ref=f"{r.get('content_type', 'unknown')}:{r.get('page_num', r.get('slide_num', 'n/a'))}"
        )
        for r in results
    ]

    generator = get_generator()
    answer = await generator.generate(
        query=request.query,
        context=retrieved_chunks,
        native_lang=request.native_lang,
        persona=request.persona
    )

    response = QueryResponse(
        answer=answer,
        citations=retrieved_chunks,
        metadata={
            "filter": filter_to_dict(filter_obj),
            "total_results": len(results),
            "filter_mode": request.filter_mode.value if request.filter_mode else "strict",
        },
        cached=False
    )

    try:
        await cache.set(request.query, filter_dict, response.model_dump_json())
    except Exception as e:
        logger.warning(f"Failed to cache response: {e}")

    latency = time.time() - start_time
    metrics.REQUEST_LATENCY.labels(endpoint="query").observe(latency)
    metrics.REQUEST_COUNT.labels(endpoint="query", status="success").inc()

    return response


async def generate_stream(
    request: QueryRequest,
    embedder,
    cache,
    qdrant
):
    filter_dict = {
        "domain": request.domain,
        "target_lang": request.target_lang,
        "level": request.level,
    }
    if request.content_type:
        filter_dict["content_type"] = request.content_type

    cached = await cache.get(request.query, filter_dict)
    if cached:
        yield f"data: {json.dumps({'chunk': cached, 'cached': True})}\n\n"
        return

    query_vector = await embedder.embed(request.query)

    filter_obj = build_query_filter(
        domain=request.domain,
        target_lang=request.target_lang,
        level=request.level,
        content_type=request.content_type,
        filter_mode=request.filter_mode,
    )

    results = await qdrant.search(
        query_vector=query_vector,
        filter=filter_obj,
        limit=request.limit
    )

    if not results:
        yield f"data: {json.dumps({'chunk': 'No relevant information found.', 'done': True})}\n\n"
        return

    retrieved_chunks = [
        RetrievedChunk(
            id=r["id"],
            text=r["text"],
            metadata=ChunkMetadata(
                domain=r["domain"],
                target_lang=r["target_lang"],
                level=r["level"],
                content_type=r["content_type"],
                tenant_id=r.get("tenant_id"),
                source_url=r.get("source_url"),
                page_num=r.get("page_num"),
                slide_num=r.get("slide_num"),
                timestamp_start=r.get("timestamp_start"),
                timestamp_end=r.get("timestamp_end"),
            ),
            score=r["score"],
            source_ref=f"{r.get('content_type', 'unknown')}:{r.get('page_num', r.get('slide_num', 'n/a'))}"
        )
        for r in results
    ]

    generator = get_generator()

    async for chunk in generator.generate_stream(
        query=request.query,
        context=retrieved_chunks,
        native_lang=request.native_lang,
        persona=request.persona
    ):
        yield f"data: {json.dumps({'chunk': chunk})}\n\n"

    yield f"data: {json.dumps({'done': True})}\n\n"


@router.post("/query/stream")
async def query_stream(request: QueryRequest):
    embedder = get_embedder()
    cache = get_cache()
    qdrant = get_qdrant()

    metrics.REQUEST_COUNT.labels(endpoint="query_stream", status="started").inc()

    return StreamingResponse(
        generate_stream(request, embedder, cache, qdrant),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache"}
    )