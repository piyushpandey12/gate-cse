from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from backend.app.db.base import Base

class SpacedRepetitionRecord(Base):
    __tablename__ = "spaced_repetition_records"
    
    id = Column(String(64), primary_key=True)
    user_id = Column(String(64), ForeignKey("users.id"), nullable=False)
    item_type = Column(String(32), default="QUESTION") # QUESTION, FLASHCARD, FORMULA
    item_id = Column(String(64), nullable=False)
    
    repetitions = Column(Integer, default=0)
    interval_days = Column(Integer, default=1)
    ease_factor = Column(Float, default=2.5)
    last_reviewed_at = Column(DateTime, default=datetime.utcnow)
    next_review_due = Column(DateTime, default=datetime.utcnow)
    is_mastered = Column(Boolean, default=False)
    
    user = relationship("User", back_populates="spaced_items")

class Flashcard(Base):
    __tablename__ = "flashcards"
    
    id = Column(String(64), primary_key=True)
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=False)
    topic_id = Column(String(64), ForeignKey("topics.id"), nullable=False)
    front_prompt = Column(String(512), nullable=False)
    back_explanation = Column(String(1024), nullable=False)
    latex_formula = Column(String(512), nullable=True)
    key_tag = Column(String(64), nullable=True)

class Formula(Base):
    __tablename__ = "formulas"
    
    id = Column(String(64), primary_key=True)
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=False)
    name = Column(String(128), nullable=False)
    formula_latex = Column(String(512), nullable=False)
    description = Column(String(512), nullable=False)
    applications = Column(String(256), nullable=True)
    is_favorite = Column(Boolean, default=False)

class StudyPlanEntry(Base):
    __tablename__ = "study_plan_entries"
    
    id = Column(String(64), primary_key=True)
    user_id = Column(String(64), ForeignKey("users.id"), nullable=False)
    day_date = Column(String(16), nullable=False) # e.g. "2027-02-15"
    title = Column(String(128), nullable=False)
    planned_minutes = Column(Integer, default=60)
    completed_minutes = Column(Integer, default=0)
    is_completed = Column(Boolean, default=False)
    
    user = relationship("User", back_populates="study_plan_entries")
