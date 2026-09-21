from pydantic import BaseModel
from typing import List, Optional, Any

class QuestionOptionDTO(BaseModel):
    key: str
    text: str

class QuestionDTO(BaseModel):
    id: str
    subject_id: str
    topic_id: str
    year: Optional[int] = None
    set_session: Optional[str] = None
    question_type: str # MCQ, MSQ, NAT
    marks: int
    negative_marks: float
    difficulty: str
    problem_statement: str
    options: Optional[List[QuestionOptionDTO]] = None
    correct_answers: str
    detailed_solution: str
    key_formula: Optional[str] = None
    shortcut_trick: Optional[str] = None
    common_trap: Optional[str] = None

class PracticeSessionRequest(BaseModel):
    subject_id: Optional[str] = None
    topic_id: Optional[str] = None
    question_count: int = 10
    include_pyq_only: bool = False
    difficulty: Optional[str] = None
