package com.example.network.api

import com.example.network.dto.*
import retrofit2.http.*

interface GateApi {

    // Auth
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): TokenResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthLoginRequest): TokenResponse

    @GET("api/v1/auth/me")
    suspend fun getProfile(): UserProfileDTO

    // Syllabus
    @GET("api/v1/syllabus/subjects")
    suspend fun getSubjects(): List<SubjectDto>

    @GET("api/v1/syllabus/subjects/{subjectId}/topics")
    suspend fun getTopicsForSubject(@Path("subjectId") subjectId: String): List<TopicDto>

    @GET("api/v1/syllabus/questions")
    suspend fun getQuestions(
        @Query("subject_id") subjectId: String? = null,
        @Query("topic_id") topicId: String? = null,
        @Query("year") year: Int? = null,
        @Query("question_type") questionType: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): PaginatedQuestionsResponse

    @GET("api/v1/syllabus/questions/{questionId}")
    suspend fun getQuestion(@Path("questionId") questionId: String): QuestionDto

    // Practice
    @POST("api/v1/practice/submit")
    suspend fun submitAnswer(@Body request: SubmitAnswerRequest): EvaluationResultDto

    @GET("api/v1/practice/questions")
    suspend fun getPracticeQuestions(
        @Query("subject_id") subjectId: String? = null,
        @Query("topic_id") topicId: String? = null,
        @Query("question_count") questionCount: Int = 10
    ): List<PracticeQuestionDto>

    // Dashboard
    @GET("api/v1/dashboard/next-action")
    suspend fun getNextAction(): DashboardNextActionResponse

    // Resources
    @GET("api/v1/resources")
    suspend fun getResources(
        @Query("subject_id") subjectId: String? = null,
        @Query("resource_type") resourceType: String? = null
    ): List<ResourceDto>

    // Revision
    @POST("api/v1/revision/review")
    suspend fun submitSpacedReview(@Body request: Map<String, Any>): SpacedItemDto

    @GET("api/v1/revision/flashcards")
    suspend fun getFlashcards(
        @Query("subject_id") subjectId: String? = null
    ): List<FlashcardDto>

    @GET("api/v1/revision/formulas")
    suspend fun getFormulas(
        @Query("subject_id") subjectId: String? = null
    ): List<FormulaDto>

    // Health
    @GET("health/live")
    suspend fun healthLive(): HealthResponse

    @GET("health/ready")
    suspend fun healthReady(): HealthResponse
}
