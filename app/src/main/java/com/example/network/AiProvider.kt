package com.example.network

import com.example.BuildConfig
import com.example.data.model.QuestionEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

sealed class AiResult {
    data class Success(val text: String, val providerName: String) : AiResult()
    data class Error(val message: String, val canRetry: Boolean = true) : AiResult()
}

enum class AiProviderType {
    OLLAMA,
    GEMINI,
    OFFLINE_KNOWLEDGE
}

interface AiProvider {
    val type: AiProviderType
    val displayName: String
    suspend fun generateResponse(
        prompt: String,
        question: QuestionEntity?,
        userAnswer: String? = null,
        mode: String = "EXAM"
    ): AiResult
}

// -------------------------------------------------------------
// Ollama Local / Emulator AI Provider (Default: http://10.0.2.2:11434)
// -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val system: String? = null
)

@JsonClass(generateAdapter = true)
data class OllamaGenerateResponse(
    val response: String? = null,
    val done: Boolean = true
)

class OllamaAiProvider(
    var baseUrl: String = "http://10.0.2.2:11434",
    var modelName: String = "qwen3:0.6b"
) : AiProvider {

    override val type: AiProviderType = AiProviderType.OLLAMA
    override val displayName: String
        get() = "Ollama Local ($modelName @ $baseUrl)"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder().build()
    private val reqAdapter = moshi.adapter(OllamaGenerateRequest::class.java)
    private val resAdapter = moshi.adapter(OllamaGenerateResponse::class.java)

    override suspend fun generateResponse(
        prompt: String,
        question: QuestionEntity?,
        userAnswer: String?,
        mode: String
    ): AiResult = withContext(Dispatchers.IO) {
        val fullPrompt = buildEngineeringContext(question, prompt, userAnswer, mode)
        val systemPrompt = "You are an expert AI Tutor for GATE Computer Science & IT. Answer concisely and accurately according to standard university textbooks."

        val requestPayload = OllamaGenerateRequest(
            model = modelName,
            prompt = fullPrompt,
            stream = false,
            system = systemPrompt
        )

        val jsonBody = reqAdapter.toJson(requestPayload)
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val endpoint = "$cleanBaseUrl/api/generate"

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext AiResult.Error(
                    "Ollama server returned HTTP ${response.code}: ${response.message}. Ensure model '$modelName' is installed (`ollama pull $modelName`)."
                )
            }

            val bodyString = response.body?.string()
            if (bodyString.isNullOrBlank()) {
                return@withContext AiResult.Error("Empty response received from Ollama.")
            }

            val parsed = resAdapter.fromJson(bodyString)
            val output = parsed?.response?.trim()

            if (output.isNullOrBlank()) {
                AiResult.Error("Ollama model generated an empty text response.")
            } else {
                AiResult.Success(output, "Ollama ($modelName)")
            }
        } catch (e: ConnectException) {
            AiResult.Error(
                "Cannot connect to Ollama at $endpoint. Ensure Ollama is running (`ollama serve`). If on Android Emulator, use http://10.0.2.2:11434."
            )
        } catch (e: SocketTimeoutException) {
            AiResult.Error("Ollama request timed out after 60s while generating response.")
        } catch (e: IOException) {
            AiResult.Error("Network I/O error communicating with Ollama: ${e.localizedMessage ?: "Unknown error"}")
        } catch (e: Exception) {
            AiResult.Error("Unexpected Ollama error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}

// -------------------------------------------------------------
// Gemini Cloud AI Provider
// -------------------------------------------------------------

class GeminiAiProvider : AiProvider {

    override val type: AiProviderType = AiProviderType.GEMINI
    override val displayName: String = "Google Gemini Flash (Cloud)"

    override suspend fun generateResponse(
        prompt: String,
        question: QuestionEntity?,
        userAnswer: String?,
        mode: String
    ): AiResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY") || apiKey.length < 10) {
            return@withContext AiResult.Error(
                "Gemini API key is missing or not configured in Secrets. You can switch to Ollama or Offline Knowledge in Settings."
            )
        }

        val fullPrompt = buildEngineeringContext(question, prompt, userAnswer, mode)

        try {
            val req = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))
                )
            )
            val response = GeminiClient.api.generateContent(apiKey, req)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!candidateText.isNullOrBlank()) {
                AiResult.Success(candidateText.trim(), "Gemini 3.5 Flash")
            } else {
                AiResult.Error("Gemini response was empty or blocked by safety filters.")
            }
        } catch (e: Exception) {
            AiResult.Error("Gemini API call failed: ${e.localizedMessage ?: "Network error"}")
        }
    }
}

