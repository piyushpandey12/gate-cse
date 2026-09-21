package com.example.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubjectDto(
    val id: String,
    val name: String,
    val code: String,
    val weightage_percent: Float,
    val display_order: Int,
    val color_hex: String,
    val icon_name: String,
    val total_pyq_count: Int
)

@JsonClass(generateAdapter = true)
data class TopicDto(
    val id: String,
    val subject_id: String,
    val name: String,
    val difficulty: String,
    val pyq_count: Int,
    val frequency_score: Float,
    val display_order: Int,
    val summary: String?
)

@JsonClass(generateAdapter = true)
data class QuestionDto(
    val id: String,
    val subject_id: String,
    val topic_id: String,
    val year: Int?,
    val set_session: String?,
    val question_type: String,
    val marks: Int,
    val negative_marks: Float,
    val difficulty: String,
    val problem_statement: String,
    val options: List<QuestionOptionDto>?,
    val correct_answers: String?,
    val detailed_solution: String?,
    val key_formula: String?,
    val shortcut_trick: String?,
    val common_trap: String?
)

@JsonClass(generateAdapter = true)
data class QuestionOptionDto(
    val key: String,
    val text: String
)

@JsonClass(generateAdapter = true)
data class PaginatedQuestionsResponse(
    val data: List<QuestionDto>,
    val total: Int,
    val page: Int,
    val page_size: Int,
    val has_next: Boolean
)
