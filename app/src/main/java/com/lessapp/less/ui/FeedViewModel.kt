package com.lessapp.less.ui

import android.app.Activity
import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lessapp.less.data.model.*
import com.lessapp.less.data.repository.*
import com.lessapp.less.service.AdMobService
import com.lessapp.less.service.AnalyticsService
import com.lessapp.less.service.NotificationService
import com.lessapp.less.service.SupabaseService
import com.lessapp.less.util.L10n
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    // Repositories
    private val learnedRepo = LearnedRepository(application)
    private val unusefulRepo = UnusefulRepository(application)
    private val reviewRepo = ReviewRepository(application)
    private val favoritesRepo = FavoritesRepository(application)
    private val settingsRepo = SettingsRepository(application)
    private val cacheRepo = CardsCacheRepository(application)
    private val seenRepo = SeenCardsRepository(application)
    private val supportTracker = SupportTracker(application)
    private val feedbackQueue = FeedbackQueue(application)
    private val dailyRepo = DailyRepository(application)
    private val streakRepo = StreakRepository(application)

    // Published State
    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showSlowHint = MutableStateFlow(false)
    val showSlowHint: StateFlow<Boolean> = _showSlowHint.asStateFlow()

    // UI State
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _showHelp = MutableStateFlow(false)
    val showHelp: StateFlow<Boolean> = _showHelp.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu: StateFlow<Boolean> = _showMenu.asStateFlow()

    private val _showCardMenu = MutableStateFlow(false)
    val showCardMenu: StateFlow<Boolean> = _showCardMenu.asStateFlow()

    private val _showFeedback = MutableStateFlow(false)
    val showFeedback: StateFlow<Boolean> = _showFeedback.asStateFlow()

    private val _showDonation = MutableStateFlow(false)
    val showDonation: StateFlow<Boolean> = _showDonation.asStateFlow()

    private val _selectedCardId = MutableStateFlow<String?>(null)
    val selectedCardId: StateFlow<String?> = _selectedCardId.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(UISettings())
    val settings: StateFlow<UISettings> = _settings.asStateFlow()

    // Undo Toast
    private val _undoToast = MutableStateFlow<UndoToast?>(null)
    val undoToast: StateFlow<UndoToast?> = _undoToast.asStateFlow()

    // Ad error
    private val _adError = MutableStateFlow<String?>(null)
    val adError: StateFlow<String?> = _adError.asStateFlow()

    // Learned count
    private val _learnedCount = MutableStateFlow(0)
    val learnedCount: StateFlow<Int> = _learnedCount.asStateFlow()

    // Session State
    private var allCards: List<Card> = emptyList()
    private var sessionOrderCache: List<String> = emptyList()
    private var uniqueViewedCount = 0
    private var sessionInjected = false
    private var viewedDurations: MutableMap<String, Int> = mutableMapOf()

    // Daily State
    private val _dailyProgress = MutableStateFlow(0)
    val dailyProgress: StateFlow<Int> = _dailyProgress.asStateFlow()

    private val _isDailyComplete = MutableStateFlow(false)
    val isDailyComplete: StateFlow<Boolean> = _isDailyComplete.asStateFlow()

    private val _showDailyCompletion = MutableStateFlow(false)
    val showDailyCompletion: StateFlow<Boolean> = _showDailyCompletion.asStateFlow()

    private val _pagerKey = MutableStateFlow(0)
    val pagerKey: StateFlow<Int> = _pagerKey.asStateFlow()

    private var dailySessionViewedIds: MutableSet<String> = mutableSetOf() // Track views in current session only

    // Streak
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    // Scoring Constants
    private val reviewPinnedBoost = 40.0
    private val reviewDueBoost = 260.0

    val l10n: L10n
        get() = L10n(Lang.fromCode(_settings.value.lang))

    init {
        viewModelScope.launch {
            _settings.value = settingsRepo.getSettings()
            _learnedCount.value = learnedRepo.count()
            _currentStreak.value = streakRepo.checkStreakValidity()

            // Load cards on startup
            loadCards()

            // Observe settings changes
            settingsRepo.settings.collect { newSettings ->
                val langChanged = _settings.value.lang != newSettings.lang
                _settings.value = newSettings
                if (langChanged) {
                    sessionOrderCache = emptyList()
                    uniqueViewedCount = 0
                    sessionInjected = false
                    loadCards()
                }
            }
        }
    }

    // MARK: - Load Cards
    fun loadCards(forceRefresh: Boolean = false) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _showSlowHint.value = false

            // Show slow hint after 6.5s
            val slowJob = launch {
                delay(6500)
                _showSlowHint.value = true
            }

            try {
                val lang = Lang.fromCode(_settings.value.lang)

                // Try cache first
                if (!forceRefresh) {
                    val cache = cacheRepo.load(lang)
                    if (cache != null && cache.isFresh) {
                        allCards = cache.cards
                        rebuildFeed()

                        // Background refresh
                        launch { refreshFromNetwork(lang) }
                        slowJob.cancel()
                        _isLoading.value = false
                        _showSlowHint.value = false
                        return@launch
                    }
                }

                // Fetch from network
                refreshFromNetwork(lang)
            } finally {
                slowJob.cancel()
                _isLoading.value = false
                _showSlowHint.value = false
            }
        }
    }

    private suspend fun refreshFromNetwork(lang: Lang) {
        try {
            val cards = SupabaseService.fetchCards(lang)
            if (cards.isNotEmpty()) {
                allCards = cards
                cacheRepo.save(lang, cards)
                rebuildFeed()
            } else if (allCards.isEmpty()) {
                // Fallback to cache
                val cache = cacheRepo.load(lang)
                if (cache != null) {
                    allCards = cache.cards
                    rebuildFeed()
                }
            }
        } catch (e: Exception) {
            // Fallback to cache on error
            val cache = cacheRepo.load(lang)
            if (cache != null) {
                allCards = cache.cards
                rebuildFeed()
            } else {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadCards(forceRefresh = true)
            _isRefreshing.value = false
        }
    }

    // MARK: - Rebuild Feed
    private suspend fun rebuildFeed() {
        val listMode = ListMode.fromValue(_settings.value.listMode)
        val lang = Lang.fromCode(_settings.value.lang)

        // Handle Daily mode separately
        if (listMode == ListMode.DAILY) {
            _feedItems.value = buildDailyFeed(lang)
            updateDailyProgress()
            return
        }

        // Filter based on list mode
        val filtered = when (listMode) {
            ListMode.FEED -> allCards.filter { card ->
                !learnedRepo.isLearned(card.id) && !unusefulRepo.isUnuseful(card.id)
            }
            ListMode.DAILY -> emptyList() // Handled above
            ListMode.LEARNED -> allCards.filter { learnedRepo.isLearned(it.id) }
            ListMode.UNUSEFUL -> allCards.filter { unusefulRepo.isUnuseful(it.id) }
            ListMode.REVIEW -> allCards.filter { reviewRepo.isInReview(it.id) }
            ListMode.FAVORITES -> allCards.filter { favoritesRepo.isFavorite(it.id) }
        }

        // Score and sort
        val scored = filtered.map { card ->
            var score = computeScore(card)

            // Review boost
            if (reviewRepo.isInReview(card.id)) {
                score += reviewPinnedBoost
                if (reviewRepo.isDue(card.id)) {
                    score += reviewDueBoost
                }
            }

            card to score
        }

        // Use session cache for stability or sort by score
        val sortedCards = if (sessionOrderCache.isEmpty()) {
            val sorted = scored.sortedByDescending { it.second }.map { it.first }
            sessionOrderCache = sorted.map { it.id }
            sorted
        } else {
            val orderMap = sessionOrderCache.withIndex().associate { it.value to it.index }
            scored.sortedBy { orderMap[it.first.id] ?: Int.MAX_VALUE }.map { it.first }
        }

        val items = sortedCards.map { FeedItem.Content(it) }.toMutableList<FeedItem>()

        // Inject system card if conditions met
        if (listMode == ListMode.FEED && shouldInjectSystemCard()) {
            val systemCard = SystemCard.forLang(lang)
            val insertIndex = minOf(8, items.size)
            items.add(insertIndex, FeedItem.System(systemCard))
            supportTracker.markInjected()
            sessionInjected = true
        }

        _feedItems.value = items
        _learnedCount.value = learnedRepo.count()
    }

    private suspend fun computeScore(card: Card): Double {
        if (unusefulRepo.isUnuseful(card.id)) return -1000.0
        if (learnedRepo.isLearned(card.id)) return -500.0

        var score = 0.0
        val viewedMs = viewedDurations[card.id] ?: 0

        if (viewedMs <= 0) {
            score += 300.0 // New cards boost
        } else {
            score += minOf(viewedMs / 10.0, 200.0) // Viewing boost capped
        }

        return score
    }

    private suspend fun shouldInjectSystemCard(): Boolean {
        if (sessionInjected) return false
        if (supportTracker.wasInjectedToday()) return false
        return uniqueViewedCount >= 8
    }

    // MARK: - Daily Mode
    private suspend fun buildDailyFeed(lang: Lang): List<FeedItem> {
        val items = mutableListOf<FeedItem>()

        // 1. Opening card - ALWAYS show in Daily mode
        val opening = OpeningCard.forLang(lang)
        items.add(FeedItem.Opening(opening))

        // 2. Select 4 deterministic content cards (different from feed order)
        val dailyCards = selectDailyCards(allCards, 4, lang)
        for (card in dailyCards) {
            items.add(FeedItem.Content(card))
        }

        // 3. Support card at the end
        val systemCard = SystemCard.forLang(lang)
        items.add(FeedItem.System(systemCard))

        return items
    }

    private suspend fun selectDailyCards(cards: List<Card>, count: Int, lang: Lang): List<Card> {
        if (cards.isEmpty()) return emptyList()

        // Filter out learned/unuseful cards
        val available = cards.filter { card ->
            !learnedRepo.isLearned(card.id) && !unusefulRepo.isUnuseful(card.id)
        }

        if (available.isEmpty()) return emptyList()

        // Sort by ID for consistent base order
        val sorted = available.sortedBy { it.id }

        // Use daily-specific seed (different from feed which doesn't use seeded random)
        val seed = dailySeed(lang)
        val random = java.util.Random(seed)

        // Multiple shuffle passes for more randomness
        val shuffled = sorted.toMutableList()
        repeat(3) {
            shuffled.shuffle(random)
        }

        // Reverse to make it even more different from any natural order
        shuffled.reverse()

        // Take first N cards
        return shuffled.take(count)
    }

    private fun dailySeed(lang: Lang): Long {
        // Create deterministic seed from today's date (UTC) + language
        val dateString = java.time.LocalDate.now(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + "_daily_" + lang.code

        // FNV-1a hash for better distribution
        var hash: Long = -3750763034362895579L // 14695981039346656037 as signed
        for (byte in dateString.toByteArray()) {
            hash = hash xor byte.toLong()
            hash *= 1099511628211L
        }
        return hash
    }

    private suspend fun updateDailyProgress() {
        // Progress is now tracked via dailySessionViewedIds in cardBecameVisible
        // This is kept for compatibility but may be called during mode switches
        val listMode = ListMode.fromValue(_settings.value.listMode)
        if (listMode == ListMode.DAILY) {
            _isDailyComplete.value = dailyRepo.isCompleteToday()
        }
    }

    fun enterDailyMode() {
        viewModelScope.launch {
            settingsRepo.updateListMode(ListMode.DAILY)
            sessionOrderCache = emptyList()

            // Reset session tracking
            dailySessionViewedIds.clear()
            _dailyProgress.value = 0
            _isDailyComplete.value = dailyRepo.isCompleteToday()

            rebuildFeed()
            _currentIndex.value = 0
            _pagerKey.value++ // Force Pager recreation to start at first item
        }
    }

    // MARK: - Card Visibility
    fun cardBecameVisible(cardId: String) {
        viewModelScope.launch {
            val lang = Lang.fromCode(_settings.value.lang)
            val listMode = ListMode.fromValue(_settings.value.listMode)

            // Track view analytics (anonymous)
            if (cardId != OpeningCard.ID && cardId != "system_card") {
                AnalyticsService.track(cardId, AnalyticsService.EventType.VIEW, lang)
            }

            // Daily mode tracking
            if (listMode == ListMode.DAILY) {
                if (cardId == OpeningCard.ID) {
                    dailyRepo.markOpeningSeen()
                    if (dailyRepo.getDailyStartedToday() == null) {
                        dailyRepo.markDailyStarted()
                    }
                } else if (cardId != "system_card") {
                    // Track in current session AND persist
                    if (!dailySessionViewedIds.contains(cardId)) {
                        dailySessionViewedIds.add(cardId)
                        dailyRepo.markCardViewed(cardId)
                    }
                }

                // Update progress based on session views (not persisted count)
                val sessionContentViews = dailySessionViewedIds.size
                _dailyProgress.value = sessionContentViews

                // Check if daily is complete (all 4 content cards viewed in THIS session)
                if (sessionContentViews >= 4 && !_isDailyComplete.value) {
                    dailyRepo.markDailyCompleted()
                    streakRepo.recordCompletion()
                    _currentStreak.value = streakRepo.getCurrentStreak()
                    _isDailyComplete.value = true
                    _showDailyCompletion.value = true // Trigger celebration animation
                }

                return@launch
            }

            // Track unique views (feed mode)
            val item = _feedItems.value.find { it.id == cardId }
            if (item is FeedItem.Content && !seenRepo.isSeen(cardId, lang)) {
                uniqueViewedCount++
                seenRepo.markSeen(cardId, lang)

                if (shouldInjectSystemCard()) {
                    rebuildFeed()
                }
            }
        }
    }

    fun cardViewDuration(cardId: String, durationMs: Int) {
        viewedDurations[cardId] = (viewedDurations[cardId] ?: 0) + durationMs

        viewModelScope.launch {
            if (reviewRepo.isInReview(cardId)) {
                reviewRepo.markSeen(cardId, durationMs)
            }
        }
    }

    // MARK: - Actions
    fun toggleLearned(cardId: String) {
        viewModelScope.launch {
            val isNowLearned = learnedRepo.toggle(cardId)

            // Track analytics
            if (isNowLearned) {
                AnalyticsService.track(cardId, AnalyticsService.EventType.LEARNED, Lang.fromCode(_settings.value.lang))
            }

            // Haptic
            vibrate(if (isNowLearned) VibrationEffect.EFFECT_TICK else VibrationEffect.EFFECT_HEAVY_CLICK)

            // Show undo toast
            if (isNowLearned) {
                showUndoToast(l10n.undoLearned) {
                    viewModelScope.launch {
                        learnedRepo.toggle(cardId)
                        rebuildFeed()
                    }
                }
            }

            rebuildFeed()

            // Auto-next if not continuous reading
            val settings = _settings.value
            if (isNowLearned && !settings.continuousReading && ListMode.fromValue(settings.listMode) == ListMode.FEED) {
                advanceToNext()
            }
        }
    }

    fun toggleUnuseful(cardId: String) {
        viewModelScope.launch {
            val isNowUnuseful = unusefulRepo.toggle(cardId)

            // Track analytics
            if (isNowUnuseful) {
                AnalyticsService.track(cardId, AnalyticsService.EventType.UNUSEFUL, Lang.fromCode(_settings.value.lang))
            }

            vibrate(VibrationEffect.EFFECT_TICK)

            if (isNowUnuseful) {
                showUndoToast(l10n.undoUnuseful) {
                    viewModelScope.launch {
                        unusefulRepo.toggle(cardId)
                        rebuildFeed()
                    }
                }
            }

            rebuildFeed()
        }
    }

    fun toggleReview(cardId: String) {
        viewModelScope.launch {
            val isNowInReview = reviewRepo.toggle(cardId)

            // Track analytics
            if (isNowInReview) {
                AnalyticsService.track(cardId, AnalyticsService.EventType.REVIEW, Lang.fromCode(_settings.value.lang))
            }

            vibrate(VibrationEffect.EFFECT_TICK)

            if (isNowInReview) {
                showUndoToast(l10n.undoReview) {
                    viewModelScope.launch {
                        reviewRepo.toggle(cardId)
                        rebuildFeed()
                    }
                }
            }

            rebuildFeed()
        }
    }

    fun toggleFavorite(cardId: String) {
        viewModelScope.launch {
            val isNowFavorite = favoritesRepo.toggle(cardId)

            // Track analytics
            if (isNowFavorite) {
                AnalyticsService.track(cardId, AnalyticsService.EventType.FAVORITE, Lang.fromCode(_settings.value.lang))
            }

            vibrate(VibrationEffect.EFFECT_TICK)

            // Rebuild if in favorites mode to update the list
            if (ListMode.fromValue(_settings.value.listMode) == ListMode.FAVORITES) {
                rebuildFeed()
            }
        }
    }

    suspend fun isFavorite(cardId: String): Boolean {
        return favoritesRepo.isFavorite(cardId)
    }

    fun submitFeedback(cardId: String, kind: FeedbackItem.Kind, message: String) {
        viewModelScope.launch {
            val item = FeedbackItem.create(
                cardId = cardId,
                lang = Lang.fromCode(_settings.value.lang),
                kind = kind,
                message = message
            )
            feedbackQueue.enqueue(item)
            _showFeedback.value = false
        }
    }

    // MARK: - Undo Toast
    private fun showUndoToast(message: String, action: () -> Unit) {
        _undoToast.value = UndoToast(message, action)

        viewModelScope.launch {
            delay(2600)
            if (_undoToast.value?.message == message) {
                _undoToast.value = null
            }
        }
    }

    fun dismissUndoToast() {
        _undoToast.value = null
    }

    fun executeUndo() {
        _undoToast.value?.undoAction?.invoke()
        _undoToast.value = null
    }

    // MARK: - Navigation
    fun advanceToNext() {
        if (_currentIndex.value < _feedItems.value.size - 1) {
            _currentIndex.value++
        }
    }

    // MARK: - UI State
    fun setShowHelp(show: Boolean) { _showHelp.value = show }
    fun setShowMenu(show: Boolean) { _showMenu.value = show }
    fun setShowCardMenu(show: Boolean) { _showCardMenu.value = show }
    fun setShowFeedback(show: Boolean) { _showFeedback.value = show }
    fun setShowDonation(show: Boolean) { _showDonation.value = show }
    fun dismissDailyCompletion() { _showDailyCompletion.value = false }
    fun setSelectedCardId(cardId: String?) { _selectedCardId.value = cardId }
    fun clearAdError() { _adError.value = null }

    // MARK: - Settings
    fun setListMode(mode: ListMode) {
        viewModelScope.launch {
            settingsRepo.updateListMode(mode)
            sessionOrderCache = emptyList()
            uniqueViewedCount = 0
            rebuildFeed()
        }
    }

    fun setLang(lang: Lang) {
        viewModelScope.launch {
            settingsRepo.updateLang(lang)
        }
    }

    fun toggleTextScale() {
        viewModelScope.launch { settingsRepo.toggleTextScale() }
    }

    fun toggleFocus() {
        viewModelScope.launch { settingsRepo.toggleFocusMode() }
    }

    fun toggleContinuous() {
        viewModelScope.launch { settingsRepo.toggleContinuousReading() }
    }

    fun toggleGestures() {
        viewModelScope.launch { settingsRepo.toggleGestures() }
    }

    fun toggleDarkMode() {
        viewModelScope.launch { settingsRepo.toggleDarkMode() }
    }

    // MARK: - Notifications
    fun toggleNotifications() {
        viewModelScope.launch {
            val currentSettings = settingsRepo.getSettings()
            if (!currentSettings.notificationsEnabled) {
                // Enabling notifications
                settingsRepo.setNotificationsEnabled(true)
                val lang = Lang.fromCode(currentSettings.lang)
                NotificationService.scheduleDailyReminder(
                    getApplication(),
                    currentSettings.notificationHour,
                    currentSettings.notificationMinute,
                    lang
                )
            } else {
                // Disabling notifications
                settingsRepo.setNotificationsEnabled(false)
                NotificationService.cancelDailyReminder(getApplication())
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepo.setNotificationTime(hour, minute)
            val currentSettings = settingsRepo.getSettings()
            if (currentSettings.notificationsEnabled) {
                val lang = Lang.fromCode(currentSettings.lang)
                NotificationService.scheduleDailyReminder(
                    getApplication(),
                    hour,
                    minute,
                    lang
                )
            }
        }
    }

    fun markHelpSeen() {
        viewModelScope.launch { settingsRepo.markHelpSeen() }
    }

    // MARK: - Support Actions
    suspend fun watchVideo(activity: Activity): Boolean {
        _adError.value = null
        val result = AdMobService.showRewardedVideo(activity)
        if (result.ok) {
            supportTracker.markUsed()
        } else if (result.reason != null) {
            _adError.value = "AdMob: ${result.reason}"
        }
        return result.ok
    }

    fun openDonation() {
        _showDonation.value = true
    }

    // MARK: - Helpers
    suspend fun isNew(cardId: String): Boolean {
        return !seenRepo.isSeen(cardId, Lang.fromCode(_settings.value.lang))
    }

    suspend fun isLearned(cardId: String): Boolean {
        return learnedRepo.isLearned(cardId)
    }

    suspend fun isInReview(cardId: String): Boolean {
        return reviewRepo.isInReview(cardId)
    }

    suspend fun isReviewDue(cardId: String): Boolean {
        return reviewRepo.isDue(cardId)
    }

    suspend fun reviewStage(cardId: String): Int? {
        return reviewRepo.get(cardId)?.stage
    }

    private fun vibrate(effect: Int) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(effect))
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
}