// -------------------------------------------------------------
// Offline Curated Knowledge Base Provider (Zero Network Required)
// -------------------------------------------------------------

class OfflineCuratedTutorProvider : AiProvider {

    override val type: AiProviderType = AiProviderType.OFFLINE_KNOWLEDGE
    override val displayName: String = "Verified Offline Knowledge Base"

    override suspend fun generateResponse(
        prompt: String,
        question: QuestionEntity?,
        userAnswer: String?,
        mode: String
    ): AiResult = withContext(Dispatchers.Default) {
        if (question == null) {
            val advice = """
                ### 🎓 GATE CSE Preparation Knowledge Base
                
                **Inquiry:** "$prompt"
                
                **Core Strategy:**
                • Master high-weightage subjects first: Operating Systems, Theory of Computation, Algorithms, Computer Networks.
                • Thoroughly study standard references: Silberschatz (OS), Peter Linz (TOC), CLRS (Algorithms), Tanenbaum (CN).
                • Practice past 20 years GATE PYQs and categorize every error into your Error Notebook.
            """.trimIndent()
            return@withContext AiResult.Success(advice, "GATE CSE Textbook Knowledge Base")
        }

        val isCorrect = userAnswer != null && userAnswer.trim().equals(question.correctAnswers.trim(), ignoreCase = true)
        val modeTitle = when (mode) {
            "HINT" -> "💡 Hint & Clue"
            "DEEP" -> "🔬 Deep Concept Breakdown"
            "QUICK" -> "⚡ Quick Summary"
            else -> "🎯 GATE Verified Solution & Derivation"
        }

        val sb = StringBuilder()
        sb.append("### $modeTitle\n\n")
        sb.append("**Concept (${question.topicId}):**\n")
        sb.append("${question.shortExplanation}\n\n")

        if (userAnswer != null) {
            if (isCorrect) {
                sb.append("✅ **Your choice ($userAnswer) is correct!**\n\n")
            } else {
                sb.append("⚠️ **Your choice ($userAnswer) was incorrect.** Correct answer is: `${question.correctAnswers}`.\n\n")
            }
        }

        sb.append("**Step-by-step Solution:**\n")
        sb.append("${question.detailedSolution}\n\n")

        if (!question.shortcutTrick.isNullOrBlank()) {
            sb.append("🚀 **Exam Shortcut / Observation:**\n${question.shortcutTrick}\n\n")
        }
        if (!question.commonTrap.isNullOrBlank()) {
            sb.append("⚠️ **Common Exam Trap:**\n${question.commonTrap}\n\n")
        }
        if (!question.keyFormula.isNullOrBlank()) {
            sb.append("📐 **Key Formula / Theorem:**\n`${question.keyFormula}`\n")
        }

        AiResult.Success(sb.toString().trim(), "Verified Solution DB")
    }
}

// -------------------------------------------------------------
// Helper Context Builder
// -------------------------------------------------------------

private fun buildEngineeringContext(
    question: QuestionEntity?,
    prompt: String,
    userAnswer: String?,
    mode: String
): String {
    val builder = StringBuilder()
    builder.append("Context: GATE Computer Science & Information Technology Exam Preparation.\n")
    builder.append("Instruction Mode: $mode.\n")

    if (question != null) {
        builder.append("\nQuestion Metadata:\n")
        builder.append("Subject: ${question.subjectId}, Topic: ${question.topicId}\n")
        builder.append("Type: ${question.questionType}, Marks: ${question.marks}\n")
        builder.append("Question Text:\n${question.questionText}\n")
        if (!question.optionA.isNullOrBlank()) {
            builder.append("Options:\n")
            builder.append("A: ${question.optionA}\n")
            builder.append("B: ${question.optionB}\n")
            builder.append("C: ${question.optionC}\n")
            builder.append("D: ${question.optionD}\n")
        }
        builder.append("Official Correct Answer: ${question.correctAnswers}\n")
        builder.append("Official Solution:\n${question.detailedSolution}\n")
        if (!question.shortcutTrick.isNullOrBlank()) builder.append("Shortcut: ${question.shortcutTrick}\n")
        if (!question.commonTrap.isNullOrBlank()) builder.append("Common Trap: ${question.commonTrap}\n")
        if (!userAnswer.isNullOrBlank()) builder.append("Student Selected Answer: $userAnswer\n")
    }

    builder.append("\nStudent Question/Request:\n$prompt\n")
    builder.append("\nPlease provide a pedagogically sound, mathematically rigorous explanation.")
    return builder.toString()
}
