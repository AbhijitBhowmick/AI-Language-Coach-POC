from typing import Optional, Any
# Remove CrawlerProvider from imports as it's no longer used this way in v0.4.x+
from crawl4ai import CrawlerRunConfig, AsyncWebCrawler
from src.handlers.base import BaseHandler
from src.models import Chunk, ChunkMetadata
from src.config import get_chunk_size, settings

class WebHandler(BaseHandler):
    # Change 'CrawlerProvider' to 'Any' or remove the hint to resolve the error
    def __init__(self, provider: Optional[Any] = None):
        self.provider = provider

    async def process(
        self,
        source: str,
        metadata: ChunkMetadata,
        filename: str | None = None
    ) -> list[Chunk]:
        if not source:
            raise ValueError("URL is required for web handler")

        markdown_content = await self._fetch_url(source)
        chunks = self._chunk_web_content(markdown_content, metadata)
        for chunk in chunks:
            chunk.metadata.source_url = source

        return chunks

    async def _fetch_url(self, url: str) -> str:
        config = CrawlerRunConfig(
            # Using the v0.4.x+ configuration parameters
            word_count_threshold=10,
            excluded_tags=['nav', 'footer', 'header'],
            # wait_for is now handled differently in RunConfig
        )

        # In modern crawl4ai, you typically don't pass a provider into the crawler init
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(url=url, config=config)

        if not result.success:
            raise RuntimeError(f"Failed to fetch URL: {url}")

        return result.markdown or result.extracted_content or ""

    def _chunk_web_content(self, text: str, metadata: ChunkMetadata) -> list[Chunk]:
        chunk_size = get_chunk_size("web")
        chunks = []
        paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
        current_chunk = []
        current_size = 0

        for para in paragraphs:
            para_size = len(para.split())
            if current_size + para_size > chunk_size and current_chunk:
                chunk_text = "\n\n".join(current_chunk).strip()
                chunks.append(Chunk(text=chunk_text, metadata=metadata.model_copy()))
                current_chunk = [para]
                current_size = para_size
            else:
                current_chunk.append(para)
                current_size += para_size

        if current_chunk:
            chunk_text = "\n\n".join(current_chunk).strip()
            chunks.append(Chunk(text=chunk_text, metadata=metadata.model_copy()))

        return chunks

async def create_web_handler() -> WebHandler:
    return WebHandler()