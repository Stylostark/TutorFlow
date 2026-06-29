package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- API Request / Response Models ---

data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiResponseFormatText(
    val mimeType: String
)

data class GeminiResponseFormat(
    val responseFormatText: GeminiResponseFormatText? = null
)

data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiCandidate(
    val content: GeminiContent
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

// --- Retrofit Interface ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            ""
        } else {
            key
        }
    }

    /**
     * Section 8.1 - Smart Subject Suggestion:
     * Gemini auto-suggests relevant subjects list for a Class and Board
     */
    suspend fun suggestSubjects(currentClass: String, board: String): List<String> {
        val apiKey = getApiKey()
        val prompt = "List exactly 6-8 relevant academic subjects for a '$currentClass' student studying under the '$board' board in India. Return ONLY a plain JSON string array of subject names, like [\"Mathematics\", \"Science\"]. Do not include markdown or other text."

        if (apiKey.isEmpty()) {
            // Simulated response when API key is not configured
            Log.d(TAG, "No API key; returning mocked subjects for $currentClass ($board)")
            return when (currentClass) {
                "Class 11", "Class 12" -> listOf("Physics", "Chemistry", "Mathematics", "Biology", "English", "Computer Science")
                "Class 9", "Class 10" -> listOf("Mathematics", "Science", "Social Science", "English", "Hindi", "Information Technology")
                else -> listOf("Mathematics", "Science", "English", "Social Studies", "Hindi", "General Knowledge")
            }
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    responseMimeType = "application/json"
                )
            )

            val response = api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            Log.d(TAG, "Gemini Response: $jsonText")

            if (!jsonText.isNullOrEmpty()) {
                // Parse the JSON array using Moshi
                val adapter = moshi.adapter(List::class.java)
                val parsed = adapter.fromJson(jsonText) as? List<*>
                parsed?.mapNotNull { it?.toString() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating subject suggestions", e)
            listOf("Mathematics", "Science", "English", "Social Studies")
        }
    }

    /**
     * Section 8.2 - Admin Video Description Generator:
     * Generates a 2-sentence class description based on the title.
     */
    suspend fun generateVideoDescription(title: String): String {
        val apiKey = getApiKey()
        val prompt = "Write a highly professional and engaging 2-sentence class description for a tuition video titled: '$title'"

        if (apiKey.isEmpty()) {
            Log.d(TAG, "No API key; returning mocked video description")
            return "This comprehensive lecture covers the core principles of $title with detailed explanations and step-by-step examples. Perfect for school exams and competitive test preparation."
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f)
            )

            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "No description generated."
        } catch (e: Exception) {
            Log.e(TAG, "Error generating video description", e)
            "Learn and master the concepts of $title with our expert tutor in this easy-to-follow video lesson."
        }
    }

    /**
     * Section 8.3 - Student Doubt Assistant Chat:
     * A simple tuition teacher agent that responds encouragingly in Hindi/English.
     */
    suspend fun askDoubt(chatHistory: List<GeminiContent>): String {
        val apiKey = getApiKey()
        val systemPrompt = "You are a helpful tuition teacher for Indian school students (Class 1–12). Answer in simple Hindi or English as needed. Be concise, extremely clear, and highly encouraging. Explain mathematical or scientific concepts step-by-step."

        if (apiKey.isEmpty()) {
            Log.d(TAG, "No API key; returning mocked doubt response")
            val lastMsg = chatHistory.lastOrNull()?.parts?.firstOrNull()?.text ?: ""
            return "Aapka sawal bohot accha hai! Let's understand this simply. Regarding '$lastMsg', it is a very key concept. Think of it step-by-step. Let me know if you need any specific calculation or diagram. Hum milkar isey aasan banayenge! 👍"
        }

        return try {
            val request = GeminiRequest(
                contents = chatHistory,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f)
            )

            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Mujhe maaf kijiye, main abhi iska jawab nahi de pa raha hoon. Kripya apna sawal fir se pucho."
        } catch (e: Exception) {
            Log.e(TAG, "Error asking doubt to Gemini", e)
            "Technical issue ki wajah se response nahi mil paya. Please check your internet connection or try again."
        }
    }
}
