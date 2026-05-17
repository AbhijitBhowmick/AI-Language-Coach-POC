from prometheus_client import Counter, Histogram, Gauge, generate_latest, CONTENT_TYPE_LATEST

REQUEST_COUNT = Counter(
    'rag_requests_total',
    'Total RAG requests',
    ['endpoint', 'status']
)

REQUEST_LATENCY = Histogram(
    'rag_request_latency_seconds',
    'Request latency in seconds',
    ['endpoint']
)

CACHE_HIT = Counter(
    'rag_cache_hits_total',
    'Total cache hits'
)

CACHE_MISS = Counter(
    'rag_cache_misses_total',
    'Total cache misses'
)

CACHE_HIT_RATIO = Gauge(
    'rag_cache_hit_ratio',
    'Cache hit ratio (0-1)'
)

LLM_TOKENS_PROMPT = Counter(
    'rag_llm_prompt_tokens_total',
    'Total LLM prompt tokens used'
)

LLM_TOKENS_COMPLETION = Counter(
    'rag_llm_completion_tokens_total',
    'Total LLM completion tokens used'
)

EMBEDDING_COUNT = Counter(
    'rag_embedding_requests_total',
    'Total embedding requests',
    ['embedder_type', 'status']
)

EMBEDDING_LATENCY = Histogram(
    'rag_embedding_latency_seconds',
    'Embedding latency in seconds',
    ['embedder_type']
)

GENERATION_LATENCY = Histogram(
    'rag_generation_latency_seconds',
    'Generation latency in seconds'
)

VECTOR_SEARCH_LATENCY = Histogram(
    'rag_vector_search_latency_seconds',
    'Vector search latency in seconds'
)

CHUNKS_INDEXED = Counter(
    'rag_chunks_indexed_total',
    'Total chunks indexed',
    ['content_type']
)

ACTIVE_WORKERS = Gauge(
    'rag_active_workers',
    'Number of active workers'
)

QDRANT_COLLECTION_SIZE = Gauge(
    'rag_qdrant_collection_size',
    'Number of vectors in Qdrant collection'
)


def get_metrics():
    return generate_latest()


def get_content_type_metrics():
    return CONTENT_TYPE_LATEST
