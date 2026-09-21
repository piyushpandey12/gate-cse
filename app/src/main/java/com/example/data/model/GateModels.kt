package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// -------------------------------------------------------------
// Syllabus Hierarchy: Subject -> Chapter -> Topic -> Subtopic
// -------------------------------------------------------------

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String, // e.g. "OS", "DBMS", "CN", "TOC", "CD", "ALGO", "DS", "C", "COA", "DL", "EM", "DM", "GA"
    val description: String,
    val iconName: String,
    val weightagePercentage: Float, // Estimated GATE weightage e.g. 10.0%
    val orderIndex: Int
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val name: String,
    val chapter: String,
    val description: String,
    val totalQuestions: Int = 0,
    val pyqCount: Int = 0,
    val highYield: Boolean = false,
    val orderIndex: Int
)

// -------------------------------------------------------------
// Questions: MCQ, MSQ, NAT with full solutions & metadata
// -------------------------------------------------------------

enum class QuestionType { MCQ, MSQ, NAT }
enum class Difficulty { EASY, MEDIUM, HARD }

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId"), Index("year"), Index("subjectId")]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String,
    val year: Int?, // null if custom practice, 1987-2026 for PYQs
    val setSession: String?, // "Set 1", "Set 2", etc.
    val questionNumber: Int?,
    val questionType: QuestionType,
    val marks: Float, // 1.0 or 2.0
    val negativeMarks: Float, // -0.33, -0.66, 0.0
    val difficulty: Difficulty,
    val questionText: String,
    val optionA: String? = null,
    val optionB: String? = null,
    val optionC: String? = null,
    val optionD: String? = null,
    val correctAnswers: String, // "A", "A,C" for MSQ, "14" or "14.0:15.0" for NAT range
    val shortExplanation: String,
    val detailedSolution: String,
    val shortcutTrick: String? = null,
    val commonTrap: String? = null,
    val keyFormula: String? = null,
    val isPyq: Boolean = true,
    val isBookmarked: Boolean = false
)

// -------------------------------------------------------------
// Question Attempts & Mistakes
// -------------------------------------------------------------

enum class MistakeCategory {
    CONCEPTUAL,
    CALCULATION,
    SILLY_MISTAKE,
    TIME_PRESSURE,
    MISREAD,
    FORMULA,
    MEMORY,
    GUESS,
    OTHER
}

@Entity(
    tableName = "attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId")]
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val marksAwarded: Float,
    val timeSpentSeconds: Int,
    val mistakeCategory: MistakeCategory?,
    val userNote: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// User Topic Mastery
// -------------------------------------------------------------

enum class MasteryLevel {
    NEW,
    LEARNING,
    WEAK,
    IMPROVING,
    STRONG,
    MASTERED
}

@Entity(
    tableName = "topic_mastery",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId", unique = true)]
)
data class TopicMasteryEntity(
    @PrimaryKey val topicId: String,
    val attemptsCount: Int = 0,
    val correctCount: Int = 0,
    val accuracy: Float = 0f,
    val averageTimeSeconds: Int = 0,
    val masteryScore: Float = 0f, // 0..100
    val level: MasteryLevel = MasteryLevel.NEW,
    val lastAttemptedAt: Long? = null,
    val needsRevision: Boolean = false,
    val needsImmediateAttention: Boolean = false,
    val attentionReason: String? = null
)

// -------------------------------------------------------------
// Curated Resources (From user PDF: Playlists, NPTEL, Books, Notes)
// -------------------------------------------------------------

enum class ResourceType {
    PLAYLIST,
    NPTEL,
    BOOK,
    NOTE,
    REVISION_VIDEO,
    PRACTICE_SET
}

@Entity(
    tableName = "resources",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class ResourceEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String? = null,
    val title: String,
    val provider: String, // "GateWallah", "Unacademy", "NPTEL (IIT Madras)", "Peter Linz", "Silberschatz", etc.
    val resourceType: ResourceType,
    val description: String,
    val url: String,
    val recommendedChapters: String? = null,
    val isFree: Boolean = true,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val personalNotes: String? = null
)

