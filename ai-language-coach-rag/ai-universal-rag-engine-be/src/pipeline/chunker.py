from typing import Optional
from src.models import Chunk, ChunkMetadata
from src.config import get_chunk_size


class AdaptiveChunker:
    @staticmethod
    def chunk_text(
        text: str,
        metadata: ChunkMetadata,
        content_type: Optional[str] = None
    ) -> list[Chunk]:
        chunk_size = get_chunk_size(content_type or "document")
        chunks = []
        paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
        current_chunk = []
        current_size = 0
        base_metadata = metadata.model_copy()

        for para in paragraphs:
            para_size = len(para.split())
            if current_size + para_size > chunk_size and current_chunk:
                chunk_text = "\n\n".join(current_chunk).strip()
                chunks.append(Chunk(text=chunk_text, metadata=base_metadata))
                current_chunk = [para]
                current_size = para_size
            else:
                current_chunk.append(para)
                current_size += para_size

        if current_chunk:
            chunk_text = "\n\n".join(current_chunk).strip()
            chunks.append(Chunk(text=chunk_text, metadata=base_metadata))

        return chunks

    @staticmethod
    def chunk_by_slides(
        slides: list[str],
        metadata: ChunkMetadata
    ) -> list[Chunk]:
        chunks = []
        for i, slide_text in enumerate(slides):
            if slide_text.strip():
                chunks.append(Chunk(
                    text=slide_text,
                    metadata=metadata.model_copy(update={"slide_num": i + 1})
                ))
        return chunks

    @staticmethod
    def chunk_by_pages(
        pages: list[str],
        metadata: ChunkMetadata
    ) -> list[Chunk]:
        chunks = []
        for i, page_text in enumerate(pages):
            if page_text.strip():
                chunks.append(Chunk(
                    text=page_text,
                    metadata=metadata.model_copy(update={"page_num": i + 1})
                ))
        return chunks