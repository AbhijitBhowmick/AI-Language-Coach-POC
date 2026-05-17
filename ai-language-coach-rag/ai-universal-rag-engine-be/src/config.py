from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Final


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    APP_HOST: str = "0.0.0.0"
    APP_PORT: int = 8000
    LOG_LEVEL: str = "INFO"

    QDRANT_HOST: str = "192.168.0.18"
    QDRANT_PORT: int = 6333
    QDRANT_GRPC_PORT: int = 6334
    QDRANT_COLLECTION: str = "knowledge_vault"
    QDRANT_VECTOR_SIZE: int = 1024
    QDRANT_HNSW_M: int = 16
    QDRANT_HNSW_EF_CONSTRUCT: int = 200
    QDRANT_HNSW_EF: int = 128
    QDRANT_ON_DISK_PAYLOAD: bool = True

    VALKEY_HOST: str = "192.168.0.18"
    VALKEY_PORT: int = 6379
    VALKEY_DB: int = 0
    CACHE_TTL: int = 3600

    GEMINI_API_KEY: str = ""
    GEMINI_EMBEDDING_MODEL: str = "text-embedding-004"
    GEMINI_GENERATION_MODEL: str = "gemini-1.5-flash-002"
    GEMINI_PRO_MODEL: str = "gemini-1.5-pro-002"

    JWT_SECRET: str = ""
    INTERNAL_API_KEY: str = ""

    CHUNK_SIZES: dict[str, int] = {
        "document": 512,
        "web": 256,
        "social": 200,
        "audio": 400,
    }
    CHUNK_OVERLAP: int = 50

    MAX_UPLOAD_DOC_SIZE: int = 25 * 1024 * 1024
    MAX_UPLOAD_AUDIO_SIZE: int = 100 * 1024 * 1024

    USE_MOCK: bool = False
    MOCK_EMBEDDING_DIM: int = 768

    EMBEDDER_TYPE: str = "gemini"
    LOCAL_EMBEDDER_MODEL: str = "mxbai-embed-large-v1"

    WORKER_COUNT: int = 4
    WORKER_CONCURRENCY: int = 100


settings = Settings()

LEVEL_HIERARCHY: Final[list[str]] = ["a1", "a2", "b1", "b2", "c1", "c2"]


def get_chunk_size(content_type: str) -> int:
    return settings.CHUNK_SIZES.get(content_type, settings.CHUNK_SIZES["document"])


def get_parent_level(level: str) -> str | None:
    idx = LEVEL_HIERARCHY.index(level) if level in LEVEL_HIERARCHY else None
    return LEVEL_HIERARCHY[idx - 1] if idx and idx > 0 else None


def get_vector_size() -> int:
    if settings.EMBEDDER_TYPE == "local" and not settings.USE_MOCK:
        return 1024
    return settings.QDRANT_VECTOR_SIZE