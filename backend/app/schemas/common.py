from pydantic import BaseModel
from typing import Any, Optional, Generic, TypeVar, List
import uuid
import time

T = TypeVar("T")


class ErrorResponse(BaseModel):
    success: bool = False
    error: ErrorDetail
    request_id: str = ""


class ErrorDetail(BaseModel):
    code: str
    message: str
    details: Optional[Any] = None


class SuccessResponse(BaseModel, Generic[T]):
    success: bool = True
    data: T
    request_id: str = ""


class PaginatedResponse(BaseModel, Generic[T]):
    success: bool = True
    data: List[T]
    total: int
    page: int
    page_size: int
    has_next: bool
    request_id: str = ""


def generate_request_id() -> str:
    return uuid.uuid4().hex[:16]


def make_error_response(code: str, message: str, status_code: int = 400, details: Any = None) -> HTTPException:
    """Create a standardized HTTPException."""
    return HTTPException(
        status_code=status_code,
        detail={
            "success": False,
            "error": {
                "code": code,
                "message": message,
                "details": details,
            },
            "request_id": generate_request_id(),
        },
    )


# Re-export HTTPException for convenience
from fastapi import HTTPException  # noqa: E402
