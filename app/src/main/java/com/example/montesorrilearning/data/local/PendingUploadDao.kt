package com.example.montesorrilearning.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {

    @Query("SELECT * FROM pending_uploads ORDER BY createdAt ASC")
    fun getAllPending(): Flow<List<PendingUpload>>

    @Query("SELECT * FROM pending_uploads ORDER BY createdAt ASC")
    suspend fun getAllPendingSync(): List<PendingUpload>

    @Insert
    suspend fun insert(upload: PendingUpload)

    @Update
    suspend fun update(upload: PendingUpload)

    @Query("DELETE FROM pending_uploads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM pending_uploads")
    suspend fun count(): Int
}
