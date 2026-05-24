package com.example.montesorrilearning.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_uploads")
data class PendingUpload(
    @PrimaryKey val id: String,
    val localPhotoPath: String,
    val childId: String,
    val title: String,
    val montessoriArea: String,
    val teacherComment: String,
    val isCover: Boolean,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
