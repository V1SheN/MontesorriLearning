package com.example.montesorrilearning.data.repository

import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.LoginRequest
import com.example.montesorrilearning.data.remote.RefreshRequest
import com.example.montesorrilearning.data.remote.RegisterRequest
import com.example.montesorrilearning.util.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(email, password))
            tokenManager.saveAuthData(
                access = response.accessToken,
                refresh = response.refreshToken,
                role = response.user.role,
                userId = response.user.id,
                name = response.user.displayName
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String, role: String): Result<Unit> {
        return try {
            val response = api.register(RegisterRequest(email, password, displayName, role))
            tokenManager.saveAuthData(
                access = response.accessToken,
                refresh = response.refreshToken,
                role = response.user.role,
                userId = response.user.id,
                name = response.user.displayName
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<String> {
        return try {
            val currentRefresh = tokenManager.getRefreshToken() ?: return Result.failure(Exception("No refresh token"))
            val response = api.refresh(RefreshRequest(currentRefresh))
            tokenManager.saveAuthData(
                access = response.accessToken,
                refresh = response.refreshToken,
                role = response.user.role,
                userId = response.user.id,
                name = response.user.displayName
            )
            Result.success(response.accessToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clear()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.getAccessToken() != null

    suspend fun getRole(): String? = tokenManager.getRole()
}
