from src.handlers.base import BaseHandler
from src.handlers.doc_handler import DocHandler, create_doc_handler
from src.handlers.web_handler import WebHandler, create_web_handler
from src.handlers.media_handler import MediaHandler, create_media_handler
from src.models import SourceType


async def get_handler(source_type: SourceType) -> BaseHandler:
    handlers = {
        SourceType.FILE: create_doc_handler,
        SourceType.URL: create_web_handler,
        SourceType.AUDIO_URL: create_media_handler,
    }

    handler_factory = handlers.get(source_type)
    if handler_factory is None:
        raise ValueError(f"Unknown source type: {source_type}")

    return await handler_factory()