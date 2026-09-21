package com.example.data.dao

import androidx.room.*
import com.example.sync.PendingOperationEntity
import com.example.sync.OperationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingOperations(): Flow<List<PendingOperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: PendingOperationEntity): Long

    @Query("UPDATE pending_operations SET status = :status WHERE id = :id")
    suspend fun updateOperationStatus(id: Long, status: OperationStatus)

    @Query("UPDATE pending_operations SET retryCount = :retryCount WHERE id = :id")
    suspend fun updateRetryCount(id: Long, retryCount: Int)

    @Query("UPDATE pending_operations SET lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateLastAttempt(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Delete
    suspend fun deleteOperation(operation: PendingOperationEntity)
}
