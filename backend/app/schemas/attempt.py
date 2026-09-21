from pydantic import BaseModel
from typing import Optional

class SubmitAnswerRequest(BaseModel):
    question_id: str
    user_answer: str
    time_taken_seconds: int = 45
    mistake_category: Optional[str] = None
    user_notes: Optional[str] = None

class EvaluationResultDTO(BaseModel):
    is_correct: bool
    marks_obtained: float
    correct_answer: str
    detailed_solution: str
    key_formula: Optional[str] = None
    shortcut_trick: Optional[str] = None
    common_trap: Optional[str] = None
    logged_to_error_notebook: bool = False
    next_review_days: int = 1
