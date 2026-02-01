package com.lessapp.less.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lessapp.less.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "less_prefs")

// JSON serializer
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

// MARK: - Preference Keys
object PreferenceKeys {
    val LEARNED_IDS = stringSetPreferencesKey("learned_cards_v1")
    val UNUSEFUL_IDS = stringSetPreferencesKey("unuseful_cards_v1")
    val REVIEWS = stringPreferencesKey("reviews_v1")
    val SETTINGS = stringPreferencesKey("settings_v1")
    val SUPPORT_INJECTED_DAY = stringPreferencesKey("support_injected_day_v1")
    val SUPPORT_USED_DAY = stringPreferencesKey("support_used_day_v1")
    val FEEDBACK_QUEUE = stringPreferencesKey("feedback_queue_v1")

    // Daily tracking
    val DAILY_OPENING_SEEN = stringPreferencesKey("daily_opening_seen_v1")
    val DAILY_STARTED_AT = stringPreferencesKey("daily_started_at_v1")
    val DAILY_COMPLETED_AT = stringPreferencesKey("daily_completed_at_v1")
    val DAILY_CARDS_VIEWED = stringPreferencesKey("daily_cards_viewed_v1")

    fun cardsCacheKey(lang: Lang) = stringPreferencesKey("cards_cache_${lang.code}")
    fun seenCardsKey(lang: Lang) = stringSetPreferencesKey("seen_cards_${lang.code}")
}

// MARK: - Learned Store
class LearnedRepository(private val context: Context) {

    val learnedIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.LEARNED_IDS] ?: emptySet()
    }

    suspend fun isLearned(cardId: String): Boolean {
        return context.dataStore.data.first()[PreferenceKeys.LEARNED_IDS]?.contains(cardId) ?: false
    }

    suspend fun toggle(cardId: String): Boolean {
        val current = context.dataStore.data.first()[PreferenceKeys.LEARNED_IDS] ?: emptySet()
        val isNowLearned = !current.contains(cardId)
        val newSet = if (isNowLearned) current + cardId else current - cardId
        context.dataStore.edit { it[PreferenceKeys.LEARNED_IDS] = newSet }
        return isNowLearned
    }

    suspend fun count(): Int {
        return context.dataStore.data.first()[PreferenceKeys.LEARNED_IDS]?.size ?: 0
    }
}

// MARK: - Unuseful Store
class UnusefulRepository(private val context: Context) {

    val unusefulIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.UNUSEFUL_IDS] ?: emptySet()
    }

    suspend fun isUnuseful(cardId: String): Boolean {
        return context.dataStore.data.first()[PreferenceKeys.UNUSEFUL_IDS]?.contains(cardId) ?: false
    }

    suspend fun toggle(cardId: String): Boolean {
        val current = context.dataStore.data.first()[PreferenceKeys.UNUSEFUL_IDS] ?: emptySet()
        val isNowUnuseful = !current.contains(cardId)
        val newSet = if (isNowUnuseful) current + cardId else current - cardId
        context.dataStore.edit { it[PreferenceKeys.UNUSEFUL_IDS] = newSet }
        return isNowUnuseful
    }
}

// MARK: - Review Store
class ReviewRepository(private val context: Context) {

    suspend fun getReviews(): Map<String, ReviewItem> {
        val jsonStr = context.dataStore.data.first()[PreferenceKeys.REVIEWS] ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, ReviewItem>>(jsonStr)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun saveReviews(reviews: Map<String, ReviewItem>) {
        context.dataStore.edit {
            it[PreferenceKeys.REVIEWS] = json.encodeToString(reviews)
        }
    }

    suspend fun isInReview(cardId: String): Boolean {
        return getReviews().containsKey(cardId)
    }

    suspend fun isDue(cardId: String): Boolean {
        return getReviews()[cardId]?.isDue ?: false
    }

    suspend fun get(cardId: String): ReviewItem? {
        return getReviews()[cardId]
    }

    suspend fun add(cardId: String) {
        val reviews = getReviews().toMutableMap()
        reviews[cardId] = ReviewItem.create()
        saveReviews(reviews)
    }

    suspend fun remove(cardId: String) {
        val reviews = getReviews().toMutableMap()
        reviews.remove(cardId)
        saveReviews(reviews)
    }

    suspend fun toggle(cardId: String): Boolean {
        val reviews = getReviews().toMutableMap()
        val isNowInReview = !reviews.containsKey(cardId)
        if (isNowInReview) {
            reviews[cardId] = ReviewItem.create()
        } else {
            reviews.remove(cardId)
        }
        saveReviews(reviews)
        return isNowInReview
    }

    suspend fun markSeen(cardId: String, durationMs: Int) {
        val reviews = getReviews().toMutableMap()
        val item = reviews[cardId] ?: return

        val updated = if (durationMs >= 6500) {
            item.advance()
        } else {
            item.reschedule(6)
        }
        reviews[cardId] = updated
        saveReviews(reviews)
    }
}

// MARK: - Settings Store
class SettingsRepository(private val context: Context) {

