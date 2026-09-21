package com.example.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubmitAnswerRequest(
    val question_id: String,
    val user_answer: String,
    val time_taken_seconds: Int = 45,
    val mistake_category: String? = null,
    val user_notes: String? = null
)

@JsonClass(generateAdapter = true)
data class EvaluationResultDto(
    val is_correct: Boolean,
    val marks_obtained: Float,
    val correct_answer: String,
    val detailed_solution: String,
    val key_formula: String?,
    val shortcut_trick: String?,
    val common_trap: String?,
    val logged_to_error_notebook: Boolean = false,
    val next_review_days: Int = 1
)

@JsonClass(generateAdapter = true)
data class PracticeQuestionDto(
    val id: String,
    val subject_id: String,
    val topic_id: String,
    val year: Int?,
    val question_type: String,
    val marks: Int,
    val negative_marks: Float,
    val difficulty: String,
    val problem_statement: String,
    val options: List<QuestionOptionDto>?
)
