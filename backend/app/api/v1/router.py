from fastapi import APIRouter
from backend.app.api.v1.auth import router as auth_router
from backend.app.api.v1.dashboard import router as dashboard_router
from backend.app.api.v1.practice import router as practice_router
from backend.app.api.v1.resources import router as resources_router
from backend.app.api.v1.revision import router as revision_router
from backend.app.api.v1.syllabus import router as syllabus_router

api_v1_router = APIRouter()
api_v1_router.include_router(auth_router)
api_v1_router.include_router(dashboard_router)
api_v1_router.include_router(practice_router)
api_v1_router.include_router(resources_router)
api_v1_router.include_router(revision_router)
api_v1_router.include_router(syllabus_router)
