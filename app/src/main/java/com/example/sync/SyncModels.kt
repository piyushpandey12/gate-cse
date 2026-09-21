package com.example.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncState {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DOWNLOAD,
    CONFLICT,
    FAILED
}

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val serverVersion: Int = 0,
    val localVersion: Int = 0,
    val lastSyncedAt: Long = 0,
    val syncState: SyncState = SyncState.SYNCED,
    val lastError: String? = null
)

enum class OperationStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CONFLICT
}

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String, // CREATE, UPDATE, DELETE
    val entityType: String, // NOTE, BOOKMARK, ATTEMPT, etc.
    val entityId: String,
    val payload: String, // JSON payload
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val status: OperationStatus = OperationStatus.PENDING,
    val idempotencyKey: String,
    val errorMessage: String? = null
)

enum class ConnectivityState {
    ONLINE,
    OFFLINE,
    RECONNECTING
}
