package com.lessapp.less.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    focusMode: Boolean,
    textScale: TextScale,
    gesturesEnabled: Boolean,
    l10n: L10n,
    onLearnedClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scale = textScale.factor

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
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .shadow(6.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Badge
                if (!focusMode && isNew) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(
                            text = "Nouveau",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.62f),
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
                    color = Color.Black,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Hook
                Text(
                    text = card.hook,
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.78f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Bullets
                card.bullets.take(3).forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        fontSize = (15 * scale).sp,
                        color = Color.Black.copy(alpha = 0.82f),
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
                    color = Color.Black.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ ${card.why}",
                    fontSize = (14 * scale).sp,
                    color = Color.Black.copy(alpha = 0.7f),
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
                                containerColor = if (isLearned) Color.Black else Color.White,
                                contentColor = if (isLearned) Color.White else Color.Black
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

                        // Menu button
                        Surface(
                            onClick = onMenuClick,
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White,
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(
                                text = "···",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Meta
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.topic.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Text(
                        text = " · ",
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Text(
                        text = l10n.difficulty(card.difficulty),
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    if (isInReview) {
                        Text(
                            text = " · à revoir${if (isReviewDue) " (due)" else ""}",
                            fontSize = 12.sp,
                            color = if (isReviewDue) Color(0xFFFF9500) else Color.Black.copy(alpha = 0.45f)
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
    onWatchVideo: () -> Unit,
    onDonate: () -> Unit
) {
    val scale = textScale.factor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = card.title,
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.hook,
                fontSize = (16 * scale).sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            card.bullets.forEach { bullet ->
                Text(
                    text = "• $bullet",
                    fontSize = (15 * scale).sp,
                    color = Color.Black.copy(alpha = 0.82f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "❤️ ${card.supportTitle}",
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.supportDescription,
                fontSize = (14 * scale).sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onWatchVideo,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = card.watchVideoLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDonate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = card.donateLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = card.finePrint,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.45f)
            )
        }
    }
}

// MARK: - Card Skeleton
@Composable
fun CardSkeleton() {
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
        colors = listOf(
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
            .shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
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
