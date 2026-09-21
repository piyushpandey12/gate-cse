from pydantic import BaseModel, EmailStr
from typing import Optional

class UserLoginRequest(BaseModel):
    email: EmailStr
    password: str

class UserRegisterRequest(BaseModel):
    email: EmailStr
    password: str
    name: str
    target_year: int = 2027

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    email: str
    name: str
    role: str

class UserProfileDTO(BaseModel):
    user_id: str
    name: str
    email: str
    role: str
    target_exam: str
    target_year: int
    target_score: float
    current_streak: int
    total_questions_solved: int
    overall_accuracy: float
