
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Any

app = FastAPI(
    title="AI Software Requirement Validator",
    description="API for validating software requirements",
    version="1.0.0"
)


class AnalyzeRequest(BaseModel):
    requirements: Any = None
    text: str | None = None


@app.get("/")
def home():
    return {
        "message": "AI Requirement Validator service is running"
    }


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze")
def analyze(request: AnalyzeRequest):
    requirement_text = request.text

    if not requirement_text and request.requirements is not None:
        requirement_text = str(request.requirements)

    if not requirement_text or not requirement_text.strip():
        raise HTTPException(
            status_code=400,
            detail="Please provide requirement text"
        )

    return {
        "status": "received",
        "message": "Requirements received successfully",
        "requirements": requirement_text
    }
