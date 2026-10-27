package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.theme.*
import kotlinx.coroutines.delay

data class VideoScene(
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val caption: String,
    val narration: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoWalkthroughScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPlaying: Boolean = true,
    initialProgress: Float = 0f
) {
    BackHandler { onNavigateBack() }

    val scenes = remember {
        listOf(
            VideoScene(
                title = "Scene 1: App Boot & Onboarding",
                subtitle = "Plan once, guided daily setup",
                durationSeconds = 10,
                caption = "NutriPath loads with brand splash, then allows 1-click Demo login and biometric onboarding.",
                narration = "Welcome to NutriPath. Plan once, guided daily. Set up your personal biometrics, choose your dietary staples, and let the smart engine calculate your metabolic targets."
            ),
            VideoScene(
                title = "Scene 2: Home Dashboard & Rings",
                subtitle = "Live calorie gauge & 2x2 macros",
                durationSeconds = 10,
                caption = "Real-time circular calorie burn ring, 4 macro metrics (Protein, Carbs, Fat, Fiber), and 1-tap hydration.",
                narration = "Your daily dashboard at a glance. Real-time calorie burn ring, 2 by 2 macronutrient targets, and instant one-tap hydration logging keep you effortlessly on track."
            ),
            VideoScene(
                title = "Scene 3: Daily Log & Overdue Detection",
                subtitle = "Automated schedules & alerts",
                durationSeconds = 10,
                caption = "Recurring meals roll forward each morning. If a scheduled meal is late, an overdue alert lets you log in 1 tap.",
                narration = "Meals are scheduled in advance, eliminating manual searching. When a meal is overdue, a helpful prompt lets you confirm consumption with a single touch."
            ),
            VideoScene(
                title = "Scene 4: Smart Food Substitution",
                subtitle = "Auto-scaled macro-balanced swaps",
                durationSeconds = 10,
                caption = "Swap any planned meal item with a pantry staple. Portions auto-scale to maintain your protein and calorie target.",
                narration = "Need to switch foods? The smart substitution engine automatically scales your replacement portion to maintain your exact protein and calorie balance."
            ),
            VideoScene(
                title = "Scene 5: Two-Stage Background Alarms",
                subtitle = "AlarmManager prep & eating alerts",
                durationSeconds = 10,
                caption = "Stage 1 fires prep alerts 30 min in advance; Stage 2 provides actionable buttons in the notification tray.",
                narration = "Our two-stage notification system gives you advance warning to prepare meals, plus instant actionable buttons in your notification shade so you never lose momentum."
            ),
            VideoScene(
                title = "Scene 6: Weekly Insights & Routine",
                subtitle = "Long-term trends & custom schedules",
                durationSeconds = 10,
                caption = "Review weekly compliance bars, macro doughnuts, and adjust weekend meal routines effortlessly.",
                narration = "Review your weekly compliance trends and fine-tune your weekend routines. NutriPath adapts to your life, not the other way around."
            )
        )
    }

    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(initialPlaying) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var sceneProgress by remember { mutableFloatStateOf(initialProgress) }

    // Animated clock timer
    LaunchedEffect(isPlaying, currentSceneIndex, playbackSpeed) {
        if (!isPlaying) return@LaunchedEffect
        val totalMs = (scenes[currentSceneIndex].durationSeconds * 1000 / playbackSpeed).toLong()
        val stepMs = 50L
        val increment = stepMs.toFloat() / totalMs

        while (isPlaying && sceneProgress < 1f) {
            delay(stepMs)
            sceneProgress = (sceneProgress + increment).coerceIn(0f, 1f)
        }

        if (sceneProgress >= 1f) {
            sceneProgress = 0f
            if (currentSceneIndex < scenes.size - 1) {
                currentSceneIndex++
            } else {
                isPlaying = false
            }
        }
    }

    val activeScene = scenes[currentSceneIndex]
    val totalTimeSeconds = scenes.sumOf { it.durationSeconds }
    val elapsedSeconds = (scenes.take(currentSceneIndex).sumOf { it.durationSeconds } + (sceneProgress * activeScene.durationSeconds)).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Product Video Showcase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                        Text(
                            text = "Interactive Screen Transitions & Walkthrough",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            playbackSpeed = when (playbackSpeed) {
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                        }
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chapter selector chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(scenes) { index, scene ->
                    val isSelected = currentSceneIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            currentSceneIndex = index
                            sceneProgress = 0f
                            isPlaying = true
                        },
                        label = {
                            Text(
                                text = "${index + 1}. ${scene.title.substringAfter(": ").substringBefore("&").trim()}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NutriTeal.copy(alpha = 0.2f),
                            selectedLabelColor = NutriTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NutriTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // Video Player Mockup Screen Stage
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(2.dp, NutriTeal.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Render Mockup Content based on currentSceneIndex
                    when (currentSceneIndex) {
                        0 -> MockupScene1Onboarding()
                        1 -> MockupScene2HomeDashboard(progress = sceneProgress)
                        2 -> MockupScene3DailyLog(progress = sceneProgress)
                        3 -> MockupScene4Substitution(progress = sceneProgress)
                        4 -> MockupScene5Notification(progress = sceneProgress)
                        5 -> MockupScene6Insights(progress = sceneProgress)
                    }

                    // Watermark / Scene Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Scene ${currentSceneIndex + 1}/6",
                            color = NutriTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Captions & Voiceover Subtitle Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🎙️", fontSize = 14.sp)
                        Text(
                            text = activeScene.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${activeScene.narration}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress scrubber & timestamp
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", totalTimeSeconds / 60, totalTimeSeconds % 60),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = {
                        val totalElapsed = (scenes.take(currentSceneIndex).sumOf { it.durationSeconds } + (sceneProgress * activeScene.durationSeconds))
                        (totalElapsed / totalTimeSeconds.toFloat()).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NutriTeal,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Media control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentSceneIndex > 0) {
                            currentSceneIndex--
                            sceneProgress = 0f
                        } else {
                            sceneProgress = 0f
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Scene",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FilledIconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = NutriTeal)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        if (currentSceneIndex < scenes.size - 1) {
                            currentSceneIndex++
                            sceneProgress = 0f
                        } else {
                            currentSceneIndex = 0
                            sceneProgress = 0f
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Scene",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        currentSceneIndex = 0
                        sceneProgress = 0f
                        isPlaying = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay All",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Scene Mockup Sub-Composables

@Composable
private fun MockupScene1Onboarding() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NutriTealLight, NutriTealDark))),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🥗", fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "NutriPath",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = NutriTeal
        )
        Text(
            text = "Plan once • Guided daily",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Step 2 of 5: Biometrics Setup", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "• Height: 175 cm  • Weight: 70 kg", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Text(text = "• Goal: Lean Muscle (+15% Protein)", color = NutriTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("Instant Demo Login ➔", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MockupScene2HomeDashboard(progress: Float) {
    val animatedCalories = (950 + (progress * 200)).toInt()
    val animatedWater = (1250 + (progress * 250)).toInt()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🥗", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "NutriPath", fontWeight = FontWeight.Bold, color = NutriTeal)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NutriAmber.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "🔥 7 Day Streak",
                    color = NutriAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Hero Calorie Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Today's Energy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "$animatedCalories / 2,150 kcal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(text = "1,000 kcal remaining", fontSize = 10.sp, color = NutriTeal)
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(4.dp, NutriTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${(animatedCalories * 100 / 2150)}%", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NutriTeal)
                }
            }
        }

        // 2x2 Macros
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, NutriProteinRed.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Protein", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(text = "68 / 135g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NutriProteinRed)
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, NutriCarbGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Carbs", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(text = "120 / 230g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NutriCarbGold)
                }
            }
        }

        // Water Tracker
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "💧 Hydration", color = NutriWaterCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$animatedWater / 2,500 ml", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (animatedWater / 2500f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = NutriWaterCyan
                )
            }
        }
    }
}

