from pydantic import BaseModel
from typing import List, Optional

class NextActionRecommendationDTO(BaseModel):
    action_type: str # PRACTICE_WEAK, REVISION, STUDY_NEW, MOCK_TEST
    title: str # e.g. "Practice 10 Deadlock PYQs"
    reason: str # e.g. "Your recent accuracy is 58% (below target 80%)."
    target_subject: str
    target_topic_id: str
    estimated_minutes: int
    due_items_count: Optional[int] = None
    urgency_level: str # HIGH, MEDIUM, LOW

class TodayPlanDTO(BaseModel):
    planned_minutes: int
    completed_minutes: int
    tasks_count: int
    tasks_completed: int

class ContinueLearningDTO(BaseModel):
    subject_id: str
    subject_name: str
    topic_id: str
    topic_name: str
    progress_percentage: int

class DashboardNextActionResponse(BaseModel):
    target_exam: str
    days_remaining: int
    next_action: NextActionRecommendationDTO
    today_plan: TodayPlanDTO
    continue_learning: ContinueLearningDTO
    due_revision_count: int
    weak_topics_count: int
    total_solved: int
    overall_accuracy: float
    streak_days: int
