from datetime import datetime, timedelta, timezone
from typing import Any, Union, Optional
from jose import jwt
import hashlib
import hmac
import secrets as pysecrets
from backend.app.core.config import settings


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verifies a plain password against a salted SHA-256 hash."""
    try:
        parts = hashed_password.split("$")
        if len(parts) == 3 and parts[0] == "sha256":
            salt = parts[1]
            expected_hash = parts[2]
            computed = hashlib.sha256(
                (salt + plain_password).encode("utf-8")
            ).hexdigest()
            return hmac.compare_digest(computed, expected_hash)
        return False
    except Exception:
        return False


def get_password_hash(password: str) -> str:
    """Computes a salted SHA-256 hash."""
    salt = pysecrets.token_hex(16)
    computed = hashlib.sha256(
        (salt + password).encode("utf-8")
    ).hexdigest()
    return f"sha256${salt}${computed}"


def create_access_token(
    subject: Union[str, Any], expires_delta: Optional[timedelta] = None
) -> str:
    now = datetime.now(timezone.utc)
    if expires_delta:
        expire = now + expires_delta
    else:
        expire = now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode = {"exp": expire, "sub": str(subject), "iat": now}
    encoded_jwt = jwt.encode(
        to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM
    )
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    """Decode and validate a JWT token. Returns payload or None."""
    try:
        from jose import JWTError
        payload = jwt.decode(
            token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM]
        )
        return payload
    except JWTError:
        return None
