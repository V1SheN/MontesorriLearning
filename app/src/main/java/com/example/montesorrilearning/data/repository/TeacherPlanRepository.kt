package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.TeacherPlanRequest
import com.example.montesorrilearning.domain.model.TeacherPlan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherPlanRepository @Inject constructor(private val api: ApiService) {
    suspend fun getPlans(
        classroomId: String? = null, termId: String? = null,
        fromDate: String? = null, toDate: String? = null
    ): Result<List<TeacherPlan>> =
        runCatching { api.getTeacherPlans(classroomId, termId, fromDate, toDate).map { it.toDomain() } }

    suspend fun createPlan(request: TeacherPlanRequest): Result<TeacherPlan> =
        runCatching { api.createTeacherPlan(request).toDomain() }

    suspend fun updatePlan(id: String, request: TeacherPlanRequest): Result<TeacherPlan> =
        runCatching { api.updateTeacherPlan(id, request).toDomain() }

    suspend fun deletePlan(id: String): Result<Unit> =
        runCatching { api.deleteTeacherPlan(id); Unit }
}
