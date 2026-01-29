package com.lessapp.less.service

import com.lessapp.less.data.model.Card
import com.lessapp.less.data.model.Lang
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object SupabaseService {

    private const val SUPABASE_URL = "https://aqbzyeepanngwgbjwoqy.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFxYnp5ZWVwYW5uZ3dnYmp3b3F5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY2OTUwOTAsImV4cCI6MjA1MjI3MTA5MH0.QJKV3HXZzHM21cRslbMIJuEz_bSBzYeSHidjWzTaVLY"

    private val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    @Serializable
    private data class CardTranslation(
        val lang: String,
        val title: String?,
        val hook: String?,
        val bullets: List<String>?,
        val why: String?
    )

    @Serializable
    private data class CardResponse(
        val id: String,
        val topic: String,
        val difficulty: Int,
        @SerialName("created_at") val createdAt: String,
        @SerialName("is_published") val isPublished: Boolean,
        @SerialName("card_translations") val translations: List<CardTranslation>
    )

    suspend fun fetchCards(lang: Lang): List<Card> {
        return try {
            val response = client.from("cards")
                .select {
                    filter {
                        eq("is_published", true)
                    }
                }
                .decodeList<CardResponse>()

            response.mapNotNull { cardResponse ->
                // Find translation for requested language
                val translation = cardResponse.translations.find { it.lang == lang.code }
                    ?: cardResponse.translations.find { it.lang == "en" }
                    ?: cardResponse.translations.firstOrNull()

                if (translation == null) return@mapNotNull null

                // Fallback to English for empty values
                val enTranslation = cardResponse.translations.find { it.lang == "en" }

                val title = translation.title?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.title?.takeIf { it.isNotBlank() }
                val hook = translation.hook?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.hook?.takeIf { it.isNotBlank() }
                val bullets = translation.bullets?.takeIf { it.isNotEmpty() }
                    ?: enTranslation?.bullets?.takeIf { it.isNotEmpty() }
                val why = translation.why?.takeIf { it.isNotBlank() }
                    ?: enTranslation?.why?.takeIf { it.isNotBlank() }

                Card.sanitize(
                    id = cardResponse.id,
                    topic = cardResponse.topic,
                    difficulty = cardResponse.difficulty,
                    createdAt = cardResponse.createdAt,
                    title = title,
                    hook = hook,
                    bullets = bullets,
                    why = why
                )
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
