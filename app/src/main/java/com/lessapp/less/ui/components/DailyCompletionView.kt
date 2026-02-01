package com.lessapp.less.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Particle(
    val id: Int,
    val initialX: Float,
    val initialY: Float,
    val targetY: Float,
    val size: Float,
    val color: Color
)

@Composable
fun DailyCompletionView(
    message: String,
    streak: Int = 0,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var showStreak by remember { mutableStateOf(false) }
    val particles = remember {
        val colors = listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFF9800), // Orange
            Color(0xFF00BCD4)  // Cyan
        )
        (0 until 20).map { i ->
            Particle(
                id = i,
                initialX = Random.nextFloat() * 300f - 150f,
                initialY = Random.nextFloat() * 400f - 200f,
                targetY = Random.nextFloat() * -150f - 50f,
                size = Random.nextFloat() * 6f + 6f,
                color = colors.random()
            )
        }
    }

    // Checkmark animation
    val checkmarkScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmarkScale"
    )

    val checkmarkAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "checkmarkAlpha"
    )

    // Text animation
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 300),
        label = "textAlpha"
    )

    val textOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(400, delayMillis = 300),
        label = "textOffset"
    )

    // Streak animation
    val streakScale by animateFloatAsState(
        targetValue = if (showStreak) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "streakScale"
    )

    val streakAlpha by animateFloatAsState(
        targetValue = if (showStreak) 1f else 0f,
        animationSpec = tween(400, delayMillis = 500),
        label = "streakAlpha"
    )

    // Start animation on appear
    LaunchedEffect(Unit) {
        visible = true
        delay(500)
        showStreak = true
        delay(2000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Particles
        particles.forEach { particle ->
            var particleVisible by remember { mutableStateOf(false) }
            val particleAlpha by animateFloatAsState(
                targetValue = if (particleVisible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = (Random.nextFloat() * 300).toInt()
                ),
                label = "particleAlpha${particle.id}"
            )
            val particleY by animateFloatAsState(
                targetValue = if (particleVisible) particle.targetY else particle.initialY,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = (Random.nextFloat() * 300).toInt(),
                    easing = FastOutSlowInEasing
                ),
                label = "particleY${particle.id}"
            )

            LaunchedEffect(Unit) {
                particleVisible = true
                delay(1000)
                particleVisible = false
            }

            Box(
                modifier = Modifier
                    .offset(x = particle.initialX.dp, y = particleY.dp)
                    .size(particle.size.dp)
                    .alpha(particleAlpha)
                    .background(particle.color, CircleShape)
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Checkmark circle
            Box(
                modifier = Modifier
                    .scale(checkmarkScale)
                    .alpha(checkmarkAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape)
                )
                // Circle border
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(4.dp, Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            // Message
            Text(
                text = message,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp)
            )

            // Streak badge
            if (streak > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .scale(streakScale)
                        .alpha(streakAlpha)
                        .background(Color(0xFFFF9800).copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "$streak jour${if (streak > 1) "s" else ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
