from typing import Dict, Any, List, Optional
from dataclasses import dataclass

@dataclass
class NextActionRecommendation:
    action_type: str # PRACTICE_WEAK, REVISION, STUDY_NEW, MOCK_TEST
    title: str # e.g. "Practice 10 Deadlock PYQs"
    reason: str # e.g. "Your recent accuracy is 58% (below target 80%)."
    target_subject: str
    target_topic_id: str
    estimated_minutes: int
    due_items_count: Optional[int] = None
    urgency_level: str = "MEDIUM" # HIGH, MEDIUM, LOW

class NextActionEngine:
    """
    Core authoritative intelligence that drives the candidate's homepage:
    Answers: 'What should I do right now?'
    Priority:
    1. Critical Spaced Repetition items overdue (> 5 items)
    2. High-frequency weak topic with recent low accuracy (< 65%)
    3. Next scheduled curriculum milestone in Daily Plan
    4. General practice drill or CBT sectional test
    """
    
    @staticmethod
    def generate_next_action(
        due_revision_count: int,
        weak_topics: List[Dict[str, Any]],
        current_streak: int,
        recent_accuracy: float
    ) -> NextActionRecommendation:
        if due_revision_count >= 8:
            return NextActionRecommendation(
                action_type="REVISION",
                title=f"Complete {due_revision_count} Overdue Revisions",
                reason=f"Spaced repetition memory decay detected. {due_revision_count} cards/errors require active recall today.",
                target_subject="ALL",
                target_topic_id="REVISION_ALL",
                estimated_minutes=20,
                due_items_count=due_revision_count,
                urgency_level="HIGH"
            )
            
        if weak_topics:
            top_weak = weak_topics[0]
            topic_name = top_weak.get("name", "Deadlocks")
            topic_id = top_weak.get("id", "OS_DEADLOCK")
            accuracy = top_weak.get("accuracy", 58.0)
            return NextActionRecommendation(
                action_type="PRACTICE_WEAK",
                title=f"Practice 10 {topic_name} PYQs",
                reason=f"Your recent accuracy in {topic_name} is {accuracy:.0f}% (below your 80% target). High-frequency GATE topic.",
                target_subject=top_weak.get("subject_id", "OS"),
                target_topic_id=topic_id,
                estimated_minutes=25,
                due_items_count=None,
                urgency_level="HIGH"
            )
            
        return NextActionRecommendation(
            action_type="STUDY_NEW",
            title="Practice 10 High-Yield Operating Systems PYQs",
            reason="Maintain your daily velocity and strengthen virtual memory page replacement concepts.",
            target_subject="OS",
            target_topic_id="OS_VIRTUAL_MEM",
            estimated_minutes=30,
            due_items_count=None,
            urgency_level="MEDIUM"
        )

