import uuid
import logging
from src.models import Chunk, IngestResponse, IngestRequest, SourceType
from src.handlers.factory import get_handler
from src.embedding import get_embedder, BaseEmbedder
from src.qdrant_wrapper import get_qdrant, QdrantWrapper
from src.config import settings

logger = logging.getLogger(__name__)


class IngestPipeline:
    def __init__(self):
        self.embedder: BaseEmbedder = get_embedder()
        self.qdrant: QdrantWrapper = get_qdrant()

    async def run(
        self,
        request: IngestRequest,
        file_content: bytes | None = None,
    ) -> IngestResponse:
        document_id = str(uuid.uuid4())
        source = file_content or request.url or request.audio_url or ""
        source_type = request.source_type

        if source_type == SourceType.FILE and file_content is None:
            raise ValueError("File content is required for file source type")

        if source_type == SourceType.URL and not request.url:
            raise ValueError("URL is required for URL source type")

        if source_type == SourceType.AUDIO_URL and not request.audio_url:
            raise ValueError("Audio URL is required for audio source type")

        handler = await get_handler(source_type)

        metadata = request.metadata.model_copy()
        metadata.tenant_id = str(metadata.tenant_id or settings.APP_PORT)

        source_str = str(source) if not isinstance(source, bytes) else ""
        if request.url:
            source_str = request.url
        elif request.audio_url:
            source_str = request.audio_url

        chunks = await handler.process(source_str, metadata)

        if not chunks:
            raise ValueError("No chunks generated from source")

        text_contents = [chunk.text for chunk in chunks]
        embeddings = await self.embedder.embed_batch(text_contents)

        for chunk, embedding in zip(chunks, embeddings):
            chunk.embedding = embedding

        await self.qdrant.upsert_chunks(chunks)

        logger.info(
            f"Ingested {len(chunks)} chunks for document {document_id}, "
            f"domain={metadata.domain}, target_lang={metadata.target_lang}, "
            f"level={metadata.level}"
        )

        return IngestResponse(
            status="ingested",
            chunks_created=len(chunks),
            document_id=document_id
        )


_pipeline_instance: IngestPipeline | None = None


def get_pipeline() -> IngestPipeline:
    global _pipeline_instance
    if _pipeline_instance is None:
        _pipeline_instance = IngestPipeline()
    return _pipeline_instance