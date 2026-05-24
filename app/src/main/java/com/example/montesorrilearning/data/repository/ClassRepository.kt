package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.domain.model.Classroom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getClassrooms(): Result<List<Classroom>> {
        return try {
            Result.success(api.getClassrooms())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
