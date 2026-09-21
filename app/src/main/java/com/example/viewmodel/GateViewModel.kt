package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GateRepository
import com.example.data.repository.SmartRecommendation
import com.example.network.AiProviderType
import com.example.network.AiResult
import com.example.network.AiTutorService
import com.example.service.QuestionEvaluator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PracticeUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String = "",
    val isEvaluated: Boolean = false,
    val lastAttempt: AttemptEntity? = null,
    val timeSpentSeconds: Int = 0,
    val isFinished: Boolean = false,
    val score: Float = 0f,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
)

data class TestUiState(
    val testId: String = "",
    val title: String = "",
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<String, String> = emptyMap(),
    val markedForReview: Set<String> = emptySet(),
    val remainingSeconds: Int = 180 * 60, // 3 hours default for full mock
    val isTimerRunning: Boolean = false,
    val isSubmitted: Boolean = false,
    val submittedResult: TestSessionEntity? = null
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GateViewModel(
    private val repository: GateRepository,
    private val aiTutorService: AiTutorService
) : ViewModel() {

    // -------------------------------------------------------------
    // Global Syllabus & Data
    // -------------------------------------------------------------
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val examConfig: StateFlow<ExamConfigEntity?> = repository.examConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val nextExamEvent: StateFlow<ExamEventEntity?> = repository.getNextUpcomingExamEvent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExamEvents: StateFlow<List<ExamEventEntity>> = repository.allExamEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainExamEvent: StateFlow<ExamEventEntity?> = repository.mainExamEvent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topicMasteryList: StateFlow<List<TopicMasteryEntity>> = repository.allTopicMastery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAttempts: StateFlow<List<AttemptEntity>> = repository.recentAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakeAttempts: StateFlow<List<AttemptEntity>> = repository.mistakeAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // Smart Recommendations & Dashboard Metrics
    // -------------------------------------------------------------
    private val _recommendation = MutableStateFlow<SmartRecommendation?>(null)
    val recommendation: StateFlow<SmartRecommendation?> = _recommendation.asStateFlow()

    init {
        refreshRecommendation()
    }

    fun refreshRecommendation() {
        viewModelScope.launch {
            _recommendation.value = repository.getSmartRecommendation()
        }
    }

    // -------------------------------------------------------------
    // PYQ Explorer & Filter State
    // -------------------------------------------------------------
    val selectedSubjectFilter = MutableStateFlow<String?>(null)
    val selectedTopicFilter = MutableStateFlow<String?>(null)
    val selectedYearFilter = MutableStateFlow<Int?>(null)
    val selectedDifficultyFilter = MutableStateFlow<Difficulty?>(null)
    val selectedTypeFilter = MutableStateFlow<QuestionType?>(null)

    val availableYears: StateFlow<List<Int>> = repository.availableYears
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredQuestions: StateFlow<List<QuestionEntity>> = combine(
        selectedSubjectFilter,
        selectedTopicFilter,
        selectedYearFilter,
        selectedDifficultyFilter,
        selectedTypeFilter
    ) { sub, top, yr, diff, qType ->
        FilterParams(sub, top, yr, diff, qType)
    }.flatMapLatest { params ->
        repository.filterQuestions(
            params.subjectId,
            params.topicId,
            params.year,
            params.difficulty,
            params.qType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FilterParams(
        val subjectId: String?,
        val topicId: String?,
        val year: Int?,
        val difficulty: Difficulty?,
        val qType: QuestionType?
    )

    fun toggleBookmark(questionId: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleQuestionBookmark(questionId, current)
        }
    }

    // -------------------------------------------------------------
    // Practice Engine
    // -------------------------------------------------------------
    private val _practiceState = MutableStateFlow(PracticeUiState())
    val practiceState: StateFlow<PracticeUiState> = _practiceState.asStateFlow()
    private var practiceTimerJob: Job? = null

    fun startPractice(topicId: String = "ALL", questionCount: Int = 10) {
        viewModelScope.launch {
            val allQ = repository.allQuestions.firstOrNull().orEmpty()
            val filtered = if (topicId == "ALL") {
                allQ.shuffled().take(questionCount)
            } else {
                allQ.filter { it.topicId == topicId }.take(questionCount)
            }

            _practiceState.value = PracticeUiState(
                questions = if (filtered.isNotEmpty()) filtered else allQ.take(5),
                currentIndex = 0,
                selectedAnswer = "",
                isEvaluated = false,
                lastAttempt = null,
                timeSpentSeconds = 0,
                isFinished = false,
                score = 0f,
                correctCount = 0,
                incorrectCount = 0
            )
            startPracticeTimer()
        }
    }

    private fun startPracticeTimer() {
        practiceTimerJob?.cancel()
        practiceTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _practiceState.update { it.copy(timeSpentSeconds = it.timeSpentSeconds + 1) }
            }
        }
    }

    fun selectPracticeAnswer(answer: String) {
        if (!_practiceState.value.isEvaluated) {
            _practiceState.update { it.copy(selectedAnswer = answer) }
        }
    }

    fun submitPracticeCurrentAnswer(mistakeCategory: MistakeCategory? = null, userNote: String? = null) {
        val state = _practiceState.value
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        if (state.selectedAnswer.isBlank() && currentQ.questionType != QuestionType.NAT) return

        viewModelScope.launch {
            val attempt = repository.recordAttempt(
                question = currentQ,
                selectedAnswer = state.selectedAnswer,
                timeSpentSeconds = state.timeSpentSeconds,
                mistakeCategory = mistakeCategory,
                note = userNote
            )

            val newScore = state.score + attempt.marksAwarded
            val newCorrect = state.correctCount + if (attempt.isCorrect) 1 else 0
            val newIncorrect = state.incorrectCount + if (attempt.isCorrect) 0 else 1

            _practiceState.update {
                it.copy(
                    isEvaluated = true,
                    lastAttempt = attempt,
                    score = newScore,
                    correctCount = newCorrect,
                    incorrectCount = newIncorrect
                )
            }
            refreshRecommendation()
        }
    }

    fun nextPracticeQuestion() {
        val state = _practiceState.value
        if (state.currentIndex + 1 < state.questions.size) {
            _practiceState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswer = "",
                    isEvaluated = false,
                    lastAttempt = null,
                    timeSpentSeconds = 0
                )
            }
        } else {
            practiceTimerJob?.cancel()
            _practiceState.update { it.copy(isFinished = true) }
        }
    }

    // -------------------------------------------------------------
    // Test Engine & Full Exam Simulator
    // -------------------------------------------------------------
    private val _testState = MutableStateFlow(TestUiState())
    val testState: StateFlow<TestUiState> = _testState.asStateFlow()
    private var examTimerJob: Job? = null

    fun startExam(testType: String = "FULL_MOCK") {
        viewModelScope.launch {
            val allQ = repository.allQuestions.firstOrNull().orEmpty()
            val totalQ = if (testType == "FULL_MOCK") kotlin.math.min(65, allQ.size) else kotlin.math.min(15, allQ.size)
            val selected = allQ.shuffled().take(totalQ)
            val durationMin = if (testType == "FULL_MOCK") 180 else 30

            val testId = "TEST_" + System.currentTimeMillis()
            _testState.value = TestUiState(
                testId = testId,
                title = if (testType == "FULL_MOCK") "GATE 2027 All-India Grand Mock Test" else "Sectional Mini Mock",
                questions = selected,
                currentIndex = 0,
                userAnswers = emptyMap(),
                markedForReview = emptySet(),
                remainingSeconds = durationMin * 60,
                isTimerRunning = true,
                isSubmitted = false,
                submittedResult = null
            )
            startExamTimer()
        }
    }

    private fun startExamTimer() {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_testState.value.remainingSeconds > 0 && !_testState.value.isSubmitted) {
                delay(1000)
                _testState.update {
                    it.copy(remainingSeconds = it.remainingSeconds - 1)
                }
            }
            if (_testState.value.remainingSeconds <= 0 && !_testState.value.isSubmitted) {
                submitExam()
            }
        }
    }

    fun selectExamQuestionIndex(index: Int) {
        val maxIdx = _testState.value.questions.size - 1
        if (index in 0..maxIdx) {
            _testState.update { it.copy(currentIndex = index) }
        }
    }

    fun saveExamAnswer(answer: String) {
        val state = _testState.value
        val q = state.questions.getOrNull(state.currentIndex) ?: return
        val newAnswers = state.userAnswers.toMutableMap()
        newAnswers[q.id] = answer
        _testState.update { it.copy(userAnswers = newAnswers) }
    }

    fun clearExamResponse() {
        val state = _testState.value
        val q = state.questions.getOrNull(state.currentIndex) ?: return
        val newAnswers = state.userAnswers.toMutableMap()
        newAnswers.remove(q.id)
        _testState.update { it.copy(userAnswers = newAnswers) }
    }

    fun toggleMarkForReview() {
        val state = _testState.value
        val q = state.questions.getOrNull(state.currentIndex) ?: return
        val currentReview = state.markedForReview.toMutableSet()
        if (currentReview.contains(q.id)) {
            currentReview.remove(q.id)
        } else {
            currentReview.add(q.id)
        }
        _testState.update { it.copy(markedForReview = currentReview) }
    }

    fun submitExam() {
        examTimerJob?.cancel()
        val state = _testState.value
        if (state.isSubmitted) return

        viewModelScope.launch {
            var totalScore = 0f
            var correctCount = 0
            var attemptedCount = 0

            for (q in state.questions) {
                val ans = state.userAnswers[q.id]
                if (ans != null && ans.isNotBlank()) {
                    attemptedCount++
                    val eval = QuestionEvaluator.evaluate(q, ans)
                    totalScore += eval.marksAwarded
                    if (eval.isCorrect) correctCount++

                    // Record each attempt to database
                    repository.recordAttempt(
                        question = q,
                        selectedAnswer = eval.normalizedUserAnswer,
                        timeSpentSeconds = 60,
                        mistakeCategory = if (eval.isCorrect) null else MistakeCategory.TIME_PRESSURE
                    )
                }
            }

            val accuracy = if (attemptedCount > 0) (correctCount.toFloat() / attemptedCount) * 100f else 0f
            val session = TestSessionEntity(
                id = state.testId,
                title = state.title,
                testType = "FULL_MOCK",
                totalQuestions = state.questions.size,
                durationMinutes = 180,
                questionIdsJson = state.questions.joinToString(",") { it.id },
                userAnswersJson = state.userAnswers.entries.joinToString(";") { "${it.key}:${it.value}" },
                markedForReviewJson = state.markedForReview.joinToString(","),
                startedAt = System.currentTimeMillis() - ((180 * 60 - state.remainingSeconds) * 1000L),
                submittedAt = System.currentTimeMillis(),
                totalScore = totalScore,
                accuracy = accuracy,
                isCompleted = true
            )

            repository.saveTestSession(session)
            _testState.update {
                it.copy(
                    isSubmitted = true,
                    submittedResult = session
                )
            }
            refreshRecommendation()
        }
    }

    // -------------------------------------------------------------
    // Curated Resources Directory
    // -------------------------------------------------------------
    val allResources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedResourceType = MutableStateFlow<ResourceType?>(null)

    fun toggleResourceBookmark(id: String, current: Boolean) {
        viewModelScope.launch { repository.toggleResourceBookmark(id, current) }
    }

    fun toggleResourceCompleted(id: String, current: Boolean) {
        viewModelScope.launch { repository.toggleResourceCompleted(id, current) }
    }

    fun updateResourceNotes(id: String, notes: String) {
        viewModelScope.launch { repository.updateResourceNotes(id, notes) }
    }

    // -------------------------------------------------------------
    // Revision & Spaced Repetition
    // -------------------------------------------------------------
    val dueRevisionItems: StateFlow<List<RevisionItemEntity>> = repository.getDueRevisionItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRevisionItems: StateFlow<List<RevisionItemEntity>> = repository.allRevisionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topicsNeedingAttention: StateFlow<List<TopicMasteryEntity>> = repository.topicsNeedingAttention
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reevaluateTopicAttention(topicId: String) {
        viewModelScope.launch {
            repository.reevaluateTopicAttention(topicId)
            refreshRecommendation()
        }
    }

    fun markRevisionMastered(questionId: String) {
        viewModelScope.launch {
            repository.markRevisionMastered(questionId)
            refreshRecommendation()
        }
    }

    fun updateMistakeDetails(attemptId: Long, category: MistakeCategory, note: String?) {
        viewModelScope.launch {
            repository.updateMistakeDetails(attemptId, category, note)
        }
    }

    // -------------------------------------------------------------
    // Flashcards & Formula Sheets
    // -------------------------------------------------------------
    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFormulas: StateFlow<List<FormulaEntity>> = repository.allFormulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val formulaSearchQuery = MutableStateFlow("")

    val filteredFormulas: StateFlow<List<FormulaEntity>> = combine(allFormulas, formulaSearchQuery) { list, query ->
        if (query.isBlank()) list else list.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.subjectId.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reviewFlashcard(flashcard: FlashcardEntity, quality: Int) {
        viewModelScope.launch {
            repository.reviewFlashcard(flashcard, quality)
        }
    }

    fun toggleFormulaFavorite(id: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFormulaFavorite(id, current)
        }
    }

    // -------------------------------------------------------------
    // Notes Management
    // -------------------------------------------------------------
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveNote(title: String, content: String, subjectId: String, topicId: String?, tags: String) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = "NOTE_" + UUID.randomUUID().toString().take(8),
                title = title,
                content = content,
                subjectId = subjectId,
                topicId = topicId,
                tags = tags
            )
            repository.saveNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    // -------------------------------------------------------------
    // Study Planner & Focus Timer
    // -------------------------------------------------------------
    val currentDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dailyTasks: StateFlow<List<StudyTaskEntity>> = repository.getTasksForDate(currentDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudyMinutes: StateFlow<Int?> = repository.totalStudyMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleTaskStatus(taskId: String, current: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, !current)
        }
    }

    fun addStudyTask(title: String, subjectId: String, minutes: Int, priority: String) {
        viewModelScope.launch {
            val task = StudyTaskEntity(
                id = "TASK_" + UUID.randomUUID().toString().take(8),
                title = title,
                subjectId = subjectId,
                topicId = null,
                allocatedMinutes = minutes,
                priority = priority,
                isCompleted = false,
                targetDate = currentDateStr
            )
            repository.addTask(task)
        }
    }

    fun logFocusSession(subjectId: String, topicName: String, durationMinutes: Int, sessionType: String) {
        viewModelScope.launch {
            repository.logStudySession(subjectId, topicName, durationMinutes, sessionType)
        }
    }

    // -------------------------------------------------------------
    // AI Tutor
    // -------------------------------------------------------------
    private val _aiChatMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = "AI",
                message = "👋 Hello Aspirant! I am your GATE CSE 2027 AI Tutor. Ask me any conceptual question, algorithm proof, shortcut trick, or step-by-step derivation from the GATE syllabus!"
            )
        )
    )
    val aiChatMessages: StateFlow<List<AiChatMessage>> = _aiChatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiProviderType = MutableStateFlow(aiTutorService.activeProviderType)
    val aiProviderType: StateFlow<AiProviderType> = _aiProviderType.asStateFlow()

    fun setAiProviderType(type: AiProviderType) {
        aiTutorService.activeProviderType = type
        _aiProviderType.value = type
    }

    fun configureOllama(baseUrl: String, modelName: String) {
        aiTutorService.ollamaProvider.baseUrl = baseUrl
        aiTutorService.ollamaProvider.modelName = modelName
    }

    fun askAiTutor(prompt: String, question: QuestionEntity? = null, mode: String = "EXAM") {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(sender = "USER", message = prompt)
        _aiChatMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val result = aiTutorService.askTutor(
                question = question,
                prompt = prompt,
                userAnswer = null,
                mode = mode
            )
            val responseText = when (result) {
                is AiResult.Success -> "[${result.providerName}]\n\n${result.text}"
                is AiResult.Error -> "⚠️ Connection Error: ${result.message}\n\nYou can switch to the Verified Offline Knowledge Base in Settings or retry."
            }
            val aiMsg = AiChatMessage(sender = "AI", message = responseText)
            _aiChatMessages.update { it + aiMsg }
            _isAiLoading.value = false
        }
    }

    fun recordDirectAttempt(
        question: QuestionEntity,
        selectedAnswer: String,
        isCorrect: Boolean,
        mistakeCategory: MistakeCategory?,
        userNote: String?
    ) {
        viewModelScope.launch {
            repository.recordAttempt(
                question = question,
                selectedAnswer = selectedAnswer,
                timeSpentSeconds = 30,
                mistakeCategory = mistakeCategory,
                note = userNote
            )
        }
    }

    // -------------------------------------------------------------
    // Profile Updates, Role & Clean Database Reset
    // -------------------------------------------------------------
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    fun updateUserGoals(targetHours: Float, dailyQuestions: Int, weeklyTests: Int, preferredTime: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(
                current.copy(
                    dailyTargetHours = targetHours,
                    dailyQuestionsTarget = dailyQuestions,
                    weeklyTestsTarget = weeklyTests,
                    preferredStudyTime = preferredTime
                )
            )
        }
    }

    fun updateUserRole(newRole: String) {
        viewModelScope.launch {
            repository.updateUserRole(newRole)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetDatabaseToCleanState()
            refreshRecommendation()
        }
    }
}
