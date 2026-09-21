package com.example.service

import com.example.data.model.AttemptEntity
import com.example.data.model.Difficulty
import com.example.data.model.MistakeCategory
import com.example.data.model.RevisionItemEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Result data class from SpacedRepetitionService scheduling calculation.
 */
data class SrsScheduleResult(
    val nextReviewDueDate: String,
    val intervalDays: Int,
    val repetitionCount: Int,
    val easeFactor: Float,
    val isMastered: Boolean,
    val qualityRating: Int
)

/**
 * Summary flags indicating whether a topic requires immediate intervention.
 */
data class TopicAttentionAssessment(
    val topicId: String,
    val needsImmediateAttention: Boolean,
    val reason: String?,
    val urgencyScore: Float // 0.0 to 1.0 (higher = more urgent)
)

/**
 * High-precision Spaced Repetition Scheduling (SRS) Service for GATE CSE PYQs and Topics.
 *
 * Employs a modified SuperMemo SM-2 algorithm tailored for technical competitive examinations:
 * - Maps user accuracy, time spent relative to question complexity, and mistake categories
 *   to a standard SM-2 response quality rating (0-5).
 * - Dynamically computes the next review date based on performance metrics.
 * - Computes Topic Attention Assessment flags to immediately alert candidates to recurring
 *   weaknesses, concept gaps, or severe negative marking traps.
 */
