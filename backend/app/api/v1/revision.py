from fastapi import APIRouter, Query
from typing import List, Optional
from datetime import datetime
from backend.app.schemas.revision import SpacedReviewRequest, SpacedItemDTO
from backend.app.services.spaced_repetition import SpacedRepetitionService

router = APIRouter(prefix="/revision", tags=["revision"])

@router.post("/review", response_model=SpacedItemDTO)
async def submit_spaced_review(req: SpacedReviewRequest):
    """
    Applies SuperMemo SM-2 interval expansion based on student recall quality (0-5).
    """
    reps, interval, ef, next_due = SpacedRepetitionService.calculate_sm2(
        rating=req.rating,
        current_repetitions=1,
        current_interval_days=1,
        current_ease_factor=2.5
    )
    
    return SpacedItemDTO(
        id=f"SRS_{req.item_id}",
        item_id=req.item_id,
        item_type=req.item_type,
        repetitions=reps,
        interval_days=interval,
        ease_factor=ef,
        next_review_due=next_due,
        is_mastered=(interval >= 21)
    )
