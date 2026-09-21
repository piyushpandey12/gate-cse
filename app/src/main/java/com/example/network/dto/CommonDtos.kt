package com.example.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val request_id: String?
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val success: Boolean = false,
    val error: ApiErrorDetail?,
    val request_id: String?
)

@JsonClass(generateAdapter = true)
data class ApiErrorDetail(
    val code: String,
    val message: String,
    val details: Any?
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String,
    val service: String,
    val version: String
)
