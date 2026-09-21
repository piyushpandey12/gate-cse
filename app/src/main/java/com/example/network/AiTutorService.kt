package com.example.network

import com.example.BuildConfig
import com.example.data.model.QuestionEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = "user"
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}

class AiTutorService {

    val ollamaProvider = OllamaAiProvider()
    val geminiProvider = GeminiAiProvider()
    val offlineProvider = OfflineCuratedTutorProvider()

    var activeProviderType: AiProviderType = AiProviderType.OLLAMA

    fun getActiveProvider(): AiProvider {
        return when (activeProviderType) {
            AiProviderType.OLLAMA -> ollamaProvider
            AiProviderType.GEMINI -> geminiProvider
            AiProviderType.OFFLINE_KNOWLEDGE -> offlineProvider
        }
    }

    suspend fun askTutor(
        question: QuestionEntity?,
        prompt: String,
        userAnswer: String? = null,
        mode: String = "EXAM"
    ): AiResult {
        val provider = getActiveProvider()
        val result = provider.generateResponse(prompt, question, userAnswer, mode)

        // If primary remote provider fails (e.g. Ollama or Gemini offline),
        // we return the real error so UI shows error + retry or user can switch to offline knowledge
        return result
    }

    suspend fun askTutorWithFallback(
        question: QuestionEntity?,
        prompt: String,
        userAnswer: String? = null,
        mode: String = "EXAM"
    ): String {
        return when (val res = askTutor(question, prompt, userAnswer, mode)) {
            is AiResult.Success -> res.text
            is AiResult.Error -> {
                // If caller requested simple string with automatic textbook fallback:
                val fallback = offlineProvider.generateResponse(prompt, question, userAnswer, mode)
                if (fallback is AiResult.Success) {
                    "> ⚠️ *(${res.message})*\n> *Showing verified textbook solution:*\n\n${fallback.text}"
                } else {
                    res.message
                }
            }
        }
    }
}
