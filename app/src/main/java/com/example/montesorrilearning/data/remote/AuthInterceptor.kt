package com.example.montesorrilearning.data.remote

import com.example.montesorrilearning.util.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = runBlocking {
            tokenManager.getAccessToken()
        }

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
