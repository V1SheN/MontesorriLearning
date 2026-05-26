package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.TermRequest
import com.example.montesorrilearning.domain.model.Term
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TermRepository @Inject constructor(private val api: ApiService) {
    suspend fun getTerms(year: Int? = null): Result<List<Term>> =
        runCatching { api.getTerms(year).map { it.toDomain() } }

    suspend fun createTerm(request: TermRequest): Result<Term> =
        runCatching { api.createTerm(request).toDomain() }

    suspend fun updateTerm(id: String, request: TermRequest): Result<Term> =
        runCatching { api.updateTerm(id, request).toDomain() }

    suspend fun deleteTerm(id: String): Result<Unit> =
        runCatching { api.deleteTerm(id); Unit }
}
