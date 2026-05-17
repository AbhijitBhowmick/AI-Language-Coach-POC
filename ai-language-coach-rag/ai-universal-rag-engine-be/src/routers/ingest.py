import logging
from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from fastapi.responses import JSONResponse
from src.models import IngestResponse, IngestRequest, SourceType
from src.pipeline.ingest_pipeline import get_pipeline

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ai/v1", tags=["ingest"])


@router.post("/ingest", response_model=IngestResponse)
async def ingest_document(
    source_type: str = Form(...),
    domain: str = Form(...),
    target_lang: str = Form(...),
    level: str = Form(...),
    content_type: str = Form(...),
    tenant_id: str | None = Form(None),
    source_url: str | None = Form(None),
    audio_url: str | None = Form(None),
    file: UploadFile | None = File(None),
):
    try:
        st = SourceType(source_type)
    except ValueError:
        raise HTTPException(status_code=400, detail=f"Invalid source_type: {source_type}")

    file_content = None
    filename = None
    if file:
        if file.size and file.size > 25 * 1024 * 1024:
            raise HTTPException(status_code=413, detail="File too large. Max 25MB for documents.")

        contents = await file.read()
        if len(contents) > 25 * 1024 * 1024:
            raise HTTPException(status_code=413, detail="File too large. Max 25MB for documents.")

        file_content = contents
        filename = file.filename

    request = IngestRequest(
        source_type=st,
        metadata={
            "domain": domain,
            "target_lang": target_lang,
            "level": level,
            "content_type": content_type,
            "tenant_id": tenant_id,
            "source_url": source_url,
        },
        url=source_url,
        audio_url=audio_url,
    )

    pipeline = get_pipeline()
    result = await pipeline.run(request, file_content)

    return result


@router.post("/ingest/audio", response_model=IngestResponse)
async def ingest_audio(
    audio_url: str = Form(...),
    domain: str = Form(...),
    target_lang: str = Form(...),
    level: str = Form(...),
    content_type: str = Form(...),
    tenant_id: str | None = Form(None),
):
    request = IngestRequest(
        source_type=SourceType.AUDIO_URL,
        metadata={
            "domain": domain,
            "target_lang": target_lang,
            "level": level,
            "content_type": content_type,
            "tenant_id": tenant_id,
            "source_url": audio_url,
        },
        audio_url=audio_url,
    )

    pipeline = get_pipeline()
    result = await pipeline.run(request, file_content=None)

    return result