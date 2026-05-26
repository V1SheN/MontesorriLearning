package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.SyllabusRequest
import com.example.montesorrilearning.domain.model.Syllabus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyllabusRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getSyllabus(
        classroomId: String? = null,
        montessoriArea: String? = null,
        year: Int? = null
    ): Result<List<Syllabus>> {
        return try {
            Result.success(api.getSyllabus(classroomId, montessoriArea, year).map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSyllabusItem(id: String): Result<Syllabus> {
        return try {
            Result.success(api.getSyllabusItem(id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSyllabus(request: SyllabusRequest): Result<Syllabus> {
        return try {
            Result.success(api.createSyllabus(request).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSyllabus(id: String, request: SyllabusRequest): Result<Syllabus> {
        return try {
            Result.success(api.updateSyllabus(id, request).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSyllabus(id: String): Result<Unit> {
        return try {
            val response = api.deleteSyllabus(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
