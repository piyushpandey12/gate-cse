package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import com.example.sync.PendingOperationEntity
import com.example.sync.SyncMetadataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_metadata` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `serverVersion` INTEGER NOT NULL DEFAULT 0, `localVersion` INTEGER NOT NULL DEFAULT 0, `lastSyncedAt` INTEGER NOT NULL DEFAULT 0, `syncState` TEXT NOT NULL DEFAULT 'SYNCED', `lastError` TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_operations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `operationType` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `payload` TEXT NOT NULL, `createdAt` INTEGER NOT NULL DEFAULT 0, `retryCount` INTEGER NOT NULL DEFAULT 0, `lastAttemptAt` INTEGER, `status` TEXT NOT NULL DEFAULT 'PENDING', `idempotencyKey` TEXT NOT NULL, `errorMessage` TEXT)")
    }
}

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        TopicMasteryEntity::class,
        ResourceEntity::class,
        RevisionItemEntity::class,
        FlashcardEntity::class,
        FormulaEntity::class,
        NoteEntity::class,
        StudyTaskEntity::class,
        StudySessionEntity::class,
        TestSessionEntity::class,
        UserProfileEntity::class,
        ExamConfigEntity::class,
        ExamEventEntity::class,
        SyncMetadataEntity::class,
        PendingOperationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun masteryDao(): MasteryDao
    abstract fun resourceDao(): ResourceDao
    abstract fun revisionDao(): RevisionDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun formulaDao(): FormulaDao
    abstract fun noteDao(): NoteDao
    abstract fun studyDao(): StudyDao
    abstract fun testDao(): TestDao
    abstract fun userDao(): UserDao
    abstract fun examDao(): ExamDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gate_cse_database.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.examDao().getExamConfigOnce() == null) {
                            database.examDao().insertExamConfig(SeedData.getInitialExamConfig())
                            database.examDao().insertExamEvents(SeedData.getInitialExamEvents())
                        }
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            db.subjectDao().insertSubjects(SeedData.getInitialSubjects())
            db.subjectDao().insertTopics(SeedData.getInitialTopics())
            db.resourceDao().insertResources(SeedData.getInitialResources())
            db.questionDao().insertQuestions(SeedData.getInitialQuestions())
            db.formulaDao().insertFormulas(SeedData.getInitialFormulas())
            db.flashcardDao().insertFlashcards(SeedData.getInitialFlashcards())
            db.studyDao().insertTasks(SeedData.getInitialTasks())
            db.userDao().insertUserProfile(UserProfileEntity())
            db.examDao().insertExamConfig(SeedData.getInitialExamConfig())
            db.examDao().insertExamEvents(SeedData.getInitialExamEvents())

            SeedData.getInitialTopics().forEach { topic ->
                db.masteryDao().upsertMastery(
                    TopicMasteryEntity(
                        topicId = topic.id,
                        attemptsCount = 0,
                        correctCount = 0,
                        accuracy = 0f,
                        averageTimeSeconds = 0,
                        masteryScore = 0f,
                        level = MasteryLevel.NEW
                    )
                )
            }
        }

        suspend fun resetDatabaseToCleanState(db: AppDatabase) {
            db.clearAllTables()
            populateDatabase(db)
        }
    }
}
