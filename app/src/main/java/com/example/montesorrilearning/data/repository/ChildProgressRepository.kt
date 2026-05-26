package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.ChildProgressRequest
import com.example.montesorrilearning.domain.model.ChildProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildProgressRepository @Inject constructor(private val api: ApiService) {
    suspend fun getProgress(
        childId: String? = null, status: String? = null, syllabusId: String? = null
    ): Result<List<ChildProgress>> =
        runCatching { api.getChildProgress(childId, status, syllabusId).map { it.toDomain() } }

    suspend fun upsertProgress(request: ChildProgressRequest): Result<ChildProgress> =
        runCatching { api.upsertChildProgress(request).toDomain() }

    suspend fun deleteProgress(id: String): Result<Unit> =
        runCatching { api.deleteChildProgress(id); Unit }
}
