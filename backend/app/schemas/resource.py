from pydantic import BaseModel
from typing import Optional

class ResourceDTO(BaseModel):
    id: str
    subject_id: str
    topic_id: Optional[str] = None
    title: str
    provider: str
    resource_type: str # BOOK, PLAYLIST, NPTEL, NOTE, PRACTICE_SET, COURSE
    description: str
    url: str
    recommended_chapters: Optional[str] = None
    is_free: bool = True
    is_bookmarked: bool = False
