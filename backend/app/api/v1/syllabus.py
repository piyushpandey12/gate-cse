from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import Optional, List

from backend.app.core.deps import get_current_user
from backend.app.db.session import get_db
from backend.app.models.user import User
from backend.app.models.syllabus import Subject, Topic
from backend.app.models.question import Question
from backend.app.schemas.resource import ResourceDTO

router = APIRouter(prefix="/syllabus", tags=["syllabus"])


@router.get("/subjects")
async def get_subjects(db: AsyncSession = Depends(get_db)):
    """Get all subjects from PostgreSQL."""
    result = await db.execute(select(Subject).order_by(Subject.display_order))
    subjects = result.scalars().all()
    return [
        {
            "id": s.id,
            "name": s.name,
            "code": s.code,
            "weightage_percent": s.weightage_percent,
            "display_order": s.display_order,
            "color_hex": s.color_hex,
            "icon_name": s.icon_name,
            "total_pyq_count": s.total_pyq_count,
        }
        for s in subjects
    ]


@router.get("/subjects/{subject_id}/topics")
async def get_topics_for_subject(subject_id: str, db: AsyncSession = Depends(get_db)):
    """Get topics for a subject from PostgreSQL."""
    result = await db.execute(
        select(Topic).where(Topic.subject_id == subject_id).order_by(Topic.display_order)
    )
    topics = result.scalars().all()
    return [
        {
            "id": t.id,
            "subject_id": t.subject_id,
            "name": t.name,
            "difficulty": t.difficulty,
            "pyq_count": t.pyq_count,
            "frequency_score": t.frequency_score,
            "display_order": t.display_order,
            "summary": t.summary,
        }
        for t in topics
    ]


@router.get("/questions")
async def get_questions(
    subject_id: Optional[str] = Query(None),
    topic_id: Optional[str] = Query(None),
    year: Optional[int] = Query(None),
    question_type: Optional[str] = Query(None),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    """Get questions with pagination from PostgreSQL."""
    query = select(Question)
    count_query = select(Question.id)

    if subject_id:
        query = query.where(Question.subject_id == subject_id)
        count_query = count_query.where(Question.subject_id == subject_id)
    if topic_id:
        query = query.where(Question.topic_id == topic_id)
        count_query = count_query.where(Question.topic_id == topic_id)
    if year:
        query = query.where(Question.year == year)
        count_query = count_query.where(Question.year == year)
    if question_type:
        query = query.where(Question.question_type == question_type)
        count_query = count_query.where(Question.question_type == question_type)

    # Count total
    from sqlalchemy import func
    total_result = await db.execute(select(func.count()).select_from(count_query.subquery()))
    total = total_result.scalar() or 0

    # Paginate
    offset = (page - 1) * page_size
    query = query.order_by(Question.year.desc()).offset(offset).limit(page_size)
    result = await db.execute(query)
    questions = result.scalars().all()

    return {
        "data": [
            {
                "id": q.id,
                "subject_id": q.subject_id,
                "topic_id": q.topic_id,
                "year": q.year,
                "set_session": q.set_session,
                "question_type": q.question_type,
                "marks": q.marks,
                "negative_marks": q.negative_marks,
                "difficulty": q.difficulty,
                "problem_statement": q.problem_statement,
                "options": q.options,
            }
            for q in questions
        ],
        "total": total,
        "page": page,
        "page_size": page_size,
        "has_next": offset + page_size < total,
    }


@router.get("/questions/{question_id}")
async def get_question(question_id: str, db: AsyncSession = Depends(get_db)):
    """Get a single question by ID."""
    result = await db.execute(select(Question).where(Question.id == question_id))
    q = result.scalars().first()
    if not q:
        from fastapi import HTTPException
        raise HTTPException(status_code=404, detail="Question not found")
    return {
        "id": q.id,
        "subject_id": q.subject_id,
        "topic_id": q.topic_id,
        "year": q.year,
        "set_session": q.set_session,
        "question_type": q.question_type,
        "marks": q.marks,
        "negative_marks": q.negative_marks,
        "difficulty": q.difficulty,
        "problem_statement": q.problem_statement,
        "options": q.options,
        "correct_answers": q.correct_answers,
        "detailed_solution": q.detailed_solution,
        "key_formula": q.key_formula,
        "shortcut_trick": q.shortcut_trick,
        "common_trap": q.common_trap,
    }
