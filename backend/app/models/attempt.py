from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, Text, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
import enum
from backend.app.db.base import Base

class MistakeCategory(str, enum.Enum):
    CONCEPTUAL = "CONCEPTUAL"
    CALCULATION = "CALCULATION"
    MISREAD = "MISREAD"
    TIME_PRESSURE = "TIME_PRESSURE"
    FORMULA_FORGOTTEN = "FORMULA_FORGOTTEN"

class Attempt(Base):
    __tablename__ = "attempts"
    
    id = Column(String(64), primary_key=True)
    user_id = Column(String(64), ForeignKey("users.id"), nullable=False)
    question_id = Column(String(64), ForeignKey("questions.id"), nullable=False)
    user_answer = Column(String(128), nullable=False)
    is_correct = Column(Boolean, nullable=False)
    marks_obtained = Column(Float, nullable=False)
    time_taken_seconds = Column(Integer, default=60)
    attempted_at = Column(DateTime, default=datetime.utcnow)
    
    # Error Notebook categorization if incorrect
    mistake_category = Column(String(32), nullable=True)
    user_notes = Column(Text, nullable=True)
    is_bookmarked = Column(Boolean, default=False)
    
    user = relationship("User", back_populates="attempts")
    question = relationship("Question", back_populates="attempts")
