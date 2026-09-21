package com.example.data.dao

import androidx.room.*
import com.example.sync.SyncMetadataEntity
import com.example.sync.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun getMetadata(entityType: String, entityId: String): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE syncState != 'SYNCED'")
    fun getUnsyncedItems(): Flow<List<SyncMetadataEntity>>

    @Query("UPDATE sync_metadata SET syncState = :state, lastError = :error WHERE id = :id")
    suspend fun updateSyncState(id: Long, state: SyncState, error: String? = null)
}
