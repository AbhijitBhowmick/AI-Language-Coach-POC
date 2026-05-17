import io
from typing import Optional
from docling.document_converter import DocumentConverter
from docling.datamodel.base_models import ConversionStatus
from docling.datamodel.pipeline_options import PdfPipelineOptions, EasyOcrOptions
# Note: InputMode is often no longer required for basic conversion in v2
from src.handlers.base import BaseHandler
from src.models import Chunk, ChunkMetadata
from src.config import get_chunk_size


class DocHandler(BaseHandler):
    def __init__(self):
        self.converter = DocumentConverter()
        self.pipeline_options = PdfPipelineOptions(
            do_ocr=True,
            ocr_options=EasyOcrOptions()
        )

    async def process(
        self,
        source: str,
        metadata: ChunkMetadata,
        filename: str | None = None
    ) -> list[Chunk]:
        result = await self._convert_document(source, filename)
        return self._chunk_document(result, metadata)

    async def _convert_document(
        self,
        source: bytes,
        filename: str | None
    ) -> str:
        result = self.converter.convert(
            source={"bytes": source, "loc": filename or "document.pdf"},
            input_mode=InputMode.PDF if not filename or filename.endswith(".pdf") else InputMode.DOCX
        )
        return result.document.export_to_markdown()

    def _chunk_document(self, text: str, metadata: ChunkMetadata) -> list[Chunk]:
        chunk_size = get_chunk_size("document")
        chunks = []
        lines = text.split("\n")
        current_chunk = []
        current_size = 0
        page_num = metadata.page_num or 1

        for line in lines:
            line_size = len(line.split())
            if current_size + line_size > chunk_size and current_chunk:
                chunk_text = "\n".join(current_chunk)
                chunks.append(Chunk(
                    text=chunk_text,
                    metadata=metadata.model_copy(update={"page_num": page_num})
                ))
                current_chunk = [line]
                current_size = line_size
            else:
                current_chunk.append(line)
                current_size += line_size

            if "---" in line:
                page_num += 1

        if current_chunk:
            chunk_text = "\n".join(current_chunk)
            chunks.append(Chunk(
                text=chunk_text,
                metadata=metadata.model_copy(update={"page_num": page_num})
            ))

        return chunks


async def create_doc_handler() -> DocHandler:
    return DocHandler()