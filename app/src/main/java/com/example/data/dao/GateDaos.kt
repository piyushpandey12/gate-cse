package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics ORDER BY orderIndex ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY year DESC, questionNumber ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE isPyq = 1 ORDER BY year DESC, questionNumber ASC")
    fun getAllPyqs(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND isPyq = 1 ORDER BY year DESC")
    fun getPyqsForSubject(subjectId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY year DESC")
    fun getQuestionsForTopic(topicId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions 
        WHERE (:subjectId IS NULL OR subjectId = :subjectId)
        AND (:topicId IS NULL OR topicId = :topicId)
        AND (:year IS NULL OR year = :year)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:qType IS NULL OR questionType = :qType)
        ORDER BY year DESC, questionNumber ASC
    """)
    fun filterQuestions(
        subjectId: String?,
        topicId: String?,
        year: Int?,
        difficulty: Difficulty?,
        qType: QuestionType?
    ): Flow<List<QuestionEntity>>

    @Query("SELECT DISTINCT year FROM questions WHERE year IS NOT NULL ORDER BY year DESC")
    fun getAvailableYears(): Flow<List<Int>>

    @Query("SELECT * FROM questions WHERE questionText LIKE '%' || :query || '%' OR detailedSolution LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET isBookmarked = :isBookmarked WHERE id = :questionId")
    suspend fun updateBookmark(questionId: String, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE isPyq = 1")
    suspend fun getPyqCount(): Int
}

@Dao
interface AttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity): Long

    @Query("SELECT * FROM attempts ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE isCorrect = 0 ORDER BY timestamp DESC")
    fun getMistakeAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE questionId = :questionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAttemptForQuestion(questionId: String): AttemptEntity?

    @Query("UPDATE attempts SET mistakeCategory = :mistakeCategory, userNote = :note WHERE id = :attemptId")
    suspend fun updateMistakeDetails(attemptId: Long, mistakeCategory: MistakeCategory, note: String?)

    @Query("SELECT COUNT(*) FROM attempts")
    fun getTotalAttemptsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attempts WHERE isCorrect = 1")
    fun getCorrectAttemptsCount(): Flow<Int>

    @Query("SELECT a.* FROM attempts a INNER JOIN questions q ON a.questionId = q.id WHERE q.topicId = :topicId ORDER BY a.timestamp ASC")
    suspend fun getAttemptsForTopic(topicId: String): List<AttemptEntity>
}

@Dao
interface MasteryDao {
    @Query("SELECT * FROM topic_mastery")
    fun getAllMastery(): Flow<List<TopicMasteryEntity>>

    @Query("SELECT * FROM topic_mastery WHERE topicId = :topicId")
    suspend fun getMasteryForTopic(topicId: String): TopicMasteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMastery(mastery: TopicMasteryEntity)

    @Query("SELECT * FROM topic_mastery WHERE level = 'WEAK' OR needsRevision = 1")
    fun getWeakOrOverdueTopics(): Flow<List<TopicMasteryEntity>>

    @Query("SELECT * FROM topic_mastery WHERE needsImmediateAttention = 1")
    fun getTopicsNeedingImmediateAttention(): Flow<List<TopicMasteryEntity>>
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE subjectId = :subjectId ORDER BY resourceType, title")
    fun getResourcesForSubject(subjectId: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE resourceType = :type")
    fun getResourcesByType(type: ResourceType): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE isBookmarked = 1")
    fun getBookmarkedResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY subjectId, resourceType")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("UPDATE resources SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE resources SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query("UPDATE resources SET personalNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_items WHERE nextReviewDueDate <= :currentTimestamp ORDER BY nextReviewDueDate ASC")
    fun getDueRevisionItems(currentTimestamp: Long): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items ORDER BY nextReviewDueDate ASC")
    fun getAllRevisionItems(): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items WHERE questionId = :questionId")
    suspend fun getRevisionItemForQuestion(questionId: String): RevisionItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRevisionItem(item: RevisionItemEntity)

    @Delete
    suspend fun deleteRevisionItem(item: RevisionItemEntity)

    @Query("DELETE FROM revision_items WHERE questionId = :questionId")
    suspend fun deleteRevisionByQuestionId(questionId: String)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId")
    fun getFlashcardsForSubject(subjectId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueFlashcards(now: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY subjectId, keyConcept")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)
}

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas WHERE subjectId = :subjectId")
    fun getFormulasForSubject(subjectId: String): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE isFavorite = 1")
    fun getFavoriteFormulas(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas ORDER BY subjectId, name")
    fun getAllFormulas(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchFormulas(query: String): Flow<List<FormulaEntity>>

    @Query("UPDATE formulas SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<FormulaEntity>)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    fun getNotesForSubject(subjectId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_tasks WHERE targetDate = :date ORDER BY priority ASC")
    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks ORDER BY targetDate DESC")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("UPDATE study_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Insert
    suspend fun insertSession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions")
    fun getTotalStudyMinutes(): Flow<Int?>
}

@Dao
interface TestDao {
    @Query("SELECT * FROM test_sessions ORDER BY startedAt DESC")
    fun getAllTestSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM test_sessions WHERE id = :testId")
    suspend fun getTestSessionById(testId: String): TestSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTestSession(testSession: TestSessionEntity)

    @Query("UPDATE test_sessions SET userAnswersJson = :answersJson, markedForReviewJson = :reviewJson WHERE id = :testId")
    suspend fun autoSaveTestProgress(testId: String, answersJson: String, reviewJson: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalQuestionsSolved = totalQuestionsSolved + 1, totalCorrect = totalCorrect + :isCorrectDelta WHERE id = 'default_user'")
    suspend fun incrementSolvedCounter(isCorrectDelta: Int)

    @Query("UPDATE user_profile SET totalMinutesStudied = totalMinutesStudied + :minutes WHERE id = 'default_user'")
    suspend fun addStudyMinutes(minutes: Int)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET role = :newRole WHERE id = 'default_user'")
    suspend fun updateUserRole(newRole: String)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_config WHERE id = 'gate_cse_2027' LIMIT 1")
    fun getExamConfig(): Flow<ExamConfigEntity?>

    @Query("SELECT * FROM exam_config WHERE id = 'gate_cse_2027' LIMIT 1")
    suspend fun getExamConfigOnce(): ExamConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamConfig(config: ExamConfigEntity)

    @Query("SELECT * FROM exam_events ORDER BY eventDateMillis ASC")
    fun getAllExamEvents(): Flow<List<ExamEventEntity>>

    @Query("SELECT * FROM exam_events WHERE eventDateMillis >= :currentTimeMillis ORDER BY eventDateMillis ASC LIMIT 1")
    fun getNextUpcomingExamEvent(currentTimeMillis: Long): Flow<ExamEventEntity?>

    @Query("SELECT * FROM exam_events WHERE isMainExamDate = 1 LIMIT 1")
    fun getMainExamEvent(): Flow<ExamEventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamEvents(events: List<ExamEventEntity>)
}
