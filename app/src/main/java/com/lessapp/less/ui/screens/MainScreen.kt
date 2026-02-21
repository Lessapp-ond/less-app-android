package com.lessapp.less.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lessapp.less.R
import com.lessapp.less.data.model.*
import com.lessapp.less.service.SupabaseService
import com.lessapp.less.ui.FeedViewModel
import com.lessapp.less.ui.components.*
import com.lessapp.less.ui.theme.AppColors
import com.lessapp.less.util.CardShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    activity: Activity,
    openDailyMode: Boolean = false,
    viewModel: FeedViewModel = viewModel()
) {
    val feedItems by viewModel.feedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val showMenu by viewModel.showMenu.collectAsState()
    val showHelp by viewModel.showHelp.collectAsState()
    val showCardMenu by viewModel.showCardMenu.collectAsState()
    val showFeedback by viewModel.showFeedback.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val selectedCardId by viewModel.selectedCardId.collectAsState()
    val undoToast by viewModel.undoToast.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val adError by viewModel.adError.collectAsState()

    val dailyProgress by viewModel.dailyProgress.collectAsState()
    val isDailyComplete by viewModel.isDailyComplete.collectAsState()
    val showDailyCompletion by viewModel.showDailyCompletion.collectAsState()
    val pagerKey by viewModel.pagerKey.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val maxStreak by viewModel.maxStreak.collectAsState()

    val l10n = viewModel.l10n
    val pagerState = rememberPagerState(pageCount = { maxOf(1, feedItems.size) })
    val scope = rememberCoroutineScope()
    val colors = AppColors.forDarkMode(settings.darkMode)

    // Card detail sheet state
    var selectedCardForDetail by remember { mutableStateOf<Card?>(null) }

    // Load cards on first launch
    LaunchedEffect(Unit) {
        viewModel.loadCards()
        // Open daily mode if launched from widget
        if (openDailyMode) {
            viewModel.enterDailyMode()
        }
    }

    // Show onboarding if not seen
    if (!settings.helpSeen) {
        OnboardingScreen(
            l10n = l10n,
            currentLang = Lang.fromCode(settings.lang),
            onSelectLang = { viewModel.setLang(it) },
            onComplete = { viewModel.markHelpSeen() }
        )
        return
    }

    // Keep pager in bounds when feedItems changes
    LaunchedEffect(feedItems.size) {
        if (feedItems.isNotEmpty() && pagerState.currentPage >= feedItems.size) {
            pagerState.scrollToPage(maxOf(0, feedItems.size - 1))
        }
    }

    // Track visible cards - only when pager has settled
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        // Wait for pager to finish scrolling before tracking
        if (!pagerState.isScrollInProgress && feedItems.isNotEmpty() && pagerState.currentPage < feedItems.size) {
            viewModel.cardBecameVisible(feedItems[pagerState.currentPage].id)
        }
    }

    // Scroll to top when switching modes
    LaunchedEffect(settings.listMode) {
        if (feedItems.isNotEmpty()) {
            pagerState.scrollToPage(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Header(
                learnedCount = learnedCount,
                listMode = ListMode.fromValue(settings.listMode),
                dailyProgress = dailyProgress,
                isDailyComplete = isDailyComplete,
                currentStreak = currentStreak,
                colors = colors,
                onFeedClick = { viewModel.setListMode(ListMode.FEED) },
                onDailyClick = { viewModel.enterDailyMode() },
                onMenuClick = { viewModel.setShowMenu(true) }
            )

            // Content
            if (isLoading && feedItems.isEmpty()) {
                // Loading skeleton
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CardSkeleton(isDark = settings.darkMode)
                }
            } else if (feedItems.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val errorMsg = SupabaseService.lastError
                    val listMode = ListMode.fromValue(settings.listMode)
                    EmptyState(
                        message = if (errorMsg != null) {
                            "Erreur: $errorMsg"
                        } else if (listMode == ListMode.REVIEW) {
                            l10n.nothingToReview
                        } else if (listMode == ListMode.FAVORITES) {
                            l10n.noFavorites
                        } else {
                            l10n.noCards
                        },
                        isDark = settings.darkMode
                    )
                }
            } else {
                // Feed - VerticalPager for one card at a time
                // key() forces recreation when pagerKey changes (entering Daily mode)
                key(pagerKey) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        pageSpacing = 16.dp,
                        key = { index -> feedItems.getOrNull(index)?.id ?: index }
                    ) { index ->
                    val item = feedItems.getOrNull(index) ?: return@VerticalPager

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (item) {
                            is FeedItem.Content -> {
                                var isNew by remember { mutableStateOf(false) }
                                var isLearned by remember { mutableStateOf(false) }
                                var isInReview by remember { mutableStateOf(false) }
                                var isReviewDue by remember { mutableStateOf(false) }
                                var isFavorite by remember { mutableStateOf(false) }

                                LaunchedEffect(item.card.id) {
                                    isNew = viewModel.isNew(item.card.id)
                                    isLearned = viewModel.isLearned(item.card.id)
                                    isInReview = viewModel.isInReview(item.card.id)
                                    isReviewDue = viewModel.isReviewDue(item.card.id)
                                    isFavorite = viewModel.isFavorite(item.card.id)
                                }

                                CardView(
                                    card = item.card,
                                    isNew = isNew,
                                    isLearned = isLearned,
                                    isInReview = isInReview,
                                    isReviewDue = isReviewDue,
                                    isFavorite = isFavorite,
                                    focusMode = settings.focusMode,
                                    textScale = TextScale.fromValue(settings.textScale),
                                    gesturesEnabled = settings.gesturesEnabled,
                                    isDark = settings.darkMode,
                                    l10n = l10n,
                                    onLearnedClick = {
                                        viewModel.toggleLearned(item.card.id)
                                        isLearned = !isLearned
                                    },
                                    onMenuClick = {
                                        viewModel.setSelectedCardId(item.card.id)
                                        viewModel.setShowCardMenu(true)
                                    },
                                    onShareClick = {
                                        CardShareHelper.shareCard(activity, item.card, l10n)
                                    },
                                    onFavoriteClick = {
                                        viewModel.toggleFavorite(item.card.id)
                                        isFavorite = !isFavorite
                                    },
                                    onSwipeRight = {
                                        viewModel.toggleLearned(item.card.id)
                                        isLearned = !isLearned
                                    },
                                    onSwipeLeft = { viewModel.toggleUnuseful(item.card.id) },
                                    onCardTap = { selectedCardForDetail = item.card }
                                )
                            }
                            is FeedItem.System -> {
                                SystemCardView(
                                    card = item.card,
                                    textScale = TextScale.fromValue(settings.textScale),
                                    isDark = settings.darkMode,
                                    onWatchVideo = {
                                        scope.launch {
                                            viewModel.watchVideo(activity)
                                        }
                                    }
                                )
                            }
                            is FeedItem.Opening -> {
                                OpeningCardView(
                                    card = item.card,
                                    textScale = TextScale.fromValue(settings.textScale),
                                    isDark = settings.darkMode
                                )
                            }
                        }
                    }
                }
                } // end key(pagerKey)
            }

            // Triangle decoration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▼",
                    fontSize = 24.sp,
                    color = colors.textPrimary.copy(alpha = 0.35f)
                )
            }
        }

        // Undo Toast
        undoToast?.let { toast ->
            UndoToastView(
                message = toast.message,
                undoLabel = l10n.undo,
                onUndo = { viewModel.executeUndo() },
                onDismiss = { viewModel.dismissUndoToast() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }

        // Daily Completion Celebration
        if (showDailyCompletion) {
            DailyCompletionView(
                message = l10n.dailyComplete,
                streak = currentStreak,
                onDismiss = { viewModel.dismissDailyCompletion() }
            )
        }
    }

    // Menu Sheet
    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowMenu(false) }
        ) {
            MenuSheet(
                settings = settings,
                l10n = l10n,
                availableTopics = viewModel.availableTopics,
                onModeChange = { viewModel.setListMode(it) },
                onLangChange = { viewModel.setLang(it) },
                onTopicToggle = { viewModel.toggleTopic(it) },
                onClearTopics = { viewModel.clearTopicFilter() },
                onSettingsClick = {
                    viewModel.setShowMenu(false)
                    viewModel.setShowSettings(true)
                },
                onHelpClick = {
                    viewModel.setShowMenu(false)
                    viewModel.setShowHelp(true)
                },
                onClose = { viewModel.setShowMenu(false) }
            )
        }
    }

    // Settings Sheet
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowSettings(false) }
        ) {
            SettingsSheet(
                settings = settings,
                l10n = l10n,
                learnedCount = learnedCount,
                maxStreak = maxStreak,
                topTopic = viewModel.topTopic,
                onToggleTextScale = { viewModel.toggleTextScale() },
                onToggleFocus = { viewModel.toggleFocus() },
                onToggleContinuous = { viewModel.toggleContinuous() },
                onToggleGestures = { viewModel.toggleGestures() },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleNotifications = { viewModel.toggleNotifications() },
                onNotificationTimeChange = { hour, minute -> viewModel.setNotificationTime(hour, minute) },
                onSupportClick = {
                    viewModel.setShowSettings(false)
                    scope.launch {
                        viewModel.watchVideo(activity)
                    }
                },
                onClose = { viewModel.setShowSettings(false) }
            )
        }
    }

    // Help Sheet
    if (showHelp) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.setShowHelp(false)
                viewModel.markHelpSeen()
            }
        ) {
            HelpSheet(
                l10n = l10n,
                onClose = {
                    viewModel.setShowHelp(false)
                    viewModel.markHelpSeen()
                }
            )
        }
    }

    // Card Menu Sheet
    if (showCardMenu && selectedCardId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowCardMenu(false) }
        ) {
            var isUnuseful by remember { mutableStateOf(false) }
            var isInReview by remember { mutableStateOf(false) }

            LaunchedEffect(selectedCardId) {
                selectedCardId?.let {
                    isUnuseful = viewModel.isLearned(it) // This should be isUnuseful check
                    isInReview = viewModel.isInReview(it)
                }
            }

            CardMenuSheet(
                l10n = l10n,
                isUnuseful = isUnuseful,
                isInReview = isInReview,
                onToggleUnuseful = {
                    selectedCardId?.let { viewModel.toggleUnuseful(it) }
                    viewModel.setShowCardMenu(false)
                },
                onToggleReview = {
                    selectedCardId?.let { viewModel.toggleReview(it) }
                    viewModel.setShowCardMenu(false)
                },
                onReport = {
                    viewModel.setShowCardMenu(false)
                    viewModel.setShowFeedback(true)
                },
                onClose = { viewModel.setShowCardMenu(false) }
            )
        }
    }

    // Feedback Modal
    if (showFeedback && selectedCardId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowFeedback(false) },
            title = { Text(l10n.feedbackTitle) },
            text = {
                FeedbackContent(
                    l10n = l10n,
                    onSubmit = { kind, message ->
                        selectedCardId?.let {
                            viewModel.submitFeedback(it, kind, message)
                        }
                    }
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setShowFeedback(false) }) {
                    Text(l10n.cancel)
                }
            }
        )
    }

    // Card Detail Sheet (for long content)
    selectedCardForDetail?.let { card ->
        ModalBottomSheet(
            onDismissRequest = { selectedCardForDetail = null }
        ) {
            var isLearned by remember { mutableStateOf(false) }
            LaunchedEffect(card.id) {
                isLearned = viewModel.isLearned(card.id)
            }
            CardDetailSheet(
                card = card,
                l10n = l10n,
                isLearned = isLearned,
                onLearnedClick = {
                    viewModel.toggleLearned(card.id)
                    isLearned = !isLearned
                },
                onShareClick = { CardShareHelper.shareCard(activity, card, l10n) },
                onMenuClick = {
                    selectedCardForDetail = null
                    viewModel.setSelectedCardId(card.id)
                    viewModel.setShowCardMenu(true)
                },
                onClose = { selectedCardForDetail = null }
            )
        }
    }

    // Ad Error Alert
    adError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearAdError() },
            title = { Text(l10n.adsError) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAdError() }) {
                    Text(l10n.ok)
                }
            }
        )
    }
}