@Composable
private fun MockupScene3DailyLog(progress: Float) {
    val isOverdueHandled = progress > 0.4f

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Daily Meal Timeline", fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Today", fontSize = 11.sp, color = NutriTeal)
        }

        // Overdue Alert Banner
        AnimatedVisibility(visible = !isOverdueHandled) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "⚠️ Breakfast Overdue (08:30)", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEF4444)) {
                        Text(text = "Tap to Confirm", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }

        // Meal Row 1
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "🥣 Oatmeal & Whey", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text(text = "08:30 AM • 420 kcal • 32g P", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOverdueHandled) Color(0xFF10B981).copy(alpha = 0.2f) else NutriAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isOverdueHandled) "✓ Consumed" else "Pending",
                        color = if (isOverdueHandled) Color(0xFF34D399) else NutriAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Meal Row 2
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "🍗 Grilled Chicken & Rice", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text(text = "01:00 PM • 540 kcal • 42g P", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NutriAmber.copy(alpha = 0.2f)
                ) {
                    Text(text = "⇄ Substitute", color = NutriAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun MockupScene4Substitution(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Smart Food Substitution", fontWeight = FontWeight.Bold, color = NutriTeal)

        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "ORIGINAL PLANNED MEAL", fontSize = 9.sp, color = Color(0xFF94A3B8))
                Text(text = "🍗 Grilled Chicken Breast (150g)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                Text(text = "248 kcal • 46.5g Protein", fontSize = 10.sp, color = NutriProteinRed)
            }
        }

        Text(text = "⇅ Smart Swapping with Pantry Item", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = NutriTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF132E35), border = BorderStroke(1.dp, NutriTeal), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "AUTO-SCALED PORTION", fontSize = 9.sp, color = NutriTeal)
                Text(text = "🧀 Paneer Tikka (Auto-Scaled: 165g)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                Text(text = "252 kcal (Δ +4) • 44.2g Protein (Δ -2g)", fontSize = 10.sp, color = NutriProteinRed)
            }
        }

        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "✨ Portion auto-scaled to match your protein target within 98% accuracy!",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun MockupScene5Notification(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "⏰", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Android AlarmManager Wake Alert", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
        Text(text = "Two-stage exact heads-up alerts", fontSize = 11.sp, color = Color(0xFF94A3B8))

        Spacer(modifier = Modifier.height(14.dp))

        // Mock System Notification Box
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "🔔 NutriPath System • 12:30 PM", fontSize = 9.sp, color = NutriTeal, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Lunch in 30 mins: Prep Alert", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                Text(text = "Paneer Tikka (165g) & Brown Rice. Start prepping!", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = NutriTeal.copy(alpha = 0.2f), border = BorderStroke(1.dp, NutriTeal)) {
                        Text(text = "✓ I Ate It", color = NutriTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF334155)) {
                        Text(text = "⇄ Substitute", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MockupScene6Insights(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Weekly Analytics", fontWeight = FontWeight.Bold, color = Color.White)
            Surface(shape = RoundedCornerShape(6.dp), color = NutriTeal) {
                Text(text = "92% Adherence", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        // Bar Chart
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Daily Calorie Compliance", fontSize = 10.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.85f, 0.92f, 0.78f, 0.95f, 0.88f, 0.65f, 0.90f)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    heights.forEachIndexed { i, h ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height((50 * h).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (h > 0.75f) NutriTeal else NutriAmber)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = days[i], fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Weekend Routine Variations", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                Text(text = "Breakfast automatically shifted to 09:30 AM on weekends", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
