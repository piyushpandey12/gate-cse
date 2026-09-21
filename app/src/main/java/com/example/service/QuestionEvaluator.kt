package com.example.service

import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionType
import kotlin.math.abs

data class QuestionEvaluationResult(
    val isCorrect: Boolean,
    val marksAwarded: Float,
    val negativeMarksApplied: Float,
    val normalizedUserAnswer: String,
    val expectedAnswer: String
)

object QuestionEvaluator {

    fun evaluate(question: QuestionEntity, rawUserAnswer: String): QuestionEvaluationResult {
        val trimmedAnswer = rawUserAnswer.trim()
        val expected = question.correctAnswers.trim()

        val isCorrect = when (question.questionType) {
            QuestionType.MCQ -> {
                trimmedAnswer.equals(expected, ignoreCase = true)
            }
            QuestionType.MSQ -> {
                val userSet = trimmedAnswer
                    .split(",", ";", " ")
                    .map { it.trim().uppercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                val expectedSet = expected
                    .split(",", ";", " ")
                    .map { it.trim().uppercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                userSet.isNotEmpty() && userSet == expectedSet
            }
            QuestionType.NAT -> {
                evaluateNatAnswer(trimmedAnswer, expected)
            }
        }

        // Calculate marks according to official GATE marking scheme:
        // MCQ: Negative marks apply (-1/3 of question marks)
        // MSQ: No negative marks (0.0)
        // NAT: No negative marks (0.0)
        val negativeMarks = if (!isCorrect) {
            when (question.questionType) {
                QuestionType.MCQ -> abs(question.negativeMarks)
                QuestionType.MSQ, QuestionType.NAT -> 0.0f
            }
        } else {
            0.0f
        }

        val marksAwarded = if (isCorrect) {
            question.marks
        } else {
            -negativeMarks
        }

        return QuestionEvaluationResult(
            isCorrect = isCorrect,
            marksAwarded = marksAwarded,
            negativeMarksApplied = negativeMarks,
            normalizedUserAnswer = trimmedAnswer,
            expectedAnswer = expected
        )
    }

    private fun evaluateNatAnswer(userAnswer: String, expectedAnswer: String): Boolean {
        val userVal = userAnswer.replace(",", "").toDoubleOrNull() ?: return false

        // Check if expected is a range (e.g., "14:15", "14.0-15.0", "14 to 15")
        val rangeDelimiters = listOf(":", " to ", " - ")
        for (delimiter in rangeDelimiters) {
            if (expectedAnswer.contains(delimiter, ignoreCase = true)) {
                val parts = expectedAnswer.split(Regex(Regex.escape(delimiter), RegexOption.IGNORE_CASE))
                if (parts.size == 2) {
                    val minVal = parts[0].trim().replace(",", "").toDoubleOrNull()
                    val maxVal = parts[1].trim().replace(",", "").toDoubleOrNull()
                    if (minVal != null && maxVal != null) {
                        val lower = kotlin.math.min(minVal, maxVal) - 1e-4
                        val upper = kotlin.math.max(minVal, maxVal) + 1e-4
                        return userVal in lower..upper
                    }
                }
            }
        }

        // Single numeric target (with 0.01 tolerance for precision differences)
        val targetVal = expectedAnswer.replace(",", "").toDoubleOrNull()
        return if (targetVal != null) {
            abs(userVal - targetVal) < 0.015
        } else {
            userAnswer.equals(expectedAnswer, ignoreCase = true)
        }
    }
}
