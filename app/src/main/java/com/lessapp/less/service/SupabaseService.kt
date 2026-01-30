package com.lessapp.less.service

import android.util.Log
import com.lessapp.less.data.model.Card
import com.lessapp.less.data.model.Lang
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object SupabaseService {

    private const val TAG = "SupabaseService"
    private const val SUPABASE_URL = "https://aqbzyeepanngwgbjwoqy.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFxYnp5ZWVwYW5uZ3dnYmp3b3F5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc3Mjc2NDUsImV4cCI6MjA4MzMwMzY0NX0.FAWlbkmKCgZaYx666Egtl-gu_TQoccl_PIKa8Td1SZU"

    var lastError: String? = null
        private set

    private val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    @Serializable
    private data class CardTranslation(
        val lang: String,
        val title: String? = null,
        val hook: String? = null,
        val bullets: JsonElement? = null,
        val why: String? = null
    ) {
        fun getBulletsList(): List<String>? {
            return try {
                bullets?.jsonArray?.map { it.jsonPrimitive.content }
            } catch (e: Exception) {
                null
            }
        }
    }

    @Serializable
    private data class CardResponse(
        val id: String,
        val topic: String? = null,
        val difficulty: Int? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("is_published") val isPublished: Boolean? = null,
        @SerialName("card_translations") val translations: List<CardTranslation>? = null
    )

    suspend fun fetchCards(lang: Lang): List<Card> {
        lastError = null
        return try {
            Log.d(TAG, "Fetching cards for lang=${lang.code}")
            Log.d(TAG, "URL: $SUPABASE_URL")

            val response = client.from("cards")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, card_translations(*)")) {
                    filter {
                        eq("is_published", true)
                    }
                }
                .decodeList<CardResponse>()

            Log.d(TAG, "Got ${response.size} cards from API")

            if (response.isEmpty()) {
                Log.w(TAG, "No cards returned from API")
                lastError = "No cards returned from API"
                return emptyList()
            }

            val cards = response.mapNotNull { cardResponse ->
                Log.d(TAG, "Processing card ${cardResponse.id}, translations: ${cardResponse.translations?.size ?: 0}")

                val translations = cardResponse.translations
                if (translations.isNullOrEmpty()) {
                    Log.w(TAG, "Card ${cardResponse.id} has no translations")
                    return@mapNotNull null
                }

                // Find translation for requested language
                val translation = translations.find { it.lang == lang.code }
                    ?: translations.find { it.lang == "en" }
                    ?: translations.firstOrNull()

                if (translation == null) {
                    Log.w(TAG, "Card ${cardResponse.id} has no usable translation")
                    return@mapNotNull null
                }

                // Fallback to English for empty values
                val enTranslation = translations.find { it.lang == "en" }

                val title = translation.title?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.title?.takeIf { it.isNotBlank() }
                val hook = translation.hook?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.hook?.takeIf { it.isNotBlank() }
                val bullets = translation.getBulletsList()?.takeIf { it.isNotEmpty() }
                    ?: enTranslation?.getBulletsList()?.takeIf { it.isNotEmpty() }
                val why = translation.why?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.why?.takeIf { it.isNotBlank() }

                Card.sanitize(
                    id = cardResponse.id,
                    topic = cardResponse.topic ?: "general",
                    difficulty = cardResponse.difficulty ?: 1,
                    createdAt = cardResponse.createdAt ?: "",
                    title = title,
                    hook = hook,
                    bullets = bullets,
                    why = why
                )
            }.sortedByDescending { it.createdAt }

            Log.d(TAG, "Returning ${cards.size} sanitized cards")

            if (cards.isEmpty() && response.isNotEmpty()) {
                lastError = "Cards found but none passed sanitization"
            }

            cards
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching cards: ${e.message}", e)
            lastError = e.message ?: "Unknown error"
            emptyList()
        }
    }
}
