from pydantic import BaseModel, Field
from typing import Optional, Any
from enum import Enum
import uuid


class SourceType(str, Enum):
    FILE = "file"
    URL = "url"
    AUDIO_URL = "audio_url"


class ChunkMetadata(BaseModel):
    domain: str = Field(..., description="Broad category (e.g., language-coach)")
    target_lang: str = Field(..., description="Target language (e.g., czech, german)")
    level: str = Field(..., description="Educational level (e.g., a1, a2, b1)")
    content_type: str = Field(..., description="Category (e.g., grammar, vocabulary, dialogue)")
    tenant_id: Optional[str] = Field(None, description="Tenant identifier for isolation")
    source_url: Optional[str] = Field(None, description="Source URL if applicable")
    page_num: Optional[int] = Field(None, description="Page number for PDF")
    slide_num: Optional[int] = Field(None, description="Slide number for PPTX")
    timestamp_start: Optional[float] = Field(None, description="Audio timestamp start (seconds)")
    timestamp_end: Optional[float] = Field(None, description="Audio timestamp end (seconds)")


class Chunk(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    text: str
    metadata: ChunkMetadata
    embedding: Optional[list[float]] = None


class IngestSource(BaseModel):
    source_type: SourceType
    file: Optional[bytes] = None
    url: Optional[str] = None
    audio_url: Optional[str] = None
    filename: Optional[str] = None


class IngestRequest(BaseModel):
    source_type: SourceType
    metadata: ChunkMetadata
    url: Optional[str] = None
    audio_url: Optional[str] = None


class IngestResponse(BaseModel):
    status: str
    chunks_created: int
    document_id: str


class FilterMode(str, Enum):
    STRICT = "strict"
    PARENT = "parent"
    OPEN = "open"


class QueryRequest(BaseModel):
    query: str
    native_lang: str = "en"
    domain: str = "language-coach"
    target_lang: str = "czech"
    level: str = "a1"
    content_type: Optional[str] = None
    persona: Optional[str] = None
    widen_fallback: bool = True
    filter_mode: FilterMode = FilterMode.STRICT
    limit: int = 3


class RetrievedChunk(BaseModel):
    id: str
    text: str
    metadata: ChunkMetadata
    score: float
    source_ref: str


class QueryResponse(BaseModel):
    answer: str
    citations: list[RetrievedChunk]
    metadata: dict[str, Any]
    cached: bool = False


class HealthResponse(BaseModel):
    status: str
    qdrant: str
    valkey: str
    version: str = "1.0.0"


class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None