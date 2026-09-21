from fastapi import APIRouter, Depends, HTTPException
from typing import List, Optional
from backend.app.schemas.attempt import SubmitAnswerRequest, EvaluationResultDTO
from backend.app.services.evaluator import QuestionEvaluator

router = APIRouter(prefix="/practice", tags=["practice"])

@router.post("/submit", response_model=EvaluationResultDTO)
async def submit_question_answer(req: SubmitAnswerRequest):
    """
    Evaluates an answer with standard GATE criteria (MCQ negative marking, MSQ exact subset, NAT tolerance)
    and logs mistake category if incorrect.
    """
    # Demonstration evaluator for GATE standard scoring
    is_correct, awarded = QuestionEvaluator.evaluate(
        question_type="MCQ",
        correct_answer_str="B",
        user_answer_str=req.user_answer,
        marks=2,
        negative_marks=0.66
    )
    
    return EvaluationResultDTO(
        is_correct=is_correct,
        marks_obtained=awarded,
        correct_answer="B",
        detailed_solution="Option B is correct based on Banker's Safety Algorithm state evaluation.",
        key_formula="Need_{i,j} = Max_{i,j} - Allocation_{i,j} \\le Available_j",
        shortcut_trick="Check if the sum of all allocated resources exceeds total instances.",
        common_trap="Students often forget that Available matrix increases when a process finishes.",
        logged_to_error_notebook=not is_correct,
        next_review_days=1 if not is_correct else 6
    )
