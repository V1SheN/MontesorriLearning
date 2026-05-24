package com.example.montesorrilearning.data.remote

import com.example.montesorrilearning.util.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Provider

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val apiService: Provider<ApiService>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        if (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/auth/refresh")) {
            return chain.proceed(original)
        }

        val token = runBlocking { tokenManager.getAccessToken() }

        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            val success = runBlocking {
                try {
                    val refreshToken = tokenManager.getRefreshToken() ?: return@runBlocking false
                    val refreshResponse = apiService.get().refresh(RefreshRequest(refreshToken))
                    tokenManager.saveAuthData(
                        access = refreshResponse.accessToken,
                        refresh = refreshResponse.refreshToken,
                        role = refreshResponse.user.role,
                        userId = refreshResponse.user.id,
                        name = refreshResponse.user.displayName
                    )
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (success) {
                response.close()
                val newToken = runBlocking { tokenManager.getAccessToken() } ?: return response
                return chain.proceed(
                    original.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                )
            } else {
                runBlocking { tokenManager.clear() }
            }
        }

        return response
    }
}
