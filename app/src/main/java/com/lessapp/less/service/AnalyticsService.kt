package com.lessapp.less.service

import android.content.Context
import android.util.Log
import com.lessapp.less.BuildConfig
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lessapp.less.data.model.Lang
import com.lessapp.less.data.repository.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Anonymous analytics service - no user tracking.
 * Aggregates events locally and sends to Supabase in batches.
 */
object AnalyticsService {

    private const val TAG = "AnalyticsService"
    // Loaded from secrets.properties via BuildConfig
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    private val PENDING_EVENTS_KEY = stringPreferencesKey("pending_analytics_v1")

    private val json = Json { ignoreUnknownKeys = true }

    // In-memory cache of pending events
    private val pendingEvents = mutableMapOf<String, Int>()

    enum class EventType(val value: String) {
        VIEW("view"),
        LEARNED("learned"),
        UNUSEFUL("unuseful"),
        FAVORITE("favorite"),
        REVIEW("review")
    }

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
                sendToSupabase(cardId, eventType, lang, count)
                Log.d(TAG, "Sent: $key x $count")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send $key: ${e.message}")
                failedEvents[key] = count
            }
        }

        if (failedEvents.isNotEmpty()) {
            synchronized(pendingEvents) {
                failedEvents.forEach { (key, count) ->
                    pendingEvents[key] = (pendingEvents[key] ?: 0) + count
                }
            }
        }

        savePending(context)
    }

    /**
     * Send to Supabase RPC via HTTP
     */
    private suspend fun sendToSupabase(cardId: String, eventType: String, lang: String, count: Int) {
        withContext(Dispatchers.IO) {
            val url = URL("$SUPABASE_URL/rest/v1/rpc/upsert_analytics")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.doOutput = true

                val body = """{"p_card_id":"$cardId","p_event_type":"$eventType","p_lang":"$lang","p_count":$count}"""
                connection.outputStream.use { os ->
                    os.write(body.toByteArray())
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw Exception("HTTP $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}
