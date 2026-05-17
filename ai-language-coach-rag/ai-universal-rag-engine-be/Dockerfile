FROM python:3.12-slim

LABEL maintainer="Platform Team <platform@example.com>"
LABEL description="Universal Hierarchical RAG Microservice - Optimized for 1M Users"

RUN apt-get update && apt-get install -y \
    curl \
    libsndfile1 \
    ffmpeg \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir uv==0.5.11

WORKDIR /app

COPY pyproject.toml ./
RUN uv sync --frozen --no-dev --system

COPY src/ ./src/

ENV PYTHONPATH=/app
ENV PYTHONUNBUFFERED=1
ENV WORKER_COUNT=4
ENV WORKER_CONCURRENCY=100

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "src.main:app", \
     "--host", "0.0.0.0", \
     "--port", "8000", \
     "--workers", "${WORKER_COUNT:-4}", \
     "--limit-concurrency", "${WORKER_CONCURRENCY:-100}", \
     "--limit-max-requests", "1000"]
