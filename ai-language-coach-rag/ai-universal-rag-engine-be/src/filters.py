from qdrant_client.http import models
from typing import Optional
from src.models import FilterMode
from src.config import get_parent_level


def build_query_filter(
    domain: str,
    target_lang: str,
    level: str,
    tenant_id: Optional[str] = None,
    content_type: Optional[str] = None,
    filter_mode: FilterMode = FilterMode.STRICT,
) -> models.Filter:
    conditions = [
        models.FieldCondition(
            key="domain",
            match=models.MatchValue(value=domain)
        ),
        models.FieldCondition(
            key="target_lang",
            match=models.MatchValue(value=target_lang)
        ),
    ]

    if filter_mode == FilterMode.STRICT:
        conditions.append(
            models.FieldCondition(
                key="level",
                match=models.MatchValue(value=level)
            )
        )
    elif filter_mode == FilterMode.PARENT:
        parent_level = get_parent_level(level)
        if parent_level:
            conditions.append(
                models.FieldCondition(
                    key="level",
                    match=models.MatchValue(value=parent_level)
                )
            )

    if content_type:
        conditions.append(
            models.FieldCondition(
                key="content_type",
                match=models.MatchValue(value=content_type)
            )
        )

    if tenant_id:
        conditions.append(
            models.FieldCondition(
                key="tenant_id",
                match=models.MatchValue(value=tenant_id)
            )
        )

    return models.Filter(must=conditions)


def build_ingest_filter(
    domain: Optional[str] = None,
    target_lang: Optional[str] = None,
    level: Optional[str] = None,
    tenant_id: Optional[str] = None,
    content_type: Optional[str] = None,
) -> models.Filter:
    conditions = []

    if domain:
        conditions.append(
            models.FieldCondition(
                key="domain",
                match=models.MatchValue(value=domain)
            )
        )
    if target_lang:
        conditions.append(
            models.FieldCondition(
                key="target_lang",
                match=models.MatchValue(value=target_lang)
            )
        )
    if level:
        conditions.append(
            models.FieldCondition(
                key="level",
                match=models.MatchValue(value=level)
            )
        )
    if tenant_id:
        conditions.append(
            models.FieldCondition(
                key="tenant_id",
                match=models.MatchValue(value=tenant_id)
            )
        )
    if content_type:
        conditions.append(
            models.FieldCondition(
                key="content_type",
                match=models.MatchValue(value=content_type)
            )
        )

    return models.Filter(must=conditions) if conditions else models.Filter(must=[])


def filter_to_dict(filter: models.Filter) -> dict:
    result = {}
    for condition in filter.must:
        if isinstance(condition, models.FieldCondition):
            key = condition.key
            match = condition.match
            if isinstance(match, models.MatchValue):
                result[key] = match.value
    return result