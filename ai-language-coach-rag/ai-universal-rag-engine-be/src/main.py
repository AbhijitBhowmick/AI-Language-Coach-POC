import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import Response
from src.routers import ingest, query
from src.config import settings
from src.qdrant_wrapper import get_qdrant
from src.cache import get_cache
import src.metrics as metrics

logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting ai-universal-rag-engine-be...")

    qdrant = get_qdrant()
    try:
        await qdrant.ensure_collection()
        logger.info("Qdrant collection ensured")
    except Exception as e:
        logger.error(f"Failed to initialize Qdrant: {e}")

    cache = get_cache()
    logger.info("Valkey cache initialized")

    yield

    logger.info("Shutting down ai-universal-rag-engine-be...")


app = FastAPI(
    title="ai-universal-rag-engine-be",
    description="Universal Hierarchical RAG Microservice for Multi-Modal Knowledge Retrieval",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ingest.router)
app.include_router(query.router)


@app.get("/metrics", tags=["monitoring"])
async def prometheus_metrics():
    return Response(
        content=metrics.get_metrics(),
        media_type=metrics.get_content_type_metrics()
    )


@app.get("/health", tags=["health"])
async def health_check():
    qdrant_status = "unknown"
    valkey_status = "unknown"

    try:
        qdrant = get_qdrant()
        collections = qdrant.client.get_collections()
        qdrant_status = "healthy" if collections else "unhealthy"
    except Exception as e:
        qdrant_status = f"error: {str(e)}"

    try:
        cache = get_cache()
        cache.client.ping()
        valkey_status = "healthy"
    except Exception as e:
        valkey_status = f"error: {str(e)}"

    return {
        "status": "ok" if qdrant_status == "healthy" and valkey_status == "healthy" else "degraded",
        "qdrant": qdrant_status,
        "valkey": valkey_status,
        "version": "1.0.0"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "src.main:app",
        host=settings.APP_HOST,
        port=settings.APP_PORT,
        reload=settings.LOG_LEVEL == "DEBUG"
    )