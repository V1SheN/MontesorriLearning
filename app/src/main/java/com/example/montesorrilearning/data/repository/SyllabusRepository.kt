package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.SyllabusRequest
import com.example.montesorrilearning.domain.model.Syllabus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyllabusRepository @Inject constructor(private val api: ApiService) {
    suspend fun getSyllabus(
        classroomId: String? = null, termId: String? = null,
        montessoriArea: String? = null, weekNumber: Int? = null,
        dayOfWeek: Int? = null, isExtracurricular: Boolean? = null
    ): Result<List<Syllabus>> =
        runCatching { api.getSyllabus(classroomId, termId, montessoriArea, weekNumber, dayOfWeek, isExtracurricular).map { it.toDomain() } }

    suspend fun getSyllabusItem(id: String): Result<Syllabus> =
        runCatching { api.getSyllabusItem(id).toDomain() }

    suspend fun createSyllabus(request: SyllabusRequest): Result<Syllabus> =
        runCatching { api.createSyllabus(request).toDomain() }

    suspend fun updateSyllabus(id: String, request: SyllabusRequest): Result<Syllabus> =
        runCatching { api.updateSyllabus(id, request).toDomain() }

    suspend fun deleteSyllabus(id: String): Result<Unit> =
        runCatching { val r = api.deleteSyllabus(id); if (r.isSuccessful) Unit else throw Exception("Delete failed: ${r.code()}") }
}
