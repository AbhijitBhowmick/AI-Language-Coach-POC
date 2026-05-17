from abc import ABC, abstractmethod
from typing import Protocol
import numpy as np
import hashlib
from src.config import settings, get_vector_size


class Embedder(Protocol):
    async def embed(self, text: str) -> list[float]: ...


class BaseEmbedder(ABC):
    @abstractmethod
    async def embed(self, text: str) -> list[float]: ...


class GeminiEmbedder(BaseEmbedder):
    def __init__(self, model_name: str = "text-embedding-004"):
        self.model_name = model_name
        self._client = None

    @property
    def client(self):
        if self._client is None:
            import google.generativeai as genai
            genai.configure(api_key=settings.GEMINI_API_KEY)
            self._client = genai
        return self._client

    async def embed(self, text: str) -> list[float]:
        result = self.client.embed_content(
            model=self.model_name,
            content=text,
            task_type="retrieval_query"
        )
        return list(result.embedding)

    async def embed_batch(self, texts: list[str]) -> list[list[float]]:
        results = []
        for text in texts:
            emb = await self.embed(text)
            results.append(emb)
        return results


class MockEmbedder(BaseEmbedder):
    def __init__(self, dim: int = 1024):
        self.dim = dim

    async def embed(self, text: str) -> list[float]:
        hash_bytes = hashlib.sha256(text.encode()).digest()
        seed = int.from_bytes(hash_bytes[:4], "big")
        rng = np.random.RandomState(seed)
        vector = rng.randn(self.dim).astype(np.float32)
        norm = np.linalg.norm(vector)
        if norm > 0:
            vector = vector / norm
        return vector.tolist()

    async def embed_batch(self, texts: list[str]) -> list[list[float]]:
        return [await self.embed(text) for text in texts]


class LocalEmbedder(BaseEmbedder):
    def __init__(self, model_name: str = "mxbai-embed-large-v1"):
        self.model_name = model_name
        self._model = None

    async def _load_model(self):
        if self._model is None:
            from sentence_transformers import SentenceTransformer
            self._model = SentenceTransformer(self.model_name, device='cpu')

    async def embed(self, text: str) -> list[float]:
        await self._load_model()
        embedding = self._model.encode(text, normalize_embeddings=True)
        return embedding.tolist()

    async def embed_batch(self, texts: list[str]) -> list[list[float]]:
        await self._load_model()
        embeddings = self._model.encode(texts, normalize_embeddings=True, show_progress_bar=False)
        return embeddings.tolist()


def get_embedder() -> BaseEmbedder:
    if settings.USE_MOCK:
        dim = 1024 if settings.EMBEDDER_TYPE == "local" else settings.MOCK_EMBEDDING_DIM
        return MockEmbedder(dim=dim)

    if settings.EMBEDDER_TYPE == "local":
        return LocalEmbedder(model_name=settings.LOCAL_EMBEDDER_MODEL)

    return GeminiEmbedder(model_name=settings.GEMINI_EMBEDDING_MODEL)