// -------------------------------------------------------------
// Spaced Repetition Revision
// -------------------------------------------------------------

@Entity(
    tableName = "revision_items",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId", unique = true)]
)
data class RevisionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val topicId: String,
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val nextReviewDueDate: Long, // timestamp
    val easeFactor: Float = 2.5f,
    val lastQualityRating: Int = 0,
    val isOverdue: Boolean = false,
    val isMastered: Boolean = false,
    val lastReviewedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// Flashcards for Formulas, Complexities, Definitions
// -------------------------------------------------------------

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String,
    val front: String, // Question / Definition prompt
    val back: String,  // Answer / Formula / Explanation
    val keyConcept: String,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val nextReviewDate: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// Formula Sheets
// -------------------------------------------------------------

@Entity(tableName = "formulas")
data class FormulaEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String,
    val name: String,
    val formulaLatex: String,
    val description: String,
    val applications: String,
    val isFavorite: Boolean = false
)

// -------------------------------------------------------------
// Study Notes (Markdown & LaTeX support)
// -------------------------------------------------------------

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String?,
    val title: String,
    val content: String,
    val tags: String, // Comma-separated
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// Study Planner & Tasks
// -------------------------------------------------------------

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectId: String,
    val topicId: String?,
    val allocatedMinutes: Int,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val targetDate: String // "2026-09-21"
)

// -------------------------------------------------------------
// Study Sessions & Focus Timer
// -------------------------------------------------------------

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val topicName: String,
    val durationMinutes: Int,
    val sessionType: String, // "FOCUS", "POMODORO", "PRACTICE"
    val timestamp: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// Tests and Exam Simulator
// -------------------------------------------------------------

@Entity(tableName = "test_sessions")
data class TestSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val testType: String, // "FULL_MOCK", "SECTIONAL", "TOPIC_TEST"
    val totalQuestions: Int,
    val durationMinutes: Int,
    val questionIdsJson: String, // JSON list of question IDs
    val userAnswersJson: String = "{}", // Map of questionId -> selected answer
    val markedForReviewJson: String = "[]", // List of question IDs
    val startedAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = null,
    val totalScore: Float = 0f,
    val accuracy: Float = 0f,
    val isCompleted: Boolean = false
)

// -------------------------------------------------------------
// User Profile & Preferences
// -------------------------------------------------------------

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val name: String = "Aspirant",
    val targetExam: String = "GATE CSE",
    val targetYear: Int = 2027,
    val dailyTargetHours: Float = 3.5f,
    val dailyQuestionsTarget: Int = 15,
    val weeklyTestsTarget: Int = 2,
    val preferredStudyTime: String = "Morning (06:00 - 09:30)",
    val role: String = "STUDENT", // "STUDENT" or "ADMIN"
    val currentStreak: Int = 1,
    val totalMinutesStudied: Int = 0,
    val totalQuestionsSolved: Int = 0,
    val totalCorrect: Int = 0,
    val preparationLevel: String = "Intermediate",
    val weakSubjectIds: String = "EM,COA"
)

// -------------------------------------------------------------
// Official Exam Configuration & Schedule Events
// -------------------------------------------------------------

@Entity(tableName = "exam_config")
data class ExamConfigEntity(
    @PrimaryKey val id: String = "gate_cse_2027",
    val examName: String = "GATE CSE 2027",
    val conductingInstitute: String = "IIT",
    val officialWebsiteUrl: String = "https://gate2027.iit.ac.in",
    val lastVerifiedDate: String = "September 2026"
)

@Entity(tableName = "exam_events")
data class ExamEventEntity(
    @PrimaryKey val id: String,
    val examConfigId: String = "gate_cse_2027",
    val eventName: String,
    val eventDateMillis: Long,
    val eventDateFormatted: String,
    val isMainExamDate: Boolean = false,
    val sourceUrl: String = "https://gate2027.iit.ac.in"
)
