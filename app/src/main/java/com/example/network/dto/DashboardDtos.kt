package com.example.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardNextActionResponse(
    val target_exam: String,
    val days_remaining: Int,
    val next_action: NextActionRecommendationDto,
    val today_plan: TodayPlanDto,
    val continue_learning: ContinueLearningDto,
    val due_revision_count: Int,
    val weak_topics_count: Int,
    val total_solved: Int,
    val overall_accuracy: Float,
    val streak_days: Int
)

@JsonClass(generateAdapter = true)
data class NextActionRecommendationDto(
    val action_type: String,
    val title: String,
    val reason: String,
    val target_subject: String,
    val target_topic_id: String,
    val estimated_minutes: Int,
    val due_items_count: Int?,
    val urgency_level: String
)

@JsonClass(generateAdapter = true)
data class TodayPlanDto(
    val planned_minutes: Int,
    val completed_minutes: Int,
    val tasks_count: Int,
    val tasks_completed: Int
)

@JsonClass(generateAdapter = true)
data class ContinueLearningDto(
    val subject_id: String,
    val subject_name: String,
    val topic_id: String,
    val topic_name: String,
    val progress_percentage: Int
)
