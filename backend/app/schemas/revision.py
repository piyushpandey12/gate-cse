from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class SpacedReviewRequest(BaseModel):
    item_id: str
    item_type: str = "QUESTION" # QUESTION, FLASHCARD, FORMULA
    rating: int # 0 to 5 (SM-2 scale)

class SpacedItemDTO(BaseModel):
    id: str
    item_id: str
    item_type: str
    repetitions: int
    interval_days: int
    ease_factor: float
    next_review_due: datetime
    is_mastered: bool

class FlashcardDTO(BaseModel):
    id: str
    subject_id: str
    topic_id: str
    front_prompt: str
    back_explanation: str
    latex_formula: Optional[str] = None
    key_tag: Optional[str] = None

class FormulaDTO(BaseModel):
    id: str
    subject_id: str
    name: str
    formula_latex: str
    description: str
    applications: Optional[str] = None
    is_favorite: bool = False
