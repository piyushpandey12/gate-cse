"""Initial schema - create all authoritative tables

Revision ID: 001_initial
Revises:
Create Date: 2026-09-21
"""
from alembic import op
import sqlalchemy as sa

revision = "001_initial"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # Users
    op.create_table(
        "users",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("email", sa.String(255), unique=True, index=True, nullable=False),
        sa.Column("name", sa.String(128), nullable=False),
        sa.Column("hashed_password", sa.String(255), nullable=False),
        sa.Column("role", sa.String(32), nullable=False, server_default="STUDENT"),
        sa.Column("is_active", sa.Boolean, server_default=sa.text("true")),
        sa.Column("is_premium", sa.Boolean, server_default=sa.text("false")),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
    )

    # User Profiles
    op.create_table(
        "user_profiles",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("user_id", sa.String(64), sa.ForeignKey("users.id"), unique=True, nullable=False),
        sa.Column("target_exam", sa.String(64), server_default="GATE CSE"),
        sa.Column("target_year", sa.Integer, server_default=sa.text("2027")),
        sa.Column("target_score", sa.Float, server_default=sa.text("78.0")),
        sa.Column("target_rank", sa.Integer, server_default=sa.text("150")),
        sa.Column("current_streak", sa.Integer, server_default=sa.text("1")),
        sa.Column("longest_streak", sa.Integer, server_default=sa.text("1")),
        sa.Column("total_questions_solved", sa.Integer, server_default=sa.text("0")),
        sa.Column("overall_accuracy", sa.Float, server_default=sa.text("0.0")),
        sa.Column("updated_at", sa.DateTime, server_default=sa.func.now(), onupdate=sa.func.now()),
    )

    # Subjects
    op.create_table(
        "subjects",
        sa.Column("id", sa.String(32), primary_key=True),
        sa.Column("code", sa.String(32), unique=True, nullable=False),
        sa.Column("name", sa.String(128), nullable=False),
        sa.Column("weightage_percent", sa.Float, server_default=sa.text("8.0")),
        sa.Column("display_order", sa.Integer, server_default=sa.text("0")),
        sa.Column("color_hex", sa.String(16), server_default="#06B6D4"),
        sa.Column("icon_name", sa.String(64), server_default="MenuBook"),
        sa.Column("total_pyq_count", sa.Integer, server_default=sa.text("0")),
    )

    # Topics
    op.create_table(
        "topics",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=False),
        sa.Column("name", sa.String(128), nullable=False),
        sa.Column("difficulty", sa.String(16), server_default="MEDIUM"),
        sa.Column("pyq_count", sa.Integer, server_default=sa.text("0")),
        sa.Column("frequency_score", sa.Float, server_default=sa.text("1.0")),
        sa.Column("display_order", sa.Integer, server_default=sa.text("0")),
        sa.Column("summary", sa.Text, nullable=True),
    )
    op.create_index("ix_topics_subject_id", "topics", ["subject_id"])

    # Questions
    op.create_table(
        "questions",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("topic_id", sa.String(64), sa.ForeignKey("topics.id"), nullable=False),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=False),
        sa.Column("year", sa.Integer, nullable=True),
        sa.Column("set_session", sa.String(16), nullable=True),
        sa.Column("question_type", sa.String(16), nullable=False, server_default="MCQ"),
        sa.Column("marks", sa.Integer, server_default=sa.text("1")),
        sa.Column("negative_marks", sa.Float, server_default=sa.text("0.33")),
        sa.Column("difficulty", sa.String(16), server_default="MEDIUM"),
        sa.Column("problem_statement", sa.Text, nullable=False),
        sa.Column("options", sa.JSON, nullable=True),
        sa.Column("correct_answers", sa.String(64), nullable=False),
        sa.Column("tolerance_lower", sa.Float, nullable=True),
        sa.Column("tolerance_upper", sa.Float, nullable=True),
        sa.Column("detailed_solution", sa.Text, nullable=False),
        sa.Column("key_formula", sa.Text, nullable=True),
        sa.Column("shortcut_trick", sa.Text, nullable=True),
        sa.Column("common_trap", sa.Text, nullable=True),
        sa.Column("total_attempts", sa.Integer, server_default=sa.text("0")),
        sa.Column("correct_attempts", sa.Integer, server_default=sa.text("0")),
    )
    op.create_index("ix_questions_topic_id", "questions", ["topic_id"])
    op.create_index("ix_questions_subject_id", "questions", ["subject_id"])
    op.create_index("ix_questions_year", "questions", ["year"])

    # Attempts
    op.create_table(
        "attempts",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("user_id", sa.String(64), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("question_id", sa.String(64), sa.ForeignKey("questions.id"), nullable=False),
        sa.Column("user_answer", sa.String(128), nullable=False),
        sa.Column("is_correct", sa.Boolean, nullable=False),
        sa.Column("marks_obtained", sa.Float, nullable=False),
        sa.Column("time_taken_seconds", sa.Integer, server_default=sa.text("60")),
        sa.Column("attempted_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("mistake_category", sa.String(32), nullable=True),
        sa.Column("user_notes", sa.Text, nullable=True),
        sa.Column("is_bookmarked", sa.Boolean, server_default=sa.text("false")),
    )
    op.create_index("ix_attempts_user_id", "attempts", ["user_id"])
    op.create_index("ix_attempts_question_id", "attempts", ["question_id"])
    op.create_index("ix_attempts_user_question", "attempts", ["user_id", "question_id"])

    # Spaced Repetition Records
    op.create_table(
        "spaced_repetition_records",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("user_id", sa.String(64), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("item_type", sa.String(32), server_default="QUESTION"),
        sa.Column("item_id", sa.String(64), nullable=False),
        sa.Column("repetitions", sa.Integer, server_default=sa.text("0")),
        sa.Column("interval_days", sa.Integer, server_default=sa.text("1")),
        sa.Column("ease_factor", sa.Float, server_default=sa.text("2.5")),
        sa.Column("last_reviewed_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("next_review_due", sa.DateTime, server_default=sa.func.now()),
        sa.Column("is_mastered", sa.Boolean, server_default=sa.text("false")),
    )
    op.create_index("ix_srr_user_id", "spaced_repetition_records", ["user_id"])
    op.create_index("ix_srr_next_review", "spaced_repetition_records", ["next_review_due"])

    # Flashcards
    op.create_table(
        "flashcards",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=False),
        sa.Column("topic_id", sa.String(64), sa.ForeignKey("topics.id"), nullable=False),
        sa.Column("front_prompt", sa.String(512), nullable=False),
        sa.Column("back_explanation", sa.String(1024), nullable=False),
        sa.Column("latex_formula", sa.String(512), nullable=True),
        sa.Column("key_tag", sa.String(64), nullable=True),
    )

    # Formulas
    op.create_table(
        "formulas",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=False),
        sa.Column("name", sa.String(128), nullable=False),
        sa.Column("formula_latex", sa.String(512), nullable=False),
        sa.Column("description", sa.String(512), nullable=False),
        sa.Column("applications", sa.String(256), nullable=True),
        sa.Column("is_favorite", sa.Boolean, server_default=sa.text("false")),
    )

    # Study Plan Entries
    op.create_table(
        "study_plan_entries",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("user_id", sa.String(64), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("day_date", sa.String(16), nullable=False),
        sa.Column("title", sa.String(128), nullable=False),
        sa.Column("planned_minutes", sa.Integer, server_default=sa.text("60")),
        sa.Column("completed_minutes", sa.Integer, server_default=sa.text("0")),
        sa.Column("is_completed", sa.Boolean, server_default=sa.text("false")),
    )
    op.create_index("ix_spe_user_date", "study_plan_entries", ["user_id", "day_date"])

    # Resources
    op.create_table(
        "resources",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=False),
        sa.Column("topic_id", sa.String(64), sa.ForeignKey("topics.id"), nullable=True),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("provider", sa.String(128), nullable=False),
        sa.Column("resource_type", sa.String(32), nullable=False, server_default="PLAYLIST"),
        sa.Column("description", sa.Text, nullable=False),
        sa.Column("url", sa.String(512), nullable=False),
        sa.Column("recommended_chapters", sa.String(256), nullable=True),
        sa.Column("is_free", sa.Boolean, server_default=sa.text("true")),
        sa.Column("is_bookmarked", sa.Boolean, server_default=sa.text("false")),
    )
    op.create_index("ix_resources_subject_id", "resources", ["subject_id"])

    # Mock Tests
    op.create_table(
        "mock_tests",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("title", sa.String(128), nullable=False),
        sa.Column("test_type", sa.String(32), server_default="FULL_MOCK"),
        sa.Column("duration_minutes", sa.Integer, server_default=sa.text("180")),
        sa.Column("total_marks", sa.Float, server_default=sa.text("100.0")),
        sa.Column("total_questions", sa.Integer, server_default=sa.text("65")),
        sa.Column("subject_id", sa.String(32), sa.ForeignKey("subjects.id"), nullable=True),
        sa.Column("is_live", sa.Boolean, server_default=sa.text("true")),
    )

    # Test Submissions
    op.create_table(
        "test_submissions",
        sa.Column("id", sa.String(64), primary_key=True),
        sa.Column("user_id", sa.String(64), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("test_id", sa.String(64), sa.ForeignKey("mock_tests.id"), nullable=False),
        sa.Column("score_obtained", sa.Float, server_default=sa.text("0.0")),
        sa.Column("total_marks", sa.Float, server_default=sa.text("100.0")),
        sa.Column("rank", sa.Integer, nullable=True),
        sa.Column("percentile", sa.Float, nullable=True),
        sa.Column("time_taken_seconds", sa.Integer, server_default=sa.text("0")),
        sa.Column("submitted_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("answer_sheet", sa.JSON, nullable=True),
    )
    op.create_index("ix_ts_user_id", "test_submissions", ["user_id"])


def downgrade() -> None:
    op.drop_table("test_submissions")
    op.drop_table("mock_tests")
    op.drop_table("resources")
    op.drop_table("study_plan_entries")
    op.drop_table("formulas")
    op.drop_table("flashcards")
    op.drop_table("spaced_repetition_records")
    op.drop_table("attempts")
    op.drop_table("questions")
    op.drop_table("topics")
    op.drop_table("subjects")
    op.drop_table("user_profiles")
    op.drop_table("users")
