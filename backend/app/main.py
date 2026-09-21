import time
import uuid
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from backend.app.core.config import settings
from backend.app.api.v1.router import api_v1_router

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Authoritative full-stack backend serving GATE CSE 2027 students with SM-2 spaced repetition, next-action guidance, and precise MCQ/MSQ/NAT evaluation.",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "PATCH"],
    allow_headers=["Authorization", "Content-Type"],
)


@app.middleware("http")
async def add_request_id(request: Request, call_next):
    request_id = request.headers.get("X-Request-ID", uuid.uuid4().hex[:16])
    start = time.time()
    response = await call_next(request)
    duration = round((time.time() - start) * 1000, 1)
    response.headers["X-Request-ID"] = request_id
    response.headers["X-Response-Time"] = f"{duration}ms"
    return response


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "error": {
                "code": "INTERNAL_ERROR",
                "message": "An unexpected error occurred",
                "details": {},
            },
            "request_id": request.headers.get("X-Request-ID", ""),
        },
    )


app.include_router(api_v1_router, prefix=settings.API_V1_STR)


@app.get("/health/live", tags=["system"])
async def health_live():
    return {"status": "alive", "service": "GATE CSE 2027 Backend", "version": settings.VERSION}


@app.get("/health/ready", tags=["system"])
async def health_ready():
    from sqlalchemy import text
    from backend.app.db.session import AsyncSessionLocal

    checks = {"database": "unknown"}
    healthy = True
    try:
        async with AsyncSessionLocal() as session:
            await session.execute(text("SELECT 1"))
        checks["database"] = "ok"
    except Exception as e:
        checks["database"] = f"error: {str(e)[:100]}"
        healthy = False

    status_code = 200 if healthy else 503
    return JSONResponse(
        status_code=status_code,
        content={
            "status": "ready" if healthy else "degraded",
            "service": "GATE CSE 2027 Backend",
            "version": settings.VERSION,
            "checks": checks,
        },
    )


@app.get("/health", tags=["system"])
async def health_check():
    return {
        "status": "healthy",
        "service": "GATE CSE 2027 Authoritative Backend",
        "version": settings.VERSION,
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=8000, reload=True)
