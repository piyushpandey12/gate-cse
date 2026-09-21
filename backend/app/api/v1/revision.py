from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Optional
from datetime import datetime, timezone

from backend.app.core.deps import get_current_user
from backend.app.db.session import get_db
from backend.app.models.user import User
from backend.app.models.revision import SpacedRepetitionRecord, Flashcard, Formula
from backend.app.schemas.revision import SpacedReviewRequest, SpacedItemDTO, FlashcardDTO, FormulaDTO
from backend.app.services.spaced_repetition import SpacedRepetitionService

router = APIRouter(prefix="/revision", tags=["revision"])


@router.post("/review", response_model=SpacedItemDTO)
async def submit_spaced_review(
    req: SpacedReviewRequest,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Applies SuperMemo SM-2 interval expansion for authenticated user."""
    # Find existing record
    result = await db.execute(
        select(SpacedRepetitionRecord).where(
            SpacedRepetitionRecord.user_id == current_user.id,
            SpacedRepetitionRecord.item_id == req.item_id,
        )
    )
    record = result.scalars().first()

    current_reps = record.repetitions if record else 0
    current_interval = record.interval_days if record else 1
    current_ef = record.ease_factor if record else 2.5

    reps, interval, ef, next_due = SpacedRepetitionService.calculate_sm2(
        rating=req.rating,
        current_repetitions=current_reps,
        current_interval_days=current_interval,
        current_ease_factor=current_ef,
    )

    if record:
        record.repetitions = reps
        record.interval_days = interval
        record.ease_factor = ef
        record.next_review_due = next_due
        record.is_mastered = interval >= 21
        record.last_reviewed_at = datetime.now(timezone.utc)
    else:
        import uuid
        record = SpacedRepetitionRecord(
            id=str(uuid.uuid4()),
            user_id=current_user.id,
            item_type=req.item_type,
            item_id=req.item_id,
            repetitions=reps,
            interval_days=interval,
            ease_factor=ef,
            next_review_due=next_due,
            is_mastered=interval >= 21,
        )
        db.add(record)

    await db.commit()

    return SpacedItemDTO(
        id=record.id,
        item_id=record.item_id,
        item_type=record.item_type,
        repetitions=reps,
        interval_days=interval,
        ease_factor=ef,
        next_review_due=next_due,
        is_mastered=interval >= 21,
    )


@router.get("/flashcards", response_model=List[FlashcardDTO])
async def get_flashcards(
    subject_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
):
    """Get flashcards from PostgreSQL."""
    query = select(Flashcard)
    if subject_id:
        query = query.where(Flashcard.subject_id == subject_id)

    result = await db.execute(query)
    flashcards = result.scalars().all()

    return [
        FlashcardDTO(
            id=f.id,
            subject_id=f.subject_id,
            topic_id=f.topic_id,
            front_prompt=f.front_prompt,
            back_explanation=f.back_explanation,
            latex_formula=f.latex_formula,
            key_tag=f.key_tag,
        )
        for f in flashcards
    ]


@router.get("/formulas", response_model=List[FormulaDTO])
async def get_formulas(
    subject_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
):
    """Get formulas from PostgreSQL."""
    query = select(Formula)
    if subject_id:
        query = query.where(Formula.subject_id == subject_id)

    result = await db.execute(query)
    formulas = result.scalars().all()

    return [
        FormulaDTO(
            id=f.id,
            subject_id=f.subject_id,
            name=f.name,
            formula_latex=f.formula_latex,
            description=f.description,
            applications=f.applications,
            is_favorite=f.is_favorite,
        )
        for f in formulas
    ]
