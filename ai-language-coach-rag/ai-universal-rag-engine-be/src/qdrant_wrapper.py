from typing import Optional
from qdrant_client import QdrantClient
from qdrant_client.http import models
from qdrant_client.models import Distance
import uuid
from src.config import settings, get_vector_size
from src.models import Chunk


class QdrantWrapper:
    def __init__(self):
        self._client: Optional[QdrantClient] = None

    @property
    def client(self) -> QdrantClient:
        if self._client is None:
            self._client = QdrantClient(
                host=settings.QDRANT_HOST,
                port=settings.QDRANT_PORT
            )
        return self._client

    '''async def ensure_collection(self) -> None:
        collections = self.client.get_collections().collections
        collection_names = [c.name for c in collections]

        if settings.QDRANT_COLLECTION not in collection_names:
            vector_size = get_vector_size()
            self.client.create_collection(
                collection_name=settings.QDRANT_COLLECTION,
                vectors_config=models.VectorParams(
                    size=vector_size,
                    distance=Distance.COSINE,
                    hnsw_config=models.HnswConfig(
                        m=settings.QDRANT_HNSW_M,
                        ef_construct=settings.QDRANT_HNSW_EF_CONSTRUCT,
                        full_scan_threshold=10000
                    ),
                    quantization_config=models.QuantizationConfig(
                        type=models.QuantizationType.SCALAR,
                        quantile=0.5,
                        always_ram=settings.QDRANT_ON_DISK_PAYLOAD is False
                    ),
                    on_disk=settings.QDRANT_ON_DISK_PAYLOAD
                )
            )'''

    async def ensure_collection(self) -> None:
        collections = self.client.get_collections().collections
        collection_names = [c.name for c in collections]

        if settings.QDRANT_COLLECTION not in collection_names:
            vector_size = get_vector_size()
            self.client.create_collection(
                collection_name=settings.QDRANT_COLLECTION,
                vectors_config=models.VectorParams(
                    size=vector_size,
                    distance=models.Distance.COSINE,
                    on_disk=settings.QDRANT_ON_DISK_PAYLOAD
                ),
                # CRITICAL: Use .model_dump() to avoid the Pydantic error
                hnsw_config=models.HnswConfig(
                    m=settings.QDRANT_HNSW_M,
                    ef_construct=settings.QDRANT_HNSW_EF_CONSTRUCT,
                    full_scan_threshold=10000
                ).model_dump(),
                quantization_config=models.ScalarQuantization(
                    scalar=models.ScalarQuantizationConfig(
                        type=models.ScalarType.INT8,
                        quantile=0.5,
                        always_ram=not settings.QDRANT_ON_DISK_PAYLOAD
                    )
                ).model_dump()
            )
    async def upsert_chunks(self, chunks: list[Chunk]) -> None:
        points = []
        for chunk in chunks:
            if chunk.embedding is None:
                continue
            points.append(
                models.PointStruct(
                    id=chunk.id,
                    vector=chunk.embedding,
                    payload={
                        "text": chunk.text,
                        "domain": chunk.metadata.domain,
                        "target_lang": chunk.metadata.target_lang,
                        "level": chunk.metadata.level,
                        "content_type": chunk.metadata.content_type,
                        "tenant_id": chunk.metadata.tenant_id,
                        "source_url": chunk.metadata.source_url,
                        "page_num": chunk.metadata.page_num,
                        "slide_num": chunk.metadata.slide_num,
                        "timestamp_start": chunk.metadata.timestamp_start,
                        "timestamp_end": chunk.metadata.timestamp_end,
                    }
                )
            )

        if points:
            self.client.upsert(
                collection_name=settings.QDRANT_COLLECTION,
                points=points
            )

    '''async def search(
        self,
        query_vector: list[float],
        filter: Optional[models.Filter] = None,
        limit: int = 10
    ) -> list[dict]:
        results = self.client.search(
            collection_name=settings.QDRANT_COLLECTION,
            query_vector=query_vector,
            query_filter=filter,
            limit=limit,
            with_payload=True,
            hnsw_ef=settings.QDRANT_HNSW_EF
        )

        return [
            {
                "id": r.id,
                "score": r.score,
                "text": r.payload.get("text"),
                "domain": r.payload.get("domain"),
                "target_lang": r.payload.get("target_lang"),
                "level": r.payload.get("level"),
                "content_type": r.payload.get("content_type"),
                "tenant_id": r.payload.get("tenant_id"),
                "source_url": r.payload.get("source_url"),
                "page_num": r.payload.get("page_num"),
                "slide_num": r.payload.get("slide_num"),
                "timestamp_start": r.payload.get("timestamp_start"),
                "timestamp_end": r.payload.get("timestamp_end"),
            }
            for r in results
        ]'''

    async def search(
            self,
            query_vector: list[float],
            filter: models.Filter | None = None,
            limit: int = 10
    ) -> list[dict]:
        # Use the official client search method
        results = self.client.search(
            collection_name=settings.QDRANT_COLLECTION,
            query_vector=query_vector,
            query_filter=filter,
            limit=limit,
            with_payload=True,
            # Ensure hnsw_ef is passed correctly if needed
            search_params=models.SearchParams(hnsw_ef=settings.QDRANT_HNSW_EF)
        )

        # Format results for the QueryResponse model
        return [{
            "id": r.id,
            "score": r.score,
            "text": r.payload.get("text"),
            "domain": r.payload.get("domain"),
            "target_lang": r.payload.get("target_lang"),
            "level": r.payload.get("level"),
            "content_type": r.payload.get("content_type"),
            "tenant_id": r.payload.get("tenant_id"),
            "source_url": r.payload.get("source_url"),
            "page_num": r.payload.get("page_num"),
            "slide_num": r.payload.get("slide_num"),
            "timestamp_start": r.payload.get("timestamp_start"),
            "timestamp_end": r.payload.get("timestamp_end"),
        }
            for r in results
        ]

    async def delete_collection(self) -> None:
        try:
            self.client.delete_collection(collection_name=settings.QDRANT_COLLECTION)
        except Exception:
            pass


_qdrant_instance: Optional[QdrantWrapper] = None


def get_qdrant() -> QdrantWrapper:
    global _qdrant_instance
    if _qdrant_instance is None:
        _qdrant_instance = QdrantWrapper()
    return _qdrant_instance