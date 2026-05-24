package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.MessageRequest
import com.example.montesorrilearning.domain.model.Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getMessages(): Result<List<Message>> {
        return try {
            Result.success(api.getMessages())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(subject: String?, body: String, classroomId: String?): Result<Message> {
        return try {
            Result.success(api.sendMessage(MessageRequest(subject, body, classroomId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markRead(id: String): Result<Unit> {
        return try {
            val response = api.markMessageRead(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Mark read failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
