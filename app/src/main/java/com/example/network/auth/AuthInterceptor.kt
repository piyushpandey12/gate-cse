package com.example.network.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login/register endpoints
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register")
        ) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.getAccessToken()
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }

        val response = chain.proceed(request)

        // Handle 401 - token expired or invalid
        if (response.code == 401) {
            tokenManager.clearAll()
            // Could broadcast to UI to redirect to login
        }

        return response
    }
}