@Composable
fun Header(
    learnedCount: Int,
    listMode: ListMode,
    dailyProgress: Int,
    isDailyComplete: Boolean,
    currentStreak: Int,
    colors: AppColors,
    onFeedClick: () -> Unit,
    onDailyClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Less logo
            Image(
                painter = painterResource(id = R.drawable.logo_less),
                contentDescription = "Less",
                modifier = Modifier.height(32.dp),
                contentScale = ContentScale.FillHeight
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Feed button
                Surface(
                    onClick = onFeedClick,
                    shape = RoundedCornerShape(14.dp),
                    color = if (listMode == ListMode.FEED) colors.buttonBackgroundActive else colors.buttonBackground,
                    border = if (listMode != ListMode.FEED) ButtonDefaults.outlinedButtonBorder else null
                ) {
                    Text(
                        text = "Feed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (listMode == ListMode.FEED) colors.buttonTextActive else colors.buttonText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Daily button
                Surface(
                    onClick = onDailyClick,
                    shape = RoundedCornerShape(14.dp),
                    color = if (listMode == ListMode.DAILY) colors.buttonBackgroundActive else colors.buttonBackground,
                    border = if (listMode != ListMode.DAILY) ButtonDefaults.outlinedButtonBorder else null
                ) {
                    Text(
                        text = "Daily",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (listMode == ListMode.DAILY) colors.buttonTextActive else colors.buttonText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Menu button
                Surface(
                    onClick = onMenuClick,
                    shape = RoundedCornerShape(17.dp),
                    color = colors.buttonBackground,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = "···",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.buttonText,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Stats or Daily Progress
        if (listMode == ListMode.DAILY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DailyProgressView(
                    progress = dailyProgress,
                    isComplete = isDailyComplete,
                    colors = colors
                )
                Spacer(modifier = Modifier.weight(1f))
                if (currentStreak > 0) {
                    StreakBadge(streak = currentStreak, colors = colors)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$learnedCount cartes \"apprises\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Cache : à jour",
                        fontSize = 12.sp,
                        color = colors.textTertiary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (currentStreak > 0) {
                    StreakBadge(streak = currentStreak, colors = colors)
                }
            }
        }
    }
}

@Composable
fun StreakBadge(
    streak: Int,
    colors: AppColors
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colors.buttonBackground, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "🔥",
            fontSize = 14.sp
        )
        Text(
            text = "$streak",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}

@Composable
fun DailyProgressView(
    progress: Int,
    isComplete: Boolean,
    colors: AppColors
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 6 dots: 1 opening + 5 content cards
        repeat(6) { index ->
            val filled = if (index == 0) progress >= 0 else progress >= index
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (filled) colors.textPrimary.copy(alpha = 0.6f)
                        else colors.textPrimary.copy(alpha = 0.15f)
                    )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isComplete) {
            Text(
                text = "✓",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun OpeningCardView(
    card: OpeningCard,
    textScale: TextScale,
    isDark: Boolean = false
) {
    val colors = AppColors.forDarkMode(isDark)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.cardBackground,
        shadowElevation = if (isDark) 0.dp else 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            // Title (styled like SystemCard)
            Text(
                text = card.title,
                fontSize = (22 * textScale.factor).sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )

            // Message (multi-line)
            Text(
                text = card.message,
                fontSize = (16 * textScale.factor).sp,
                color = colors.textPrimary.copy(alpha = 0.82f),
                lineHeight = (24 * textScale.factor).sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer with emoji (styled like "why" in SystemCard)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "🌱",
                    fontSize = (16 * textScale.factor).sp
                )
                Text(
                    text = card.footer,
                    fontSize = (14 * textScale.factor).sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String, isDark: Boolean = false) {
    val colors = AppColors.forDarkMode(isDark)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📭",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = colors.textSecondary
        )
    }
}
