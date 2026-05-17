from abc import ABC, abstractmethod
from typing import Protocol, Optional, AsyncIterator
from src.models import RetrievedChunk
from src.config import settings


class Generator(Protocol):
    async def generate(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> str: ...

    async def generate_stream(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> AsyncIterator[str]: ...


class BaseGenerator(ABC):
    @abstractmethod
    async def generate(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> str:
        pass

    async def generate_stream(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> AsyncIterator[str]:
        result = await self.generate(query, context, native_lang, persona)
        yield result


class GeminiGenerator(BaseGenerator):
    def __init__(self, model_name: Optional[str] = None):
        self.model_name = model_name or settings.GEMINI_GENERATION_MODEL
        self._client = None

    @property
    def client(self):
        if self._client is None:
            import google.generativeai as genai
            genai.configure(api_key=settings.GEMINI_API_KEY)
            self._client = genai
        return self._client

    def _build_prompt(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> str:
        persona_prefix = ""
        if persona:
            persona_prefix = f"You are a {persona}. "

        context_text = "\n\n".join([
            f"[Source {i+1} (score: {c.score:.3f}, {c.metadata.content_type})]\n{c.text}"
            for i, c in enumerate(context)
        ])

        return f"""{persona_prefix}You are a helpful language learning assistant. The user is a {native_lang} native speaker learning a new language.

CONTEXT:
{context_text}

QUESTION: {query}

INSTRUCTIONS:
1. Answer the question based on the context provided
2. If the context doesn't contain enough information, say so clearly
3. Keep your response clear and concise
4. Use the user's native language ({native_lang}) for explanations when helpful
5. Cite sources when referencing specific information

ANSWER:"""

    async def generate(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> str:
        if not context:
            return "I don't have enough context to answer your question. Please try a different query or adjust your filters."

        prompt = self._build_prompt(query, context, native_lang, persona)

        response = self.client.generate_content(
            model=self.model_name,
            contents=[prompt]
        )
        return response.text

    async def generate_stream(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> AsyncIterator[str]:
        if not context:
            yield "I don't have enough context to answer your question. Please try a different query or adjust your filters."
            return

        prompt = self._build_prompt(query, context, native_lang, persona)

        response = self.client.generate_content(
            model=self.model_name,
            contents=[prompt],
            stream=True
        )

        for chunk in response:
            if chunk.text:
                yield chunk.text


class MockGenerator(BaseGenerator):
    async def generate(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> str:
        if not context:
            return "Mock response: No context available for query."

        sources = [f"[{c.metadata.content_type}: {c.text[:100]}...]" for c in context[:3]]
        return f"""Mock response for: {query}

Based on the retrieved context, here's the answer (mock mode):

The relevant information comes from {len(context)} source(s):
{chr(10).join(sources)}

This is a mock response generated in test mode. Set USE_MOCK=false to use real Gemini API."""

    async def generate_stream(
        self,
        query: str,
        context: list[RetrievedChunk],
        native_lang: str,
        persona: Optional[str] = None,
    ) -> AsyncIterator[str]:
        result = await self.generate(query, context, native_lang, persona)
        words = result.split()
        for word in words:
            yield word + " "
        yield "\n[Mock streaming complete]"


class ProGenerator(GeminiGenerator):
    def __init__(self):
        super().__init__(model_name=settings.GEMINI_PRO_MODEL)


def get_generator(use_pro: bool = False) -> BaseGenerator:
    if settings.USE_MOCK:
        return MockGenerator()
    if use_pro:
        return ProGenerator()
    return GeminiGenerator()