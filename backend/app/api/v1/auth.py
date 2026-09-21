from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import uuid

from backend.app.core.security import get_password_hash, verify_password, create_access_token
from backend.app.db.session import get_db
from backend.app.models.user import User, UserProfile, UserRole
from backend.app.schemas.auth import UserLoginRequest, UserRegisterRequest, TokenResponse, UserProfileDTO

router = APIRouter(prefix="/auth", tags=["auth"])

@router.post("/register", response_model=TokenResponse)
async def register(req: UserRegisterRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == req.email))
    existing_user = result.scalars().first()
    if existing_user:
        raise HTTPException(status_code=400, detail="User with this email already registered")
        
    user_id = str(uuid.uuid4())
    user = User(
        id=user_id,
        email=req.email,
        name=req.name,
        hashed_password=get_password_hash(req.password),
        role=UserRole.STUDENT,
        is_active=True
    )
    profile = UserProfile(
        id=str(uuid.uuid4()),
        user_id=user_id,
        target_exam="GATE CSE",
        target_year=req.target_year,
        current_streak=1,
        total_questions_solved=0,
        overall_accuracy=0.0
    )
    db.add(user)
    db.add(profile)
    await db.commit()
    
    token = create_access_token(subject=user_id)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user_id,
        email=user.email,
        name=user.name,
        role=user.role
    )

@router.post("/login", response_model=TokenResponse)
async def login(req: UserLoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == req.email))
    user = result.scalars().first()
    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")
        
    token = create_access_token(subject=user.id)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        email=user.email,
        name=user.name,
        role=user.role
    )
