from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import Optional, List

from backend.app.core.deps import get_current_user
from backend.app.db.session import get_db
from backend.app.models.user import User
from backend.app.models.resource import Resource
from backend.app.schemas.resource import ResourceDTO

router = APIRouter(prefix="/resources", tags=["resources"])


@router.get("", response_model=List[ResourceDTO])
async def get_resources(
    subject_id: Optional[str] = Query(None),
    resource_type: Optional[str] = Query(None),
    provider: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
):
    """Get resources from PostgreSQL."""
    query = select(Resource)
    if subject_id:
        query = query.where(Resource.subject_id == subject_id)
    if resource_type:
        query = query.where(Resource.resource_type == resource_type)
    if provider:
        query = query.where(Resource.provider.ilike(f"%{provider}%"))
    query = query.order_by(Resource.subject_id, Resource.resource_type)

    result = await db.execute(query)
    resources = result.scalars().all()

    return [
        ResourceDTO(
            id=r.id,
            subject_id=r.subject_id,
            topic_id=r.topic_id,
            title=r.title,
            provider=r.provider,
            resource_type=r.resource_type,
            description=r.description,
            url=r.url,
            recommended_chapters=r.recommended_chapters,
            is_free=r.is_free,
            is_bookmarked=r.is_bookmarked,
        )
        for r in resources
    ]
