from fastapi import APIRouter, Depends
from datetime import date
from backend.app.schemas.dashboard import (
    DashboardNextActionResponse,
    TodayPlanDTO,
    ContinueLearningDTO,
    NextActionRecommendationDTO
)
from backend.app.services.next_action_engine import NextActionEngine

router = APIRouter(prefix="/dashboard", tags=["dashboard"])

@router.get("/next-action", response_model=DashboardNextActionResponse)
async def get_next_action():
    """
    Primary authoritative endpoint that answers:
    'What should I do now?'
    Priority:
    1. Next action
    2. Today's plan
    3. Continue learning
    4. Revision
    5. Weak topic
    6. Progress
    7. Statistics
    """
    weak_topics = [
        {
            "id": "OS_DEADLOCK",
            "subject_id": "OS",
            "name": "Deadlocks & Resource Allocation",
            "accuracy": 58.0
        }
    ]
    
    recommendation = NextActionEngine.generate_next_action(
        due_revision_count=8,
        weak_topics=weak_topics,
        current_streak=7,
        recent_accuracy=64.5
    )
    
    return DashboardNextActionResponse(
        target_exam="GATE CSE",
        days_remaining=320,
        next_action=recommendation,
        today_plan=TodayPlanDTO(
            planned_minutes=150,
            completed_minutes=80,
            tasks_count=3,
            tasks_completed=1
        ),
        continue_learning=ContinueLearningDTO(
            subject_id="OS",
            subject_name="Operating Systems",
            topic_id="OS_DEADLOCK",
            topic_name="Deadlocks & Banker's Algorithm",
            progress_percentage=70
        ),
        due_revision_count=8,
        weak_topics_count=1,
        total_solved=42,
        overall_accuracy=68.5,
        streak_days=7
    )
