package com.example.frontend.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.frontend.theme.NutriTeal
import kotlinx.coroutines.delay

/**
 * Minimalist Loading Screen:
 * Displays the round icon with smooth zoom effect and clean loading transition to the Login page.
 */
@Composable
fun NutriPathLoadingScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    var isFinished by remember { mutableStateOf(false) }

    if (animate) {
        LaunchedEffect(Unit) {
            delay(2200L)
            isFinished = true
            delay(250L)
            onLoadingFinished()
        }
    }

    // Zoom pulse animation for the icon
    val zoomScale = if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "icon_zoom_pulse")
        val animatedZoom by infiniteTransition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "zoom_scale"
        )
        animatedZoom
    } else {
        1.0f
    }

    val glowAlpha = if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "icon_glow_pulse")
        val animatedGlow by infiniteTransition.animateFloat(
            initialValue = 0.20f,
            targetValue = 0.60f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
        animatedGlow
    } else {
        0.45f
    }

    // Smooth exit scale when transitioning to login
    val exitScale by animateFloatAsState(
        targetValue = if (isFinished) 1.25f else 1f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "exit_scale"
    )
    val exitAlpha by animateFloatAsState(
        targetValue = if (isFinished) 0f else 1f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "exit_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFCFA),
                        Color(0xFFF3F8F4),
                        Color(0xFFEBF4EE)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isFinished) {
                    isFinished = true
                    onLoadingFinished()
                }
            }
            .alpha(exitAlpha)
            .scale(exitScale)
            .testTag("nutripath_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(zoomScale)
                    .testTag("loading_icon_container")
            ) {
                // Soft ambient circular glow pulsing with zoom
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NutriTeal.copy(alpha = glowAlpha * 0.40f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "NutriPath Logo",
                    modifier = Modifier.size(130.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Minimalist clean loading indicator
            if (animate) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = NutriTeal,
                    strokeWidth = 3.dp,
                    trackColor = NutriTeal.copy(alpha = 0.15f)
                )
            } else {
                CircularProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.size(28.dp),
                    color = NutriTeal,
                    strokeWidth = 3.dp,
                    trackColor = NutriTeal.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NutriPath",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                letterSpacing = 0.5.sp
            )
        }
    }
}
