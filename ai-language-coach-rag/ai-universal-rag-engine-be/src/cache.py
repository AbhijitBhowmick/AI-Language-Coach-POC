import json
import hashlib
from typing import Optional
import valkey
from src.config import settings


class SemanticCache:
    def __init__(self):
        self._client: Optional[valkey.Valkey] = None
        self.ttl = settings.CACHE_TTL

    @property
    def client(self) -> valkey.Valkey:
        if self._client is None:
            self._client = valkey.Valkey(
                host=settings.VALKEY_HOST,
                port=settings.VALKEY_PORT,
                db=settings.VALKEY_DB,
                decode_responses=False
            )
        return self._client

    def _make_key(self, query: str, filter_dict: dict) -> str:
        normalized_filter = json.dumps(filter_dict, sort_keys=True)
        hash_input = f"{query}:{normalized_filter}"
        hash_digest = hashlib.sha256(hash_input.encode()).hexdigest()[:16]
        return f"rag:cache:{hash_digest}"

    async def get(self, query: str, filters: dict) -> Optional[str]:
        try:
            key = self._make_key(query, filters)
            result = self.client.get(key)
            if result:
                return result.decode("utf-8") if isinstance(result, bytes) else result
        except Exception:
            pass
        return None

    async def set(self, query: str, filters: dict, response: str) -> None:
        try:
            key = self._make_key(query, filters)
            self.client.setex(key, self.ttl, response)
        except Exception:
            pass

    async def invalidate(self, pattern: str = "rag:cache:*") -> int:
        try:
            keys = self.client.keys(pattern)
            if keys:
                return self.client.delete(*keys)
        except Exception:
            pass
        return 0

    def close(self) -> None:
        if self._client:
            self._client.close()


_cache_instance: Optional[SemanticCache] = None


def get_cache() -> SemanticCache:
    global _cache_instance
    if _cache_instance is None:
        _cache_instance = SemanticCache()
    return _cache_instance