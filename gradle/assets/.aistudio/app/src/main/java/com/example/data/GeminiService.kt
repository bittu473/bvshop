package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Query Gemini model
    suspend fun queryGemini(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder! Please configure it in AI Studio Secrets.")
            return@withContext getOfflineResponse(prompt)
        }

        try {
            // Build the JSON payload using native org.json for 100% compile safety
            val payload = JSONObject()
            
            // Add contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            payload.put("contents", contentsArray)

            // Add system instruction if present
            if (systemInstruction != null) {
                val systemObj = JSONObject()
                val systemPartsArray = JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemObj.put("parts", systemPartsArray)
                payload.put("systemInstruction", systemObj)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful response from Gemini API: Code ${response.code}, Body: $errBody")
                    return@withContext getOfflineResponse(prompt)
                }

                val responseBody = response.body?.string() ?: return@withContext "No response body received."
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "Empty part text.")
                        }
                    }
                }
                "Could not find valid text in Gemini response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            getOfflineResponse(prompt)
        }
    }

    // High quality offline fallback generator for offline testing/no-key states
    private fun getOfflineResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("recommend") || lower.contains("bought together") || lower.contains("related") -> {
                "✨ **ShopNest AI Smart Choice** ✨\n\n" +
                        "Based on items commonly bought with your selected product, here are our recommended pairings:\n\n" +
                        "1. **Luxury Gold Chronograph & Premium Leather Wallet Guard** (Save 15% on bundle)\n" +
                        "2. **Symphony ANC Headphones & Aura Smart Table Dock**\n" +
                        "3. **Satin Silk Loungewear & Rose Quartz Face Mist**\n\n" +
                        "*Our matching algorithm indicates these items create a unified luxury experience.*"
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("assist") || lower.contains("shopnest") -> {
                "Hello, I am your **ShopNest AI Concierge**! \n\n" +
                        "I can recommend premium styles, help you navigate categories, check compatibility, or explain exclusive wallet/cashback benefits.\n\n" +
                        "How can I help elevate your shopping experience today?"
            }
            lower.contains("coupon") || lower.contains("discount") || lower.contains("cashback") -> {
                "To get exclusive cashback discounts, use coupon code **LUX20** for 10% instant wallet credit on luxury orders, or **WELCOME50** if you are checking out your first item!"
            }
            else -> {
                "ShopNest Concierge AI recommends combining our curated **Oud Eclipse Perfume** with the **Classic Gold Chronograph** for an ultimate high-society appearance.\n\n" +
                        "Is there a particular color, sizing variant, or category you would like me to analyze for you?"
            }
        }
    }
}
