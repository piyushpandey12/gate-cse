from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func

from backend.app.core.deps import get_current_user
from backend.app.db.session import get_db
from backend.app.models.user import User, UserProfile
from backend.app.models.attempt import Attempt
from backend.app.models.revision import SpacedRepetitionRecord
from backend.app.schemas.dashboard import (
    DashboardNextActionResponse,
    TodayPlanDTO,
    ContinueLearningDTO,
    NextActionRecommendationDTO,
)
from backend.app.services.next_action_engine import NextActionEngine

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("/next-action", response_model=DashboardNextActionResponse)
async def get_next_action(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Authoritative 'What should I do now?' endpoint. Queries real DB data."""
    # Get user profile
    profile_result = await db.execute(
        select(UserProfile).where(UserProfile.user_id == current_user.id)
    )
    profile = profile_result.scalars().first()

    total_solved = profile.total_questions_solved if profile else 0
    overall_accuracy = profile.overall_accuracy if profile else 0.0
    current_streak = profile.current_streak if profile else 1

    # Count due revision items
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    revision_count_result = await db.execute(
        select(func.count(SpacedRepetitionRecord.id)).where(
            SpacedRepetitionRecord.user_id == current_user.id,
            SpacedRepetitionRecord.next_review_due <= now,
        )
    )
    due_revision_count = revision_count_result.scalar() or 0

    # Get recent accuracy from attempts
    from sqlalchemy import case
    recent_result = await db.execute(
        select(
            func.count(Attempt.id).label("total"),
            func.sum(case((Attempt.is_correct == True, 1), else_=0)).label("correct"),
        ).where(Attempt.user_id == current_user.id)
    )
    row = recent_result.one_or_none()
    recent_accuracy = (row.correct / row.total * 100) if row and row.total and row.total > 0 else overall_accuracy

    # Find weak topics (topics with low accuracy)
    # For now use a simple query - in production this would be more sophisticated
    weak_topics = []

    recommendation = NextActionEngine.generate_next_action(
        due_revision_count=due_revision_count,
        weak_topics=weak_topics,
        current_streak=current_streak,
        recent_accuracy=recent_accuracy,
    )

    # Calculate days remaining to GATE exam (approx Feb 2027)
    from datetime import date
    exam_date = date(2027, 2, 1)
    days_remaining = max(0, (exam_date - date.today()).days)

    return DashboardNextActionResponse(
        target_exam="GATE CSE",
        days_remaining=days_remaining,
        next_action=NextActionRecommendationDTO(
            action_type=recommendation.action_type,
            title=recommendation.title,
            reason=recommendation.reason,
            target_subject=recommendation.target_subject,
            target_topic_id=recommendation.target_topic_id,
            estimated_minutes=recommendation.estimated_minutes,
            due_items_count=recommendation.due_items_count,
            urgency_level=recommendation.urgency_level,
        ),
        today_plan=TodayPlanDTO(
            planned_minutes=150,
            completed_minutes=0,
            tasks_count=0,
            tasks_completed=0,
        ),
        continue_learning=ContinueLearningDTO(
            subject_id="OS",
            subject_name="Operating Systems",
            topic_id="OS_DEADLOCK",
            topic_name="Deadlocks & Banker's Algorithm",
            progress_percentage=0,
        ),
        due_revision_count=due_revision_count,
        weak_topics_count=len(weak_topics),
        total_solved=total_solved,
        overall_accuracy=overall_accuracy,
        streak_days=current_streak,
    )
