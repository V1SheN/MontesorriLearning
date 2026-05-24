package com.example.montesorrilearning.data.repository

import android.content.Context
import android.net.Uri
import com.example.montesorrilearning.data.local.AppDatabase
import com.example.montesorrilearning.data.local.PendingUpload
import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.DailyCount
import com.example.montesorrilearning.data.remote.DailySummary
import com.example.montesorrilearning.data.remote.WorkEntryRequest
import com.example.montesorrilearning.domain.model.WorkEntry
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkRepository @Inject constructor(
    private val api: ApiService,
    private val database: AppDatabase,
    private val context: Context
) {

    suspend fun getWorkEntries(childId: String?, date: String?, page: Int? = null, limit: Int? = null): Result<List<WorkEntry>> {
        return try {
            Result.success(api.getWorkEntries(childId, date, page, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createWorkEntry(request: WorkEntryRequest): Result<WorkEntry> {
        return try {
            Result.success(api.createWorkEntry(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWorkEntry(id: String): Result<Unit> {
        return try {
            val response = api.deleteWorkEntry(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyCount(childId: String): Result<DailyCount> {
        return try {
            Result.success(api.getDailyCount(childId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailySummary(date: String, classroomId: String?): Result<DailySummary> {
        return try {
            Result.success(api.getDailySummary(date, classroomId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(
        uri: Uri,
        childId: String,
        isCover: Boolean,
        overrideLimit: Boolean
    ): Result<Unit> {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open file"))

            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", "photo_${System.currentTimeMillis()}.jpg", requestBody)
            val childIdBody = childId.toRequestBody("text/plain".toMediaTypeOrNull())
            val isCoverBody = isCover.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val overrideHeader = if (overrideLimit) "true" else null

            api.uploadPhoto(filePart, childIdBody, isCoverBody, overrideHeader)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun queueOfflineUpload(
        localPhotoPath: String,
        childId: String,
        title: String,
        montessoriArea: String,
        teacherComment: String,
        isCover: Boolean
    ) {
        val pending = PendingUpload(
            id = UUID.randomUUID().toString(),
            localPhotoPath = localPhotoPath,
            childId = childId,
            title = title,
            montessoriArea = montessoriArea,
            teacherComment = teacherComment,
            isCover = isCover
        )
        database.pendingUploadDao().insert(pending)
    }
}
