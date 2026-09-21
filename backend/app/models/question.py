from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, Text, JSON
from sqlalchemy.orm import relationship
import enum
from backend.app.db.base import Base

class QuestionType(str, enum.Enum):
    MCQ = "MCQ"
    MSQ = "MSQ"
    NAT = "NAT"

class Question(Base):
    __tablename__ = "questions"
    
    id = Column(String(64), primary_key=True)
    topic_id = Column(String(64), ForeignKey("topics.id"), nullable=False)
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=False)
    year = Column(Integer, nullable=True) # e.g. 2024
    set_session = Column(String(16), nullable=True) # e.g. 'Set 1'
    question_type = Column(String(16), default=QuestionType.MCQ, nullable=False)
    marks = Column(Integer, default=1)
    negative_marks = Column(Float, default=0.33)
    difficulty = Column(String(16), default="MEDIUM")
    
    problem_statement = Column(Text, nullable=False)
    options = Column(JSON, nullable=True) # list of options: [{"key": "A", "text": "..."}, ...]
    correct_answers = Column(String(64), nullable=False) # e.g. 'B' or 'A,C' or '2.5'
    tolerance_lower = Column(Float, nullable=True) # for NAT
    tolerance_upper = Column(Float, nullable=True) # for NAT
    
    detailed_solution = Column(Text, nullable=False)
    key_formula = Column(Text, nullable=True) # LaTeX math notation
    shortcut_trick = Column(Text, nullable=True)
    common_trap = Column(Text, nullable=True)
    
    total_attempts = Column(Integer, default=0)
    correct_attempts = Column(Integer, default=0)
    
    topic = relationship("Topic", back_populates="questions")
    attempts = relationship("Attempt", back_populates="question")