class SpacedRepetitionService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Maps an attempt on a question to an SM-2 Quality Score (0 to 5):
     * 5 - Perfect response without hesitation (fast, correct)
     * 4 - Correct response after normal deliberation
     * 3 - Correct response with significant difficulty or overtime
     * 2 - Incorrect response with familiar answer (silly/calculation mistake)
     * 1 - Incorrect response with conceptual difficulty or formula gap
     * 0 - Complete blackout / total misconception
     */
    fun calculateQualityRating(
        attempt: AttemptEntity,
        expectedSeconds: Int = 180
    ): Int {
        val timeSpent = attempt.timeSpentSeconds
        val mistake = attempt.mistakeCategory

        return if (attempt.isCorrect) {
            when {
                // Correct and solved well under expected benchmark
                timeSpent <= expectedSeconds * 0.75 -> 5
                // Correct within normal benchmark time
                timeSpent <= expectedSeconds * 1.5 -> 4
                // Correct but took excessive time / struggled
                else -> 3
            }
        } else {
            // Incorrect response
            when (mistake) {
                // Silly arithmetic, time pressure, or slip - core concept is largely intact
                MistakeCategory.CALCULATION,
                MistakeCategory.SILLY_MISTAKE,
                MistakeCategory.TIME_PRESSURE -> 2

                // Formula forgotten or memory lapse
                MistakeCategory.FORMULA,
                MistakeCategory.MEMORY -> 1

                // Severe conceptual gap, question misread, guessing, other
                MistakeCategory.CONCEPTUAL,
                MistakeCategory.MISREAD,
                MistakeCategory.GUESS,
                MistakeCategory.OTHER,
                null -> 0
            }
        }
    }

    /**
     * Calculates the next review schedule for a PYQ / Revision item using SM-2 rules.
     *
     * @param existingItem The existing revision entity (if previously practiced), or null if first attempt.
     * @param attempt The current attempt entity containing answer, correctness, time spent, and mistake classification.
     * @param questionDifficulty The question difficulty level.
     * @param customExpectedSeconds Benchmark time for the question (default 180s = 3 mins typical for 2-mark GATE questions).
     */
    fun calculateNextSchedule(
        existingItem: RevisionItemEntity?,
        attempt: AttemptEntity,
        questionDifficulty: Difficulty = Difficulty.MEDIUM,
        customExpectedSeconds: Int? = null
    ): SrsScheduleResult {
        val benchmarkTime = customExpectedSeconds ?: when (questionDifficulty) {
            Difficulty.EASY -> 90
            Difficulty.MEDIUM -> 180
            Difficulty.HARD -> 240
        }

        val quality = calculateQualityRating(attempt, benchmarkTime)

        var easeFactor = existingItem?.easeFactor ?: 2.5f
        var repetitionCount = existingItem?.repetitionCount ?: 0
        var intervalDays = existingItem?.intervalDays ?: 0

        // SM-2 Ease Factor calculation:
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val delta = 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)
        easeFactor = max(1.3f, easeFactor + delta)

        if (quality >= 3) {
            // Successful recall / mastery progression
            when (repetitionCount) {
                0 -> intervalDays = 1
                1 -> intervalDays = 3
                2 -> intervalDays = 7
                else -> {
                    intervalDays = (intervalDays * easeFactor).roundToInt()
                    // Cap interval to a reasonable exam prep horizon (e.g. 60 days)
                    if (intervalDays > 60) intervalDays = 60
                }
            }
            repetitionCount += 1
        } else {
            // Failed recall or severe slip: reset repetition streak, schedule for immediate review
            repetitionCount = 0
            intervalDays = if (quality == 2) 2 else 1
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
        val nextDueDate = dateFormat.format(calendar.time)

        // Mark mastered if high repetitions with healthy ease factor and long interval
        val isMastered = repetitionCount >= 4 && easeFactor >= 2.2f && intervalDays >= 14

        return SrsScheduleResult(
            nextReviewDueDate = nextDueDate,
            intervalDays = intervalDays,
            repetitionCount = repetitionCount,
            easeFactor = easeFactor,
            isMastered = isMastered,
            qualityRating = quality
        )
    }

    /**
     * Determines whether a specific topic requires immediate attention based on:
     * - Attempt accuracy (< 50%)
     * - Recurring conceptual gaps or formula errors
     * - Low question volume (< 3 solved)
     * - High frequency of negative marking
     */
    fun assessTopicAttention(
        topicId: String,
        attempts: List<AttemptEntity>,
        currentMasteryScore: Float = 0f
    ): TopicAttentionAssessment {
        if (attempts.isEmpty()) {
            return TopicAttentionAssessment(
                topicId = topicId,
                needsImmediateAttention = true,
                reason = "Unattempted topic: No PYQs solved yet",
                urgencyScore = 0.65f
            )
        }

        val total = attempts.size
        val correct = attempts.count { it.isCorrect }
        val accuracy = correct.toFloat() / total

        val conceptualMistakes = attempts.count {
            it.mistakeCategory == MistakeCategory.CONCEPTUAL ||
            it.mistakeCategory == MistakeCategory.FORMULA ||
            it.mistakeCategory == MistakeCategory.MEMORY
        }

        val calculationMistakes = attempts.count {
            it.mistakeCategory == MistakeCategory.CALCULATION ||
            it.mistakeCategory == MistakeCategory.SILLY_MISTAKE
        }

        val recentAttempts = attempts.takeLast(5)
        val recentIncorrect = recentAttempts.count { !it.isCorrect }

        return when {
            accuracy < 0.40f && total >= 2 -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = true,
                    reason = "Critical: Low accuracy (${(accuracy * 100).toInt()}%) across $total attempts",
                    urgencyScore = 0.95f
                )
            }
            conceptualMistakes >= 2 -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = true,
                    reason = "Recurring concept & formula gaps ($conceptualMistakes flagged)",
                    urgencyScore = 0.85f
                )
            }
            recentIncorrect >= 3 -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = true,
                    reason = "Recent slump: $recentIncorrect of last ${recentAttempts.size} attempts missed",
                    urgencyScore = 0.80f
                )
            }
            accuracy < 0.60f && currentMasteryScore < 50f -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = true,
                    reason = "Below target threshold: Mastery score ${currentMasteryScore.toInt()}%",
                    urgencyScore = 0.70f
                )
            }
            calculationMistakes >= 3 -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = false,
                    reason = "Careless calculation errors ($calculationMistakes slips); revise NAT steps",
                    urgencyScore = 0.40f
                )
            }
            else -> {
                TopicAttentionAssessment(
                    topicId = topicId,
                    needsImmediateAttention = false,
                    reason = "Healthy retention (${(accuracy * 100).toInt()}% accuracy)",
                    urgencyScore = max(0f, 1f - accuracy)
                )
            }
        }
    }
}
