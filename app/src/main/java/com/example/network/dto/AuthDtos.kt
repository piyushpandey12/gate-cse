package com.example.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthRegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val target_year: Int = 2027
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    val access_token: String,
    val token_type: String = "bearer",
    val user_id: String,
    val email: String,
    val name: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class UserProfileDTO(
    val user_id: String,
    val name: String,
    val email: String,
    val role: String,
    val target_exam: String,
    val target_year: Int,
    val target_score: Float,
    val current_streak: Int,
    val total_questions_solved: Int,
    val overall_accuracy: Float
)
