package com.example.network

import android.content.Context
import com.example.network.api.GateApi
import com.example.network.auth.AuthInterceptor
import com.example.network.auth.TokenManager
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    val tokenManager: TokenManager by lazy { TokenManager(context) }

    private val authInterceptor: AuthInterceptor by lazy { AuthInterceptor(tokenManager) }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val api: GateApi by lazy { retrofit.create(GateApi::class.java) }

    private fun getBaseUrl(): String {
        return try {
            val buildConfigClass = context.classLoader?.loadClass("com.example.BuildConfig")
            val field = buildConfigClass?.getField("API_BASE_URL")
            field?.get(null) as? String ?: "http://10.0.2.2:8000/"
        } catch (e: Exception) {
            "http://10.0.2.2:8000/"
        }
    }
}
