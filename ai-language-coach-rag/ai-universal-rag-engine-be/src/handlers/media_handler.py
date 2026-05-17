import io
import tempfile
import os
from typing import Optional
import yt_dlp
from faster_whisper import WhisperModel
from src.handlers.base import BaseHandler
from src.models import Chunk, ChunkMetadata
from src.config import get_chunk_size


class MediaHandler(BaseHandler):
    def __init__(self):
        self.whisper_model = WhisperModel("base", device="cpu", compute_type="int8")

    async def process(
        self,
        source: str,
        metadata: ChunkMetadata,
        filename: str | None = None
    ) -> list[Chunk]:
        audio_path = await self._download_audio(source)
        try:
            segments, info = self.whisper_model.transcribe(
                audio_path,
                language=None,
                beam_size=5,
                vad_filter=True
            )
            transcript_segments = list(segments)
            return self._chunk_transcript(transcript_segments, metadata)
        finally:
            if os.path.exists(audio_path):
                os.remove(audio_path)

    async def _download_audio(self, url: str) -> str:
        if "youtube.com" in url or "youtu.be" in url:
            return await self._download_youtube(url)

        with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as tmp:
            tmp_path = tmp.name

        ydl_opts = {
            "format": "bestaudio/best",
            "outtmpl": tmp_path.replace(".mp3", ".%(ext)s"),
            "postprocessors": [{
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            }],
        }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])

        return tmp_path

    async def _download_youtube(self, url: str) -> str:
        tmp_path = tempfile.mktemp(suffix=".mp3")

        ydl_opts = {
            "format": "bestaudio/best",
            "outtmpl": tmp_path.replace(".mp3", ".%(ext)s"),
            "postprocessors": [{
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            }],
        }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            filename = ydl.prepare_filename(info)
            return filename

    def _chunk_transcript(
        self,
        segments: list,
        metadata: ChunkMetadata
    ) -> list[Chunk]:
        chunk_size = get_chunk_size("audio")
        chunks = []
        current_text = []
        current_size = 0
        start_time = None
        end_time = None

        for seg in segments:
            seg_text = seg.text.strip()
            seg_size = len(seg_text.split())

            if start_time is None:
                start_time = seg.start

            if current_size + seg_size > chunk_size and current_text:
                chunk_text = " ".join(current_text).strip()
                chunks.append(Chunk(
                    text=chunk_text,
                    metadata=metadata.model_copy(update={
                        "timestamp_start": start_time,
                        "timestamp_end": end_time
                    })
                ))
                current_text = [seg_text]
                current_size = seg_size
                start_time = seg.start
            else:
                current_text.append(seg_text)
                current_size += seg_size
                end_time = seg.end

        if current_text:
            chunk_text = " ".join(current_text).strip()
            chunks.append(Chunk(
                text=chunk_text,
                metadata=metadata.model_copy(update={
                    "timestamp_start": start_time,
                    "timestamp_end": end_time
                })
            ))

        return chunks


async def create_media_handler() -> MediaHandler:
    return MediaHandler()