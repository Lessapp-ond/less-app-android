package com.lessapp.less.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessapp.less.data.model.*
import com.lessapp.less.ui.theme.AppColors
import com.lessapp.less.util.L10n
import kotlin.math.roundToInt

// MARK: - Card View
@Composable
fun CardView(
    card: Card,
    isNew: Boolean,
    isLearned: Boolean,
    isInReview: Boolean,
    isReviewDue: Boolean,
    isFavorite: Boolean,
    focusMode: Boolean,
    textScale: TextScale,
    gesturesEnabled: Boolean,
    isDark: Boolean = false,
    l10n: L10n,
    onLearnedClick: () -> Unit,
    onMenuClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onCardTap: () -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scale = textScale.factor
    val colors = AppColors.forDarkMode(isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (gesturesEnabled) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 100) onSwipeRight()
                                else if (offsetX < -100) onSwipeLeft()
                                offsetX = 0f
                            },
                            onDragCancel = { offsetX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX += dragAmount
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        // Background color indicator
        if (gesturesEnabled && kotlin.math.abs(offsetX) > 50) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (offsetX > 0) Color.Green.copy(alpha = 0.25f)
                        else Color.Red.copy(alpha = 0.25f)
                    )
            )
        }

        // Card content
        Surface(
            onClick = onCardTap,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .shadow(if (isDark) 0.dp else 6.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBackground
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Badge
                if (!focusMode && isNew) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colors.buttonBackground,
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(
                            text = "Nouveau",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Title
                Text(
                    text = card.title,
                    fontSize = (22 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Hook
                Text(
                    text = card.hook,
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary.copy(alpha = 0.78f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Bullets
                card.bullets.take(3).forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        fontSize = (15 * scale).sp,
                        color = colors.textPrimary.copy(alpha = 0.82f),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Why
                Text(
                    text = "💡 ${l10n.whyItMatters}",
                    fontSize = (14 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ ${card.why}",
                    fontSize = (14 * scale).sp,
                    color = colors.textPrimary.copy(alpha = 0.7f),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                if (!focusMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Learned button
                        Button(
                            onClick = onLearnedClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLearned) colors.buttonBackgroundActive else colors.buttonBackground,
                                contentColor = if (isLearned) colors.buttonTextActive else colors.buttonText
                            ),
                            border = if (!isLearned) ButtonDefaults.outlinedButtonBorder else null,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = l10n.learned,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share button
                        Surface(
                            onClick = onShareClick,
                            shape = RoundedCornerShape(22.dp),
                            color = colors.buttonBackground,
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = colors.buttonText,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }

                        // Menu button
                        Surface(
                            onClick = onMenuClick,
                            shape = RoundedCornerShape(22.dp),
                            color = colors.buttonBackground,
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(
                                text = "···",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.buttonText,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Meta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.topic.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary
                    )
                    Text(
                        text = " · ",
                        color = colors.textTertiary
                    )
                    Text(
                        text = l10n.difficulty(card.difficulty),
                        fontSize = 12.sp,
                        color = colors.textTertiary
                    )
                    if (isInReview) {
                        Text(
                            text = " · à revoir${if (isReviewDue) " (due)" else ""}",
                            fontSize = 12.sp,
                            color = if (isReviewDue) Color(0xFFFF9500) else colors.textTertiary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // Favorite button
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (isFavorite) "❤️" else "🤍",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// MARK: - System Card View
@Composable
fun SystemCardView(
    card: SystemCard,
    textScale: TextScale,
    isDark: Boolean = false,
    onWatchVideo: () -> Unit
) {
    val scale = textScale.factor
    val colors = AppColors.forDarkMode(isDark)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDark) 0.dp else 6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = colors.cardBackground
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = card.title,
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.hook,
                fontSize = (16 * scale).sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            card.bullets.forEach { bullet ->
                Text(
                    text = "• $bullet",
                    fontSize = (15 * scale).sp,
                    color = colors.textPrimary.copy(alpha = 0.82f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Why section
            Text(
                text = "💡 ${card.why}",
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "❤️ ${card.supportTitle}",
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.supportText,
                fontSize = (14 * scale).sp,
                color = colors.textPrimary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onWatchVideo,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonBackgroundActive,
                    contentColor = colors.buttonTextActive
                ),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = card.ctaVideo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// MARK: - Card Skeleton
@Composable
fun CardSkeleton(isDark: Boolean = false) {
    val colors = AppColors.forDarkMode(isDark)
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = if (isDark) listOf(
            Color(0xFF3A3A3C),
            Color(0xFF4A4A4C),
            Color(0xFF3A3A3C)
        ) else listOf(
            Color(0xFFE0E0E0),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 200f, 0f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(if (isDark) 0.dp else 6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = colors.cardBackground
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(16.dp))
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width((250 - it * 30).dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

// MARK: - Undo Toast
@Composable
fun UndoToastView(
    message: String,
    undoLabel: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.9f),
        onClick = onDismiss
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp
            )
            TextButton(onClick = onUndo) {
                Text(
                    text = undoLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
