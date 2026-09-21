package com.example.data.repository

import com.example.data.dao.*
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.network.RemoteDataSource
import com.example.network.dto.PaginatedQuestionsResponse
import com.example.service.QuestionEvaluator
import com.example.service.SpacedRepetitionService
import com.example.service.SrsScheduleResult
import com.example.service.TopicAttentionAssessment
import com.example.sync.ConnectivityMonitor
import com.example.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GateRepository(
    private val subjectDao: SubjectDao,
    private val questionDao: QuestionDao,
    private val attemptDao: AttemptDao,
    private val masteryDao: MasteryDao,
    private val resourceDao: ResourceDao,
    private val revisionDao: RevisionDao,
    private val flashcardDao: FlashcardDao,
    private val formulaDao: FormulaDao,
    private val noteDao: NoteDao,
    private val studyDao: StudyDao,
    private val testDao: TestDao,
    private val userDao: UserDao,
    private val examDao: ExamDao,
    private val database: AppDatabase,
    val srsService: SpacedRepetitionService = SpacedRepetitionService(),
    private val remoteDataSource: RemoteDataSource? = null,
    private val connectivityMonitor: ConnectivityMonitor? = null,
    private val syncManager: SyncManager? = null
) {
    // Subjects & Topics
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>> = subjectDao.getTopicsForSubject(subjectId)
    val allTopics: Flow<List<TopicEntity>> = subjectDao.getAllTopics()
    suspend fun getTopicById(topicId: String): TopicEntity? = subjectDao.getTopicById(topicId)
    suspend fun getSubjectById(subjectId: String): SubjectEntity? = subjectDao.getSubjectById(subjectId)

    // Questions & PYQs
    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val allPyqs: Flow<List<QuestionEntity>> = questionDao.getAllPyqs()
    fun getPyqsForSubject(subjectId: String): Flow<List<QuestionEntity>> = questionDao.getPyqsForSubject(subjectId)
    fun getQuestionsForTopic(topicId: String): Flow<List<QuestionEntity>> = questionDao.getQuestionsForTopic(topicId)
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>> = questionDao.getBookmarkedQuestions()
    val availableYears: Flow<List<Int>> = questionDao.getAvailableYears()
    fun searchQuestions(query: String): Flow<List<QuestionEntity>> = questionDao.searchQuestions(query)
    suspend fun getQuestionById(id: String): QuestionEntity? = questionDao.getQuestionById(id)

    suspend fun toggleQuestionBookmark(questionId: String, current: Boolean) {
        questionDao.updateBookmark(questionId, !current)
        if (connectivityMonitor?.isOnline() == true) {
            syncManager?.queueOperation(
                operationType = "UPDATE",
                entityType = "QUESTION",
                entityId = questionId,
                payload = """{"isBookmarked": ${!current}}""",
                idempotencyKey = "bookmark_${questionId}_${System.currentTimeMillis()}"
            )
        }
    }

    fun filterQuestions(
        subjectId: String?,
        topicId: String?,
        year: Int?,
        difficulty: Difficulty?,
        qType: QuestionType?
    ): Flow<List<QuestionEntity>> = questionDao.filterQuestions(subjectId, topicId, year, difficulty, qType)

    // Submitting a question attempt & calculating adaptive learning updates
    suspend fun recordAttempt(
        question: QuestionEntity,
        selectedAnswer: String,
        timeSpentSeconds: Int,
        mistakeCategory: MistakeCategory? = null,
        note: String? = null
    ): AttemptEntity {
        val eval = QuestionEvaluator.evaluate(question, selectedAnswer)
        val isCorrect = eval.isCorrect
        val marksAwarded = eval.marksAwarded

        val resolvedMistake = if (!isCorrect) {
            mistakeCategory ?: MistakeCategory.CONCEPTUAL
        } else null

        val attempt = AttemptEntity(
            questionId = question.id,
            selectedAnswer = eval.normalizedUserAnswer,
            isCorrect = isCorrect,
            marksAwarded = marksAwarded,
            timeSpentSeconds = timeSpentSeconds,
            mistakeCategory = resolvedMistake,
            userNote = note
        )
        val attemptId = attemptDao.insertAttempt(attempt)

        // Try to submit to server if online
        if (connectivityMonitor?.isOnline() == true && remoteDataSource != null) {
            try {
                remoteDataSource.submitAnswer(
                    questionId = question.id,
                    userAnswer = eval.normalizedUserAnswer,
                    timeTakenSeconds = timeSpentSeconds,
                    mistakeCategory = resolvedMistake?.name
                )
            } catch (e: Exception) {
                // Queue for later sync if server submission fails
                syncManager?.queueOperation(
                    operationType = "SUBMIT_ANSWER",
                    entityType = "ATTEMPT",
                    entityId = question.id,
                    payload = """{"questionId":"${question.id}","answer":"${eval.normalizedUserAnswer}","time":$timeSpentSeconds}""",
                    idempotencyKey = "attempt_${question.id}_${System.currentTimeMillis()}"
                )
            }
        } else {
            // Queue for sync when online
            syncManager?.queueOperation(
                operationType = "SUBMIT_ANSWER",
                entityType = "ATTEMPT",
                entityId = question.id,
                payload = """{"questionId":"${question.id}","answer":"${eval.normalizedUserAnswer}","time":$timeSpentSeconds}""",
                idempotencyKey = "attempt_${question.id}_${System.currentTimeMillis()}"
            )
        }

        // Update User Profile Stats
        userDao.incrementSolvedCounter(if (isCorrect) 1 else 0)

        // Spaced Repetition Scheduling
        val existingRevision = revisionDao.getRevisionItemForQuestion(question.id)
        val scheduleResult = srsService.calculateNextSchedule(
            existingItem = existingRevision,
            attempt = attempt,
            questionDifficulty = question.difficulty
        )

        val nextReviewMillis = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(scheduleResult.nextReviewDueDate)?.time ?: (System.currentTimeMillis() + scheduleResult.intervalDays * 86400000L)
        } catch (e: Exception) {
            System.currentTimeMillis() + scheduleResult.intervalDays * 86400000L
        }

        if (existingRevision != null) {
            revisionDao.upsertRevisionItem(
                existingRevision.copy(
                    intervalDays = scheduleResult.intervalDays,
                    repetitionCount = scheduleResult.repetitionCount,
                    nextReviewDueDate = nextReviewMillis,
                    easeFactor = scheduleResult.easeFactor,
                    lastQualityRating = scheduleResult.qualityRating,
                    isOverdue = false,
                    isMastered = scheduleResult.isMastered,
                    lastReviewedAt = System.currentTimeMillis()
                )
            )
        } else if (!isCorrect || scheduleResult.qualityRating < 4) {
            revisionDao.upsertRevisionItem(
                RevisionItemEntity(
                    questionId = question.id,
                    topicId = question.topicId,
                    intervalDays = scheduleResult.intervalDays,
                    repetitionCount = scheduleResult.repetitionCount,
                    nextReviewDueDate = nextReviewMillis,
                    easeFactor = scheduleResult.easeFactor,
                    lastQualityRating = scheduleResult.qualityRating,
                    isOverdue = false,
                    isMastered = scheduleResult.isMastered,
                    lastReviewedAt = System.currentTimeMillis()
                )
            )
        }

        // Update Topic Mastery
        updateTopicMasteryWithAttention(question.topicId, isCorrect, timeSpentSeconds)

        return attempt.copy(id = attemptId)
    }

    private suspend fun updateTopicMasteryWithAttention(topicId: String, isCorrect: Boolean, timeSpentSeconds: Int) {
        val current = masteryDao.getMasteryForTopic(topicId) ?: TopicMasteryEntity(topicId = topicId)
        val newAttempts = current.attemptsCount + 1
        val newCorrect = current.correctCount + if (isCorrect) 1 else 0
        val accuracy = (newCorrect.toFloat() / newAttempts.toFloat()) * 100f
        val avgTime = ((current.averageTimeSeconds * current.attemptsCount) + timeSpentSeconds) / newAttempts

        val volumeScore = min(100f, (newAttempts.toFloat() / 15f) * 100f)
        val rawMastery = (accuracy * 0.65f) + (volumeScore * 0.35f)
        val level = when {
            newAttempts < 2 -> MasteryLevel.LEARNING
            rawMastery < 45f -> MasteryLevel.WEAK
            rawMastery < 70f -> MasteryLevel.IMPROVING
            rawMastery < 85f -> MasteryLevel.STRONG
            else -> MasteryLevel.MASTERED
        }
        val needsRev = rawMastery < 50f || (level == MasteryLevel.WEAK)

        val allAttemptsForTopic = attemptDao.getAttemptsForTopic(topicId)
        val attentionAssessment = srsService.assessTopicAttention(
            topicId = topicId,
            attempts = allAttemptsForTopic,
            currentMasteryScore = rawMastery
        )

        masteryDao.upsertMastery(
            current.copy(
                attemptsCount = newAttempts,
                correctCount = newCorrect,
                accuracy = accuracy,
                averageTimeSeconds = avgTime,
                masteryScore = rawMastery,
                level = level,
                lastAttemptedAt = System.currentTimeMillis(),
                needsRevision = needsRev,
                needsImmediateAttention = attentionAssessment.needsImmediateAttention,
                attentionReason = attentionAssessment.reason
            )
        )
    }

    // Attempts & Mistakes
    val recentAttempts: Flow<List<AttemptEntity>> = attemptDao.getRecentAttempts()
    val mistakeAttempts: Flow<List<AttemptEntity>> = attemptDao.getMistakeAttempts()
    suspend fun updateMistakeDetails(attemptId: Long, mistakeCategory: MistakeCategory, note: String?) {
        attemptDao.updateMistakeDetails(attemptId, mistakeCategory, note)
    }

    // Topic Mastery
    val allTopicMastery: Flow<List<TopicMasteryEntity>> = masteryDao.getAllMastery()
    val weakOrOverdueTopics: Flow<List<TopicMasteryEntity>> = masteryDao.getWeakOrOverdueTopics()
    val topicsNeedingAttention: Flow<List<TopicMasteryEntity>> = masteryDao.getTopicsNeedingImmediateAttention()

    suspend fun reevaluateTopicAttention(topicId: String): TopicAttentionAssessment {
        val current = masteryDao.getMasteryForTopic(topicId) ?: TopicMasteryEntity(topicId = topicId)
        val attempts = attemptDao.getAttemptsForTopic(topicId)
        val assessment = srsService.assessTopicAttention(
            topicId = topicId,
            attempts = attempts,
            currentMasteryScore = current.masteryScore
        )
        masteryDao.upsertMastery(
            current.copy(
                needsImmediateAttention = assessment.needsImmediateAttention,
                attentionReason = assessment.reason
            )
        )
        return assessment
    }

    // Resources
    fun getResourcesForSubject(subjectId: String): Flow<List<ResourceEntity>> = resourceDao.getResourcesForSubject(subjectId)
    val allResources: Flow<List<ResourceEntity>> = resourceDao.getAllResources()
    val bookmarkedResources: Flow<List<ResourceEntity>> = resourceDao.getBookmarkedResources()
    suspend fun toggleResourceBookmark(id: String, current: Boolean) = resourceDao.setBookmark(id, !current)
    suspend fun toggleResourceCompleted(id: String, current: Boolean) = resourceDao.setCompleted(id, !current)
    suspend fun updateResourceNotes(id: String, notes: String?) = resourceDao.updateNotes(id, notes)

    // Revision
    fun getDueRevisionItems(): Flow<List<RevisionItemEntity>> = revisionDao.getDueRevisionItems(System.currentTimeMillis())
    val allRevisionItems: Flow<List<RevisionItemEntity>> = revisionDao.getAllRevisionItems()
    suspend fun markRevisionMastered(questionId: String) = revisionDao.deleteRevisionByQuestionId(questionId)

    // Flashcards
    val allFlashcards: Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards()
    fun getFlashcardsForSubject(subjectId: String): Flow<List<FlashcardEntity>> = flashcardDao.getFlashcardsForSubject(subjectId)
    suspend fun reviewFlashcard(flashcard: FlashcardEntity, quality: Int) {
        val newInterval = when {
            quality < 2 -> 1
            flashcard.intervalDays == 1 -> 3
            flashcard.intervalDays == 3 -> 6
            else -> (flashcard.intervalDays * flashcard.easeFactor).roundToInt()
        }
        val newEase = max(1.3f, flashcard.easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)))
        val nextReview = System.currentTimeMillis() + (newInterval.toLong() * 24 * 60 * 60 * 1000L)
        flashcardDao.updateFlashcard(
            flashcard.copy(
                intervalDays = newInterval,
                easeFactor = newEase,
                nextReviewDate = nextReview
            )
        )
    }

    // Formulas
    val allFormulas: Flow<List<FormulaEntity>> = formulaDao.getAllFormulas()
    val favoriteFormulas: Flow<List<FormulaEntity>> = formulaDao.getFavoriteFormulas()
    fun getFormulasForSubject(subjectId: String): Flow<List<FormulaEntity>> = formulaDao.getFormulasForSubject(subjectId)
    fun searchFormulas(q: String): Flow<List<FormulaEntity>> = formulaDao.searchFormulas(q)
    suspend fun toggleFormulaFavorite(id: String, current: Boolean) = formulaDao.setFavorite(id, !current)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    fun getNotesForSubject(subjectId: String): Flow<List<NoteEntity>> = noteDao.getNotesForSubject(subjectId)
    suspend fun saveNote(note: NoteEntity) = noteDao.insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    // Study Planner
    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>> = studyDao.getTasksForDate(date)
    val allTasks: Flow<List<StudyTaskEntity>> = studyDao.getAllTasks()
    suspend fun updateTaskStatus(taskId: String, completed: Boolean) = studyDao.updateTaskStatus(taskId, completed)
    suspend fun addTask(task: StudyTaskEntity) = studyDao.insertTasks(listOf(task))

    suspend fun logStudySession(subjectId: String, topicName: String, durationMinutes: Int, sessionType: String) {
        studyDao.insertSession(
            StudySessionEntity(
                subjectId = subjectId,
                topicName = topicName,
                durationMinutes = durationMinutes,
                sessionType = sessionType
            )
        )
        userDao.addStudyMinutes(durationMinutes)
    }
    val recentStudySessions: Flow<List<StudySessionEntity>> = studyDao.getRecentSessions()
    val totalStudyMinutes: Flow<Int?> = studyDao.getTotalStudyMinutes()

    // Test Sessions
    val allTestSessions: Flow<List<TestSessionEntity>> = testDao.getAllTestSessions()
    suspend fun getTestSessionById(id: String): TestSessionEntity? = testDao.getTestSessionById(id)
    suspend fun saveTestSession(session: TestSessionEntity) = testDao.upsertTestSession(session)
    suspend fun autoSaveTestProgress(testId: String, answersJson: String, reviewJson: String) =
        testDao.autoSaveTestProgress(testId, answersJson, reviewJson)

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()
    suspend fun updateUserProfile(profile: UserProfileEntity) = userDao.insertUserProfile(profile)
    suspend fun updateUserRole(newRole: String) = userDao.updateUserRole(newRole)

    // Exam Config
    val examConfig: Flow<ExamConfigEntity?> = examDao.getExamConfig()
    val allExamEvents: Flow<List<ExamEventEntity>> = examDao.getAllExamEvents()
    fun getNextUpcomingExamEvent(currentTimeMillis: Long = System.currentTimeMillis()): Flow<ExamEventEntity?> =
        examDao.getNextUpcomingExamEvent(currentTimeMillis)
    val mainExamEvent: Flow<ExamEventEntity?> = examDao.getMainExamEvent()

    suspend fun resetDatabaseToCleanState() {
        AppDatabase.resetDatabaseToCleanState(database)
    }

    // Sync operations
    suspend fun syncSubjectsFromServer() {
        if (connectivityMonitor?.isOnline() != true || remoteDataSource == null) return
        try {
            val remoteSubjects = remoteDataSource.getSubjects()
            subjectDao.insertSubjects(remoteSubjects)
        } catch (e: Exception) {
            // Keep local cache
        }
    }

    suspend fun syncQuestionsFromServer(subjectId: String? = null, page: Int = 1) {
        if (connectivityMonitor?.isOnline() != true || remoteDataSource == null) return
        try {
            val response = remoteDataSource.getQuestions(subjectId = subjectId, page = page)
            questionDao.insertQuestions(response.data.map { it.toEntity() })
        } catch (e: Exception) {
            // Keep local cache
        }
    }

    suspend fun syncResourcesFromServer() {
        if (connectivityMonitor?.isOnline() != true || remoteDataSource == null) return
        try {
            val remoteResources = remoteDataSource.getResources()
            resourceDao.insertResources(remoteResources)
        } catch (e: Exception) {
            // Keep local cache
        }
    }

    private fun com.example.network.dto.QuestionDto.toEntity() = com.example.network.mapper.DtoMapper.run {
        this@toEntity.toEntity()
    }

    // Generate Adaptive Next Recommendations
    suspend fun getSmartRecommendation(): SmartRecommendation {
        val attentionList = masteryDao.getTopicsNeedingImmediateAttention().firstOrNull().orEmpty()
        val weakList = masteryDao.getWeakOrOverdueTopics().firstOrNull().orEmpty()
        val dueRevisions = revisionDao.getDueRevisionItems(System.currentTimeMillis()).firstOrNull().orEmpty()

        if (attentionList.isNotEmpty()) {
            val urgentTopic = attentionList.first()
            val topic = subjectDao.getTopicById(urgentTopic.topicId)
            val subj = topic?.let { subjectDao.getSubjectById(it.subjectId) }
            val reasonMsg = urgentTopic.attentionReason ?: "Requires immediate attention"
            return SmartRecommendation(
                actionTitle = "Immediate Remediation Needed",
                targetName = "${subj?.name ?: "GATE"} -> ${topic?.name ?: "Topic Focus"}",
                estimatedMinutes = 20,
                reason = "FLAGGED: $reasonMsg. Solve remediation PYQs now.",
                type = RecommendationType.PRACTICE_WEAK,
                relatedTopicId = topic?.id,
                relatedSubjectId = subj?.id
            )
        }

        if (dueRevisions.isNotEmpty()) {
            val q = questionDao.getQuestionById(dueRevisions.first().questionId)
            val topic = q?.let { subjectDao.getTopicById(it.topicId) }
            val subj = q?.let { subjectDao.getSubjectById(it.subjectId) }
            return SmartRecommendation(
                actionTitle = "Revise Overdue Mistakes",
                targetName = "${subj?.name ?: "GATE"} -> ${topic?.name ?: "Concept Review"}",
                estimatedMinutes = 20,
                reason = "You have ${dueRevisions.size} questions pending in your Spaced Repetition queue.",
                type = RecommendationType.REVISION,
                relatedTopicId = topic?.id,
                relatedSubjectId = subj?.id
            )
        }

        if (weakList.isNotEmpty()) {
            val weak = weakList.first()
            val topic = subjectDao.getTopicById(weak.topicId)
            val subj = topic?.let { subjectDao.getSubjectById(it.subjectId) }
            return SmartRecommendation(
                actionTitle = "Strengthen Weak Topic",
                targetName = "${subj?.name ?: "GATE"} -> ${topic?.name ?: "Core Concept"}",
                estimatedMinutes = 25,
                reason = "Your recent accuracy is ${weak.accuracy.roundToInt()}% (below 70% threshold). Solve 5 PYQs to build mastery.",
                type = RecommendationType.PRACTICE_WEAK,
                relatedTopicId = topic?.id,
                relatedSubjectId = subj?.id
            )
        }

        return SmartRecommendation(
            actionTitle = "High-Yield PYQ Drill",
            targetName = "Operating Systems -> Deadlocks & Resource Allocation",
            estimatedMinutes = 30,
            reason = "High-weightage topic (9.5% exam frequency). Master Banker's algorithm.",
            type = RecommendationType.STUDY_NEW,
            relatedTopicId = "OS_DEADLOCK",
            relatedSubjectId = "OS"
        )
    }
}

enum class RecommendationType {
    REVISION,
    PRACTICE_WEAK,
    STUDY_NEW,
    MOCK_TEST
}

data class SmartRecommendation(
    val actionTitle: String,
    val targetName: String,
    val estimatedMinutes: Int,
    val reason: String,
    val type: RecommendationType,
    val relatedTopicId: String? = null,
    val relatedSubjectId: String? = null
)
