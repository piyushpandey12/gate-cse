from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, DateTime, JSON
from datetime import datetime
from backend.app.db.base import Base

class MockTest(Base):
    __tablename__ = "mock_tests"
    
    id = Column(String(64), primary_key=True)
    title = Column(String(128), nullable=False)
    test_type = Column(String(32), default="FULL_MOCK") # TOPIC_WISE, SUBJECT_WISE, FULL_MOCK
    duration_minutes = Column(Integer, default=180)
    total_marks = Column(Float, default=100.0)
    total_questions = Column(Integer, default=65)
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=True)
    is_live = Column(Boolean, default=True)

class TestSubmission(Base):
    __tablename__ = "test_submissions"
    
    id = Column(String(64), primary_key=True)
    user_id = Column(String(64), ForeignKey("users.id"), nullable=False)
    test_id = Column(String(64), ForeignKey("mock_tests.id"), nullable=False)
    score_obtained = Column(Float, default=0.0)
    total_marks = Column(Float, default=100.0)
    rank = Column(Integer, nullable=True)
    percentile = Column(Float, nullable=True)
    time_taken_seconds = Column(Integer, default=0)
    submitted_at = Column(DateTime, default=datetime.utcnow)
    answer_sheet = Column(JSON, nullable=True)
