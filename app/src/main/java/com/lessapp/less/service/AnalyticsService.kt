package com.lessapp.less.service

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lessapp.less.data.model.Lang
import com.lessapp.less.data.repository.dataStore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Anonymous analytics service - no user tracking.
 * Aggregates events locally and sends to Supabase in batches.
 */
object AnalyticsService {

    private const val TAG = "AnalyticsService"
    private const val SUPABASE_URL = "https://aqbzyeepanngwgbjwoqy.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFxYnp5ZWVwYW5uZ3dnYmp3b3F5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc3Mjc2NDUsImV4cCI6MjA4MzMwMzY0NX0.FAWlbkmKCgZaYx666Egtl-gu_TQoccl_PIKa8Td1SZU"

    private val PENDING_EVENTS_KEY = stringPreferencesKey("pending_analytics_v1")

    private val json = Json { ignoreUnknownKeys = true }

    private val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer(json)
        install(Postgrest)
    }

    // In-memory cache of pending events
    private val pendingEvents = mutableMapOf<String, Int>()

    enum class EventType(val value: String) {
        VIEW("view"),
        LEARNED("learned"),
        UNUSEFUL("unuseful"),
        FAVORITE("favorite"),
        REVIEW("review")
    }

    @Serializable
    private data class AnalyticsParams(
        val p_card_id: String,
        val p_event_type: String,
        val p_lang: String,
        val p_count: Int
    )

    /**
     * Track an event locally (will be sent on flush)
     */
    fun track(cardId: String, event: EventType, lang: Lang) {
        val key = "$cardId|${event.value}|${lang.code}"
        synchronized(pendingEvents) {
            pendingEvents[key] = (pendingEvents[key] ?: 0) + 1
        }
        Log.d(TAG, "Tracked: $key")
    }

    /**
     * Load pending events from storage
     */
    suspend fun loadPending(context: Context) {
        try {
            val prefs = context.dataStore.data.first()
            val jsonStr = prefs[PENDING_EVENTS_KEY] ?: return
            val loaded = json.decodeFromString<Map<String, Int>>(jsonStr)
            synchronized(pendingEvents) {
                loaded.forEach { (key, count) ->
                    pendingEvents[key] = (pendingEvents[key] ?: 0) + count
                }
            }
            Log.d(TAG, "Loaded ${loaded.size} pending events")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load pending events", e)
        }
    }

    /**
     * Save pending events to storage
     */
    private suspend fun savePending(context: Context) {
        try {
            val toSave = synchronized(pendingEvents) { pendingEvents.toMap() }
            context.dataStore.edit { prefs ->
                prefs[PENDING_EVENTS_KEY] = json.encodeToString(toSave)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save pending events", e)
        }
    }

    /**
     * Flush all pending events to Supabase
     */
    suspend fun flush(context: Context) {
        // Get and clear pending events
        val eventsToSend: Map<String, Int>
        synchronized(pendingEvents) {
            eventsToSend = pendingEvents.toMap()
            pendingEvents.clear()
        }

        if (eventsToSend.isEmpty()) {
            Log.d(TAG, "No events to flush")
            return
        }

        Log.d(TAG, "Flushing ${eventsToSend.size} events")

        val failedEvents = mutableMapOf<String, Int>()

        for ((key, count) in eventsToSend) {
            val parts = key.split("|")
            if (parts.size != 3) continue

            val cardId = parts[0]
            val eventType = parts[1]
            val lang = parts[2]

            try {
                client.postgrest.rpc(
                    function = "upsert_analytics",
                    parameters = AnalyticsParams(
                        p_card_id = cardId,
                        p_event_type = eventType,
                        p_lang = lang,
                        p_count = count
                    )
                )
                Log.d(TAG, "Sent: $key x $count")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send $key: ${e.message}")
                // Re-queue failed events
                failedEvents[key] = count
            }
        }

        // Re-add failed events
        if (failedEvents.isNotEmpty()) {
            synchronized(pendingEvents) {
                failedEvents.forEach { (key, count) ->
                    pendingEvents[key] = (pendingEvents[key] ?: 0) + count
                }
            }
        }

        // Save remaining pending events
        savePending(context)
    }
}
