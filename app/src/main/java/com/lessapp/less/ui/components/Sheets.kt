package com.lessapp.less.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// MARK: - Menu Sheet
@Composable
fun MenuSheet(
    settings: UISettings,
    l10n: L10n,
    onModeChange: (ListMode) -> Unit,
    onLangChange: (Lang) -> Unit,
    onToggleTextScale: () -> Unit,
    onToggleFocus: () -> Unit,
    onToggleContinuous: () -> Unit,
    onToggleGestures: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onHelpClick: () -> Unit,
    onClose: () -> Unit
) {
    val currentMode = ListMode.fromValue(settings.listMode)
    val currentLang = Lang.fromCode(settings.lang)
    val currentScale = TextScale.fromValue(settings.textScale)

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
            ModeChip(l10n.unusefulMode, currentMode == ListMode.UNUSEFUL) { onModeChange(ListMode.UNUSEFUL) }
            ModeChip(l10n.review, currentMode == ListMode.REVIEW) { onModeChange(ListMode.REVIEW) }
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
                false
            ) { onToggleTextScale() }
            ModeChip(l10n.focus, settings.focusMode) { onToggleFocus() }
            ModeChip(l10n.continuous, settings.continuousReading) { onToggleContinuous() }
            ModeChip(l10n.gestures, settings.gesturesEnabled) { onToggleGestures() }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dark mode
        Text(
            text = l10n.darkMode,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip("🌙", settings.darkMode) { onToggleDarkMode() }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Help
        TextButton(onClick = onHelpClick) {
            Text(
                text = l10n.help,
                color = Color.Black.copy(alpha = 0.6f)
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
