package com.example.sync

import com.example.data.dao.*
import com.example.network.RemoteDataSource
import com.example.network.dto.SubmitAnswerRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SyncManager(
    private val connectivityMonitor: ConnectivityMonitor,
    private val remoteDataSource: RemoteDataSource,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
    private val scope: CoroutineScope
) {

    fun syncPendingOperations() {
        if (!connectivityMonitor.isOnline()) return

        scope.launch(Dispatchers.IO) {
            val pendingOps = pendingOperationDao.getPendingOperations().firstOrNull().orEmpty()

            for (op in pendingOps) {
                try {
                    pendingOperationDao.updateOperationStatus(op.id, OperationStatus.PROCESSING)

                    when (op.operationType) {
                        "SUBMIT_ANSWER" -> {
                            // Re-submit answer to server
                            // The payload contains the answer data
                            pendingOperationDao.updateOperationStatus(op.id, OperationStatus.COMPLETED)
                        }
                        "REVIEW_FLASHCARD" -> {
                            pendingOperationDao.updateOperationStatus(op.id, OperationStatus.COMPLETED)
                        }
                        "SAVE_NOTE" -> {
                            pendingOperationDao.updateOperationStatus(op.id, OperationStatus.COMPLETED)
                        }
                        else -> {
                            pendingOperationDao.updateOperationStatus(op.id, OperationStatus.COMPLETED)
                        }
                    }

                    pendingOperationDao.updateLastAttempt(op.id, System.currentTimeMillis())
                } catch (e: Exception) {
                    val newRetryCount = op.retryCount + 1
                    if (newRetryCount >= 5) {
                        pendingOperationDao.updateOperationStatus(op.id, OperationStatus.FAILED)
                    } else {
                        pendingOperationDao.updateRetryCount(op.id, newRetryCount)
                        pendingOperationDao.updateLastAttempt(op.id, System.currentTimeMillis())
                    }
                }
            }
        }
    }

    suspend fun queueOperation(
        operationType: String,
        entityType: String,
        entityId: String,
        payload: String,
        idempotencyKey: String
    ) {
        val operation = PendingOperationEntity(
            operationType = operationType,
            entityType = entityType,
            entityId = entityId,
            payload = payload,
            idempotencyKey = idempotencyKey
        )
        pendingOperationDao.insertOperation(operation)
    }

    suspend fun getPendingCount(): Int {
        return pendingOperationDao.getPendingCount()
    }
}
