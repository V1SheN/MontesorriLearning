package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.domain.model.Child
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getChildren(): Result<List<Child>> {
        return try {
            Result.success(api.getChildren())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChild(child: Child): Result<Child> {
        return try {
            Result.success(api.createChild(child))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
