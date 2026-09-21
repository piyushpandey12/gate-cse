from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import Optional, List
import uuid
from datetime import datetime, timezone

from backend.app.core.deps import get_current_user
from backend.app.db.session import get_db
from backend.app.models.user import User
from backend.app.models.question import Question
from backend.app.models.attempt import Attempt
from backend.app.models.revision import SpacedRepetitionRecord
from backend.app.schemas.attempt import SubmitAnswerRequest, EvaluationResultDTO
from backend.app.services.evaluator import QuestionEvaluator
from backend.app.services.spaced_repetition import SpacedRepetitionService

router = APIRouter(prefix="/practice", tags=["practice"])


@router.post("/submit", response_model=EvaluationResultDTO)
async def submit_question_answer(
    req: SubmitAnswerRequest,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Evaluates an answer using the authoritative question from PostgreSQL.
    No hardcoded data. All evaluation comes from the database.
    """
    # Load question from PostgreSQL
    result = await db.execute(select(Question).where(Question.id == req.question_id))
    question = result.scalars().first()
    if not question:
        raise HTTPException(status_code=404, detail="Question not found")

    # Evaluate using database question
    is_correct, awarded = QuestionEvaluator.evaluate(
        question_type=question.question_type,
        correct_answer_str=question.correct_answers,
        user_answer_str=req.user_answer,
        marks=question.marks,
        negative_marks=question.negative_marks,
        tolerance_lower=question.tolerance_lower,
        tolerance_upper=question.tolerance_upper,
    )

    # Create attempt record
    attempt_id = str(uuid.uuid4())
    attempt = Attempt(
        id=attempt_id,
        user_id=current_user.id,
        question_id=req.question_id,
        user_answer=req.user_answer,
        is_correct=is_correct,
        marks_obtained=awarded,
        time_taken_seconds=req.time_taken_seconds,
        mistake_category=req.mistake_category,
        user_notes=req.user_notes,
    )
    db.add(attempt)

    # Update question statistics
    question.total_attempts += 1
    if is_correct:
        question.correct_attempts += 1

    # Update spaced repetition
    sr_result = await db.execute(
        select(SpacedRepetitionRecord).where(
            SpacedRepetitionRecord.user_id == current_user.id,
            SpacedRepetitionRecord.item_id == req.question_id,
        )
    )
    sr_record = sr_result.scalars().first()

    rating = 5 if is_correct else 0
    reps, interval, ef, next_due = SpacedRepetitionService.calculate_sm2(
        rating=rating,
        current_repetitions=sr_record.repetitions if sr_record else 0,
        current_interval_days=sr_record.interval_days if sr_record else 1,
        current_ease_factor=sr_record.ease_factor if sr_record else 2.5,
    )

    if sr_record:
        sr_record.repetitions = reps
        sr_record.interval_days = interval
        sr_record.ease_factor = ef
        sr_record.next_review_due = next_due
        sr_record.is_mastered = interval >= 21
        sr_record.last_reviewed_at = datetime.now(timezone.utc)
    else:
        new_sr = SpacedRepetitionRecord(
            id=str(uuid.uuid4()),
            user_id=current_user.id,
            item_type="QUESTION",
            item_id=req.question_id,
            repetitions=reps,
            interval_days=interval,
            ease_factor=ef,
            next_review_due=next_due,
            is_mastered=interval >= 21,
        )
        db.add(new_sr)

    await db.commit()

    return EvaluationResultDTO(
        is_correct=is_correct,
        marks_obtained=awarded,
        correct_answer=question.correct_answers,
        detailed_solution=question.detailed_solution,
        key_formula=question.key_formula,
        shortcut_trick=question.shortcut_trick,
        common_trap=question.common_trap,
        logged_to_error_notebook=not is_correct,
        next_review_days=interval,
    )


@router.get("/questions", response_model=List[dict])
async def get_practice_questions(
    subject_id: Optional[str] = Query(None),
    topic_id: Optional[str] = Query(None),
    question_count: int = Query(10, ge=1, le=50),
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Fetch questions for practice from PostgreSQL."""
    query = select(Question)
    if subject_id:
        query = query.where(Question.subject_id == subject_id)
    if topic_id:
        query = query.where(Question.topic_id == topic_id)
    query = query.order_by(Question.year.desc()).limit(question_count)

    result = await db.execute(query)
    questions = result.scalars().all()

    return [
        {
            "id": q.id,
            "subject_id": q.subject_id,
            "topic_id": q.topic_id,
            "year": q.year,
            "question_type": q.question_type,
            "marks": q.marks,
            "negative_marks": q.negative_marks,
            "difficulty": q.difficulty,
            "problem_statement": q.problem_statement,
            "options": q.options,
        }
        for q in questions
    ]