    val settings: Flow<UISettings> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[PreferenceKeys.SETTINGS]
        if (jsonStr != null) {
            try {
                json.decodeFromString<UISettings>(jsonStr)
            } catch (e: Exception) {
                UISettings()
            }
        } else {
            UISettings()
        }
    }

    suspend fun getSettings(): UISettings {
        return settings.first()
    }

    suspend fun save(settings: UISettings) {
        context.dataStore.edit {
            it[PreferenceKeys.SETTINGS] = json.encodeToString(settings)
        }
    }

    suspend fun updateLang(lang: Lang) {
        val current = getSettings()
        save(current.copy(lang = lang.code))
    }

    suspend fun updateListMode(mode: ListMode) {
        val current = getSettings()
        save(current.copy(listMode = mode.value))
    }

    suspend fun toggleTextScale() {
        val current = getSettings()
        val newScale = if (TextScale.fromValue(current.textScale) == TextScale.NORMAL) TextScale.LARGE else TextScale.NORMAL
        save(current.copy(textScale = newScale.value))
    }

    suspend fun toggleFocusMode() {
        val current = getSettings()
        save(current.copy(focusMode = !current.focusMode))
    }

    suspend fun toggleContinuousReading() {
        val current = getSettings()
        save(current.copy(continuousReading = !current.continuousReading))
    }

    suspend fun toggleGestures() {
        val current = getSettings()
        save(current.copy(gesturesEnabled = !current.gesturesEnabled))
    }

    suspend fun toggleDarkMode() {
        val current = getSettings()
        save(current.copy(darkMode = !current.darkMode))
    }

    suspend fun markHelpSeen() {
        val current = getSettings()
        save(current.copy(helpSeen = true))
    }
}

// MARK: - Cards Cache Store
class CardsCacheRepository(private val context: Context) {

    suspend fun load(lang: Lang): CardsCache? {
        val jsonStr = context.dataStore.data.first()[PreferenceKeys.cardsCacheKey(lang)] ?: return null
        return try {
            json.decodeFromString<CardsCache>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(lang: Lang, cards: List<Card>) {
        val cache = CardsCache(
            savedAt = Instant.now().toEpochMilli(),
            cards = cards
        )
        context.dataStore.edit {
            it[PreferenceKeys.cardsCacheKey(lang)] = json.encodeToString(cache)
        }
    }

    suspend fun clear(lang: Lang) {
        context.dataStore.edit {
            it.remove(PreferenceKeys.cardsCacheKey(lang))
        }
    }
}

// MARK: - Seen Cards Store
class SeenCardsRepository(private val context: Context) {

    suspend fun isSeen(cardId: String, lang: Lang): Boolean {
        return context.dataStore.data.first()[PreferenceKeys.seenCardsKey(lang)]?.contains(cardId) ?: false
    }

    suspend fun markSeen(cardId: String, lang: Lang) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.seenCardsKey(lang)] ?: emptySet()
            prefs[PreferenceKeys.seenCardsKey(lang)] = current + cardId
        }
    }

    suspend fun markSeen(cardIds: List<String>, lang: Lang) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.seenCardsKey(lang)] ?: emptySet()
            prefs[PreferenceKeys.seenCardsKey(lang)] = current + cardIds.toSet()
        }
    }
}

// MARK: - Support Tracker
class SupportTracker(private val context: Context) {

    private val todayString: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    suspend fun wasInjectedToday(): Boolean {
        val day = context.dataStore.data.first()[PreferenceKeys.SUPPORT_INJECTED_DAY]
        return day == todayString
    }

    suspend fun wasUsedToday(): Boolean {
        val day = context.dataStore.data.first()[PreferenceKeys.SUPPORT_USED_DAY]
        return day == todayString
    }

    suspend fun markInjected() {
        context.dataStore.edit {
            it[PreferenceKeys.SUPPORT_INJECTED_DAY] = todayString
        }
    }

    suspend fun markUsed() {
        context.dataStore.edit {
            it[PreferenceKeys.SUPPORT_USED_DAY] = todayString
        }
    }
}

