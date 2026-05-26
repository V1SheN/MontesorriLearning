package com.example.montesorrilearning.data.local

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.WorkEntryRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: ApiService,
    private val dao: PendingUploadDao
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pendings = dao.getAllPendingSync()
        if (pendings.isEmpty()) return Result.success()

        val childId = pendings.first().childId
        val title = pendings.first().title
        val montessoriArea = pendings.first().montessoriArea
        val teacherComment = pendings.first().teacherComment

        val uploadedKeys = mutableListOf<com.example.montesorrilearning.data.remote.MediaKey>()
        for ((i, pending) in pendings.withIndex()) {
            try {
                val file = File(pending.localPhotoPath)
                if (!file.exists()) {
                    dao.deleteById(pending.id)
                    continue
                }
                val requestBody = file.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                val childIdPart = childId.toRequestBody("text/plain".toMediaTypeOrNull())
                val isCoverPart = (pending.isCover).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = api.uploadPhoto(part, childIdPart, isCoverPart, null)
                uploadedKeys.add(com.example.montesorrilearning.data.remote.MediaKey(
                    storageKey = response.storageKey,
                    thumbnailKey = response.thumbnailKey,
                    isCover = pending.isCover,
                    sortOrder = i
                ))
                dao.deleteById(pending.id)
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        if (uploadedKeys.isNotEmpty()) {
            try {
                api.createWorkEntry(WorkEntryRequest(
                    childId = childId,
                    montessoriArea = montessoriArea,
                    title = title,
                    teacherComment = teacherComment,
                    media = uploadedKeys
                ))
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        return Result.success()
    }
}
