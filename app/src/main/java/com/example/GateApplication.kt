package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.GateRepository
import com.example.network.ApiClient
import com.example.network.RemoteDataSource
import com.example.sync.ConnectivityMonitor
import com.example.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GateApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val apiClient by lazy { ApiClient(this) }

    val remoteDataSource by lazy { RemoteDataSource(apiClient) }

    val connectivityMonitor by lazy { ConnectivityMonitor(this) }

    val syncManager by lazy {
        SyncManager(
            connectivityMonitor = connectivityMonitor,
            remoteDataSource = remoteDataSource,
            syncMetadataDao = database.syncMetadataDao(),
            pendingOperationDao = database.pendingOperationDao(),
            scope = applicationScope
        )
    }

    val repository by lazy {
        GateRepository(
            subjectDao = database.subjectDao(),
            questionDao = database.questionDao(),
            attemptDao = database.attemptDao(),
            masteryDao = database.masteryDao(),
            resourceDao = database.resourceDao(),
            revisionDao = database.revisionDao(),
            flashcardDao = database.flashcardDao(),
            formulaDao = database.formulaDao(),
            noteDao = database.noteDao(),
            studyDao = database.studyDao(),
            testDao = database.testDao(),
            userDao = database.userDao(),
            examDao = database.examDao(),
            database = database,
            remoteDataSource = remoteDataSource,
            connectivityMonitor = connectivityMonitor,
            syncManager = syncManager
        )
    }

    fun syncPendingOperations() {
        syncManager.syncPendingOperations()
    }
}
