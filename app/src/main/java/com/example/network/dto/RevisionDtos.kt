package com.example.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpacedItemDto(
    val id: String,
    val item_id: String,
    val item_type: String,
    val repetitions: Int,
    val interval_days: Int,
    val ease_factor: Float,
    val next_review_due: String,
    val is_mastered: Boolean
)

@JsonClass(generateAdapter = true)
data class FlashcardDto(
    val id: String,
    val subject_id: String,
    val topic_id: String,
    val front_prompt: String,
    val back_explanation: String,
    val latex_formula: String?,
    val key_tag: String?
)

@JsonClass(generateAdapter = true)
data class FormulaDto(
    val id: String,
    val subject_id: String,
    val name: String,
    val formula_latex: String,
    val description: String,
    val applications: String?,
    val is_favorite: Boolean
)

@JsonClass(generateAdapter = true)
data class ResourceDto(
    val id: String,
    val subject_id: String,
    val topic_id: String?,
    val title: String,
    val provider: String,
    val resource_type: String,
    val description: String,
    val url: String,
    val recommended_chapters: String?,
    val is_free: Boolean,
    val is_bookmarked: Boolean
)
