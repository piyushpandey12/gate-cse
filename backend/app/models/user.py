from sqlalchemy import Column, String, Integer, Float, Boolean, DateTime, Enum, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
import enum
from backend.app.db.base import Base

class UserRole(str, enum.Enum):
    STUDENT = "STUDENT"
    ADMIN = "ADMIN"
    FACULTY = "FACULTY"

class User(Base):
    __tablename__ = "users"
    
    id = Column(String(64), primary_key=True, index=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    name = Column(String(128), nullable=False)
    hashed_password = Column(String(255), nullable=False)
    role = Column(String(32), default=UserRole.STUDENT, nullable=False)
    is_active = Column(Boolean, default=True)
    is_premium = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    profile = relationship("UserProfile", back_populates="user", uselist=False)
    attempts = relationship("Attempt", back_populates="user")
    spaced_items = relationship("SpacedRepetitionRecord", back_populates="user")
    study_plan_entries = relationship("StudyPlanEntry", back_populates="user")

class UserProfile(Base):
    __tablename__ = "user_profiles"
    
    id = Column(String(64), primary_key=True, index=True)
    user_id = Column(String(64), ForeignKey("users.id"), unique=True, nullable=False)
    target_exam = Column(String(64), default="GATE CSE")
    target_year = Column(Integer, default=2027)
    target_score = Column(Float, default=78.0)
    target_rank = Column(Integer, default=150)
    current_streak = Column(Integer, default=1)
    longest_streak = Column(Integer, default=1)
    total_questions_solved = Column(Integer, default=0)
    overall_accuracy = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="profile")
