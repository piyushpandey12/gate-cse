package com.example.network

import com.example.network.dto.*
import com.example.network.mapper.DtoMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDataSource(private val apiClient: ApiClient) {

    private val api get() = apiClient.api

    // Auth
    suspend fun login(email: String, password: String): TokenResponse = withContext(Dispatchers.IO) {
        api.login(AuthLoginRequest(email, password))
    }

    suspend fun register(email: String, password: String, name: String, targetYear: Int = 2027): TokenResponse = withContext(Dispatchers.IO) {
        api.register(AuthRegisterRequest(email, password, name, targetYear))
    }

    suspend fun getProfile(): UserProfileDTO = withContext(Dispatchers.IO) {
        api.getProfile()
    }

    // Syllabus
    suspend fun getSubjects(): List<SubjectEntity> = withContext(Dispatchers.IO) {
        api.getSubjects().map { it.toEntity() }
    }

    suspend fun getTopicsForSubject(subjectId: String): List<TopicEntity> = withContext(Dispatchers.IO) {
        api.getTopicsForSubject(subjectId).map { it.toEntity() }
    }

    suspend fun getQuestions(
        subjectId: String? = null,
        topicId: String? = null,
        year: Int? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): PaginatedQuestionsResponse = withContext(Dispatchers.IO) {
        api.getQuestions(subjectId, topicId, year, page = page, pageSize = pageSize)
    }

    suspend fun getQuestion(questionId: String): QuestionDto = withContext(Dispatchers.IO) {
        api.getQuestion(questionId)
    }

    // Practice
    suspend fun submitAnswer(
        questionId: String,
        userAnswer: String,
        timeTakenSeconds: Int,
        mistakeCategory: String? = null
    ): EvaluationResultDto = withContext(Dispatchers.IO) {
        api.submitAnswer(SubmitAnswerRequest(questionId, userAnswer, timeTakenSeconds, mistakeCategory))
    }

    suspend fun getPracticeQuestions(
        subjectId: String? = null,
        topicId: String? = null,
        questionCount: Int = 10
    ): List<PracticeQuestionDto> = withContext(Dispatchers.IO) {
        api.getPracticeQuestions(subjectId, topicId, questionCount)
    }

    // Dashboard
    suspend fun getNextAction(): DashboardNextActionResponse = withContext(Dispatchers.IO) {
        api.getNextAction()
    }

    // Resources
    suspend fun getResources(subjectId: String? = null): List<ResourceEntity> = withContext(Dispatchers.IO) {
        api.getResources(subjectId).map { it.toEntity() }
    }

    // Revision
    suspend fun submitSpacedReview(itemId: String, itemType: String, rating: Int): SpacedItemDto = withContext(Dispatchers.IO) {
        api.submitSpacedReview(mapOf("item_id" to itemId, "item_type" to itemType, "rating" to rating))
    }

    suspend fun getFlashcards(subjectId: String? = null): List<FlashcardEntity> = withContext(Dispatchers.IO) {
        api.getFlashcards(subjectId).map { it.toEntity() }
    }

    suspend fun getFormulas(subjectId: String? = null): List<FormulaEntity> = withContext(Dispatchers.IO) {
        api.getFormulas(subjectId).map { it.toEntity() }
    }

    // Health
    suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            api.healthLive().status == "alive"
        } catch (e: Exception) {
            false
        }
    }

    private fun com.example.network.dto.SubjectDto.toEntity() = DtoMapper.run { this@toEntity.toEntity() }
    private fun com.example.network.dto.TopicDto.toEntity() = DtoMapper.run { this@toEntity.toEntity() }
    private fun com.example.network.dto.ResourceDto.toEntity() = DtoMapper.run { this@toEntity.toEntity() }
    private fun com.example.network.dto.FlashcardDto.toEntity() = DtoMapper.run { this@toEntity.toEntity() }
    private fun com.example.network.dto.FormulaDto.toEntity() = DtoMapper.run { this@toEntity.toEntity() }
}
