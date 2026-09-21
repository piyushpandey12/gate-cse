from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, Text
from sqlalchemy.orm import relationship
import enum
from backend.app.db.base import Base

class ResourceType(str, enum.Enum):
    BOOK = "BOOK"
    PLAYLIST = "PLAYLIST"
    NPTEL = "NPTEL"
    NOTE = "NOTE"
    PRACTICE_SET = "PRACTICE_SET"
    COURSE = "COURSE"

class Resource(Base):
    """
    Structured resource entity strictly representing the curated
    Madhurima Rawat & GatePlus resources:
    Subject -> Topic -> Resource Type -> Resource.
    """
    __tablename__ = "resources"
    
    id = Column(String(64), primary_key=True)
    subject_id = Column(String(32), ForeignKey("subjects.id"), nullable=False)
    topic_id = Column(String(64), ForeignKey("topics.id"), nullable=True)
    title = Column(String(255), nullable=False)
    provider = Column(String(128), nullable=False) # e.g. Amit Khurana, Knowledge Gate, Gate Smashers, Galvin, CLRS, GO Classes
    resource_type = Column(String(32), default=ResourceType.PLAYLIST, nullable=False)
    description = Column(Text, nullable=False)
    url = Column(String(512), nullable=False)
    recommended_chapters = Column(String(256), nullable=True)
    is_free = Column(Boolean, default=True)
    is_bookmarked = Column(Boolean, default=False)
    
    subject = relationship("Subject", back_populates="resources")
    topic = relationship("Topic", back_populates="resources")