// MARK: - Feedback Queue
class FeedbackQueue(private val context: Context) {

    suspend fun enqueue(item: FeedbackItem) {
        val current = list().toMutableList()
        current.add(0, item)
        context.dataStore.edit {
            it[PreferenceKeys.FEEDBACK_QUEUE] = json.encodeToString(current)
        }
    }

    suspend fun list(): List<FeedbackItem> {
        val jsonStr = context.dataStore.data.first()[PreferenceKeys.FEEDBACK_QUEUE] ?: return emptyList()
        return try {
            json.decodeFromString<List<FeedbackItem>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(PreferenceKeys.FEEDBACK_QUEUE)
        }
    }
}

// MARK: - Daily Store
class DailyRepository(private val context: Context) {

    private val todayStringUtc: String
        get() = LocalDate.now(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)

    // MARK: - Opening Card Tracking
    suspend fun hasSeenOpeningToday(): Boolean {
        val day = context.dataStore.data.first()[PreferenceKeys.DAILY_OPENING_SEEN]
        return day == todayStringUtc
    }

    suspend fun markOpeningSeen() {
        context.dataStore.edit {
            it[PreferenceKeys.DAILY_OPENING_SEEN] = todayStringUtc
        }
    }

    // MARK: - Daily Session Tracking
    suspend fun getDailyStartedToday(): String? {
        val key = stringPreferencesKey("daily_started_at_$todayStringUtc")
        return context.dataStore.data.first()[key]
    }

    suspend fun markDailyStarted() {
        val key = stringPreferencesKey("daily_started_at_$todayStringUtc")
        val now = Instant.now().toString()
        context.dataStore.edit {
            it[key] = now
        }
    }

    suspend fun getDailyCompletedToday(): String? {
        val key = stringPreferencesKey("daily_completed_at_$todayStringUtc")
        return context.dataStore.data.first()[key]
    }

    suspend fun markDailyCompleted() {
        val key = stringPreferencesKey("daily_completed_at_$todayStringUtc")
        val now = Instant.now().toString()
        context.dataStore.edit {
            it[key] = now
        }
    }

    suspend fun isCompleteToday(): Boolean {
        return getDailyCompletedToday() != null
    }

    // MARK: - Cards Viewed in Daily Session
    suspend fun getViewedCardsToday(): Set<String> {
        val key = stringPreferencesKey("daily_cards_viewed_$todayStringUtc")
        val jsonStr = context.dataStore.data.first()[key] ?: return emptySet()
        return try {
            json.decodeFromString<Set<String>>(jsonStr)
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun markCardViewed(cardId: String) {
        val key = stringPreferencesKey("daily_cards_viewed_$todayStringUtc")
        val current = getViewedCardsToday().toMutableSet()
        current.add(cardId)
        context.dataStore.edit {
            it[key] = json.encodeToString(current)
        }
    }

    suspend fun viewedCount(): Int {
        return getViewedCardsToday().size
    }
}

// MARK: - Streak Repository
class StreakRepository(private val context: Context) {

    private val streakKey = stringPreferencesKey("streak_count_v1")
    private val lastCompletionKey = stringPreferencesKey("streak_last_completion_v1")

    private val todayStringUtc: String
        get() = LocalDate.now(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val yesterdayStringUtc: String
        get() = LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    suspend fun getCurrentStreak(): Int {
        return context.dataStore.data.first()[streakKey] ?: 0
    }

    private suspend fun getLastCompletionDate(): String? {
        return context.dataStore.data.first()[lastCompletionKey]
    }

    suspend fun recordCompletion() {
        val lastDate = getLastCompletionDate()
        val today = todayStringUtc
        val yesterday = yesterdayStringUtc

        val newStreak = when {
            lastDate == today -> {
                // Already completed today - do nothing
                return
            }
            lastDate == yesterday -> {
                // Completed yesterday - increment streak
                getCurrentStreak() + 1
            }
            else -> {
                // Streak broken or first completion - start at 1
                1
            }
        }

        context.dataStore.edit {
            it[streakKey] = newStreak
            it[lastCompletionKey] = today
        }
    }

    suspend fun checkStreakValidity(): Int {
        val lastDate = getLastCompletionDate() ?: return 0
        val today = todayStringUtc
        val yesterday = yesterdayStringUtc

        // If last completion was not today or yesterday, streak is broken
        return if (lastDate != today && lastDate != yesterday) {
            context.dataStore.edit {
                it[streakKey] = 0
            }
            0
        } else {
            getCurrentStreak()
        }
    }
}
