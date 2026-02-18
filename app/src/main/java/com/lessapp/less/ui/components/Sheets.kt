package com.lessapp.less.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessapp.less.data.model.*
import com.lessapp.less.util.L10n

// MARK: - Menu Sheet (Simplified)
@Composable
fun MenuSheet(
    settings: UISettings,
    l10n: L10n,
    availableTopics: List<String>,
    onModeChange: (ListMode) -> Unit,
    onLangChange: (Lang) -> Unit,
    onTopicToggle: (String) -> Unit,
    onClearTopics: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onClose: () -> Unit
) {
    val currentMode = ListMode.fromValue(settings.listMode)
    val currentLang = Lang.fromCode(settings.lang)
    val selectedTopics = settings.selectedTopics

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Mode
        Text(
            text = "Mode",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(l10n.feed, currentMode == ListMode.FEED) { onModeChange(ListMode.FEED) }
            ModeChip(l10n.daily, currentMode == ListMode.DAILY) { onModeChange(ListMode.DAILY) }
            ModeChip(l10n.learnedMode, currentMode == ListMode.LEARNED) { onModeChange(ListMode.LEARNED) }
            ModeChip(l10n.review, currentMode == ListMode.REVIEW) { onModeChange(ListMode.REVIEW) }
            HeartChip(currentMode == ListMode.FAVORITES) { onModeChange(ListMode.FAVORITES) }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language
        Text(
            text = l10n.language,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip("FR", currentLang == Lang.FR) { onLangChange(Lang.FR) }
            ModeChip("EN", currentLang == Lang.EN) { onLangChange(Lang.EN) }
            ModeChip("ES", currentLang == Lang.ES) { onLangChange(Lang.ES) }
        }

        // Topics filter
        if (availableTopics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = l10n.topics,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.5f)
                )
                if (selectedTopics.isNotEmpty()) {
                    TextButton(
                        onClick = onClearTopics,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = l10n.clearFilter,
                            fontSize = 12.sp,
                            color = Color.Blue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTopics.forEach { topic ->
                    val isSelected = selectedTopics.isEmpty() || selectedTopics.contains(topic)
                    TopicChip(
                        label = topic.replaceFirstChar { it.uppercase() },
                        isSelected = isSelected && selectedTopics.isNotEmpty(),
                        onClick = { onTopicToggle(topic) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom row: Settings + Help
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onSettingsClick) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = l10n.settings,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            TextButton(onClick = onHelpClick) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = l10n.help,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Heart chip with better visibility
@Composable
fun HeartChip(
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color.Red else Color.White,
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Icon(
            imageVector = Icons.Outlined.Favorite,
            contentDescription = "Favorites",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).size(18.dp),
            tint = if (selected) Color.White else Color.Red
        )
    }
}

// Topic chip for filtering
@Composable
fun TopicChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) Color.Blue else Color.Gray.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// MARK: - Settings Sheet
@Composable
fun SettingsSheet(
    settings: UISettings,
    l10n: L10n,
    learnedCount: Int,
    maxStreak: Int,
    topTopic: String?,
    onToggleTextScale: () -> Unit,
    onToggleFocus: () -> Unit,
    onToggleContinuous: () -> Unit,
    onToggleGestures: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleNotifications: () -> Unit,
    onNotificationTimeChange: (Int, Int) -> Unit,
    onSupportClick: () -> Unit,
    onClose: () -> Unit
) {
    val currentScale = TextScale.fromValue(settings.textScale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n.settings,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClose) {
                Text(
                    text = l10n.done,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statistics
        Text(
            text = l10n.statistics,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "✓ ${l10n.cardsLearned}",
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Text(
                text = "$learnedCount",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🔥 ${l10n.maxStreak}",
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Text(
                text = "$maxStreak ${l10n.days}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⭐ ${l10n.topTopic}",
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Text(
                text = topTopic?.replaceFirstChar { it.uppercase() } ?: l10n.noTopicYet,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reading settings
        Text(
            text = l10n.reading,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(
                if (currentScale == TextScale.NORMAL) "A" else "A+",
                currentScale == TextScale.LARGE
            ) { onToggleTextScale() }
            ModeChip(l10n.focus, settings.focusMode) { onToggleFocus() }
            ModeChip(l10n.continuous, settings.continuousReading) { onToggleContinuous() }
            ModeChip(l10n.gestures, settings.gesturesEnabled) { onToggleGestures() }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Appearance
        Text(
            text = l10n.appearance,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip("🌙 ${l10n.darkMode}", settings.darkMode) { onToggleDarkMode() }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notifications
        Text(
            text = l10n.notifications,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeChip(
                if (settings.notificationsEnabled) l10n.notificationsOn else l10n.notificationsOff,
                settings.notificationsEnabled
            ) { onToggleNotifications() }

            if (settings.notificationsEnabled) {
                NotificationTimePicker(
                    hour = settings.notificationHour,
                    minute = settings.notificationMinute,
                    onChange = onNotificationTimeChange
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Support
        Text(
            text = l10n.support,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onSupportClick) {
            Text(
                text = "❤️ ${l10n.supportUs}",
                color = Color.Red,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color.Black else Color.White,
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// MARK: - Help Sheet
@Composable
fun HelpSheet(
    l10n: L10n,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = l10n.help,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        HelpSection(l10n.helpReadTitle, l10n.helpReadContent)
        Spacer(modifier = Modifier.height(16.dp))
        HelpSection(l10n.helpActionsTitle, l10n.helpActionsContent)
        Spacer(modifier = Modifier.height(16.dp))
        HelpSection(l10n.helpModesTitle, l10n.helpModesContent)
        Spacer(modifier = Modifier.height(16.dp))
        HelpSection(l10n.helpPrivacyTitle, l10n.helpPrivacyContent)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = l10n.close,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HelpSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

// MARK: - Card Menu Sheet
@Composable
fun CardMenuSheet(
    l10n: L10n,
    isUnuseful: Boolean,
    isInReview: Boolean,
    onToggleUnuseful: () -> Unit,
    onToggleReview: () -> Unit,
    onReport: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        CardMenuItem(
            label = l10n.notUseful,
            isActive = isUnuseful,
            onClick = onToggleUnuseful
        )
        Spacer(modifier = Modifier.height(8.dp))
        CardMenuItem(
            label = l10n.reviewLater,
            isActive = isInReview,
            onClick = onToggleReview
        )
        Spacer(modifier = Modifier.height(8.dp))
        CardMenuItem(
            label = l10n.report,
            isActive = false,
            onClick = onReport
        )
    }
}

@Composable
fun CardMenuItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black
            )
            if (isActive) {
                Text(
                    text = "✓",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// MARK: - Feedback Content
@Composable
fun FeedbackContent(
    l10n: L10n,
    onSubmit: (FeedbackItem.Kind, String) -> Unit
) {
    var selectedKind by remember { mutableStateOf(FeedbackItem.Kind.OTHER) }
    var message by remember { mutableStateOf("") }

    Column {
        Text(
            text = l10n.feedbackSubtitle,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeedbackKindChip(l10n.feedbackTypo, selectedKind == FeedbackItem.Kind.TYPO) {
                selectedKind = FeedbackItem.Kind.TYPO
            }
            FeedbackKindChip(l10n.feedbackWrong, selectedKind == FeedbackItem.Kind.WRONG) {
                selectedKind = FeedbackItem.Kind.WRONG
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeedbackKindChip(l10n.feedbackUnclear, selectedKind == FeedbackItem.Kind.UNCLEAR) {
                selectedKind = FeedbackItem.Kind.UNCLEAR
            }
            FeedbackKindChip(l10n.feedbackOther, selectedKind == FeedbackItem.Kind.OTHER) {
                selectedKind = FeedbackItem.Kind.OTHER
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text(l10n.feedbackPlaceholder) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSubmit(selectedKind, message) },
            enabled = message.length >= 3,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = l10n.send,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FeedbackKindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color.Black else Color.White,
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// MARK: - Donation Sheet (Coming Soon)
@Composable
fun DonationSheet(
    l10n: L10n,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n.supportTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Text("✕", fontSize = 20.sp, color = Color.Black.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Coming soon message
        Text("❤️", fontSize = 60.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = l10n.comingSoon,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = l10n.comingSoonSubtitle,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// MARK: - Notification Time Picker
@Composable
fun NotificationTimePicker(
    hour: Int,
    minute: Int,
    onChange: (Int, Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(hour) }
    var selectedMinute by remember { mutableIntStateOf(minute) }

    val timeString = String.format("%02d:%02d", hour, minute)

    Surface(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🕐", fontSize = 12.sp)
            Text(
                text = timeString,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Heure du rappel") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    NumberPicker(
                        value = selectedHour,
                        range = 0..23,
                        onValueChange = { selectedHour = it }
                    )
                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    // Minute picker
                    NumberPicker(
                        value = selectedMinute,
                        range = 0..59,
                        onValueChange = { selectedMinute = it }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(selectedHour, selectedMinute)
                    showDialog = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) }
        ) {
            Text("▲", fontSize = 18.sp)
        }
        Text(
            text = String.format("%02d", value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) }
        ) {
            Text("▼", fontSize = 18.sp)
        }
    }
}

// MARK: - Card Detail Sheet (for long content)
@Composable
fun CardDetailSheet(
    card: Card,
    l10n: L10n,
    isLearned: Boolean,
    onLearnedClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        // Fixed Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.topic.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.5f)
                )

                IconButton(onClick = onMenuClick) {
                    Text(
                        text = "•••",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = card.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = card.hook,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bullets
            card.bullets.forEach { bullet ->
                Text(
                    text = "• $bullet",
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Why it matters
            Text(
                text = "💡 ${l10n.whyItMatters}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "→ ${card.why}",
                fontSize = 15.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${card.topic.uppercase()} · ${l10n.difficulty(card.difficulty)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed Footer - Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onLearnedClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLearned) Color.Black else Color.White,
                    contentColor = if (isLearned) Color.White else Color.Black
                ),
                border = if (!isLearned) ButtonDefaults.outlinedButtonBorder else null,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = l10n.learned,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                onClick = onShareClick,
                shape = RoundedCornerShape(999.dp),
                color = Color.White,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(
                    text = "↗",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
