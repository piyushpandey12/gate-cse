from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, Text
from sqlalchemy.orm import relationship
from backend.app.db.base import Base

class Subject(Base):
    __tablename__ = "subjects"
    
    id = Column(String(32), primary_key=True) # e.g. 'OS', 'TOC', 'ALGO'
    code = Column(String(32), unique=True, nullable=False)
    name = Column(String(128), nullable=False)
    weightage_percent = Column(Float, default=8.0)
    display_order = Column(Integer, default=0)
    color_hex = Column(String(16), default="#06B6D4")
    icon_name = Column(String(64), default="MenuBook")
    total_pyq_count = Column(Integer, default=0)
    
    topics = relationship("Topic", back_populates="subject", cascade="all, delete-orphan")
    resources = relationship("Resource", back_populates="subject")

class Topic(Base):
    __tablename__ = "topics"
    
    id = Column(String(64), primary_key=True) # e.g. 'OS_DEADLOCK'
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=False)
    name = Column(String(128), nullable=False)
    difficulty = Column(String(16), default="MEDIUM") # EASY, MEDIUM, HARD
    pyq_count = Column(Integer, default=0)
    frequency_score = Column(Float, default=1.0)
    display_order = Column(Integer, default=0)
    summary = Column(Text, nullable=True)
    
    subject = relationship("Subject", back_populates="topics")
    questions = relationship("Question", back_populates="topic")
    resources = relationship("Resource", back_populates="topic")
