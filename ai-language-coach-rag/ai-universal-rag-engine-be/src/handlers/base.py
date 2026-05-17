from abc import ABC, abstractmethod
from typing import Protocol
from src.models import Chunk, ChunkMetadata


class Handler(Protocol):
    async def process(self, source: str, metadata: ChunkMetadata, filename: str | None = None) -> list[Chunk]: ...


class BaseHandler(ABC):
    @abstractmethod
    async def process(
        self,
        source: str,
        metadata: ChunkMetadata,
        filename: str | None = None
    ) -> list[Chunk]:
        pass