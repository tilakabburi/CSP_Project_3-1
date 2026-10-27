package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.theme.NutriTeal
import com.example.frontend.theme.NutriWaterCyan
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterSettingsScreen(
    currentWaterTarget: Int,
    waterNotificationEnabled: Boolean,
    onWaterTargetChanged: (Int) -> Unit,
    onToggleWaterNotification: (Boolean) -> Unit,
    onQuickLogWater: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    reminderIntervalHours: Int = 2,
    onIntervalHoursChanged: (Int) -> Unit = {},
    onSimulateWaterReminder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current

    var reminderInterval by remember(reminderIntervalHours, lang) {
        mutableStateOf(NutriStrings.reminderIntervalHours(reminderIntervalHours, lang))
    }
    val intervalOptions = listOf(
        1 to NutriStrings.reminderIntervalHours(1, lang),
        2 to NutriStrings.reminderIntervalHours(2, lang),
        3 to NutriStrings.reminderIntervalHours(3, lang)
    )

    var selectedGlassSize by remember { mutableStateOf(250) }
    val glassSizes = listOf(
        150 to NutriStrings.s(lang, "Cup (150ml)", "చిన్న కప్పు (150ml)"),
        250 to NutriStrings.s(lang, "Glass (250ml)", "గ్లాసు (250ml)"),
        500 to NutriStrings.s(lang, "Bottle (500ml)", "బాటిల్ (500ml)"),
        750 to NutriStrings.s(lang, "Flask (750ml)", "ఫ్లాస్క్ (750ml)")
    )

    var showLoggedSnackbar by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = NutriStrings.waterTrackerTitle(lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.waterTrackerSubtitle(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("water_settings_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = NutriStrings.back(lang))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            if (showLoggedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showLoggedSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(NutriStrings.s(lang, "Logged $selectedGlassSize ml of water! 💧", "$selectedGlassSize ml నీరు నమోదు చేయబడింది! 💧"))
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // Daily Target Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NutriWaterCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = NutriWaterCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = NutriStrings.dailyWaterTarget(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget - 100).coerceAtLeast(1000)) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("water_target_minus_100_btn"),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = NutriWaterCyan.copy(alpha = 0.15f),
                                    contentColor = NutriWaterCyan
                                )
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease 100 ml",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "$currentWaterTarget ml",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = NutriWaterCyan
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            FilledTonalIconButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget + 100).coerceAtMost(5000)) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("water_target_plus_100_btn"),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = NutriWaterCyan.copy(alpha = 0.15f),
                                    contentColor = NutriWaterCyan
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase 100 ml",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = NutriStrings.s(
                                lang,
                                "Approx. ${(currentWaterTarget / 250)} standard glasses (250 ml each)",
                                "సుమారు ${(currentWaterTarget / 250)} సాధారణ గ్లాసులు (ఒక్కొక్కటి 250 ml)"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stepper row with balanced equal weights so all 4 buttons fit neatly without squeezing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget - 500).coerceAtLeast(1000)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("water_target_minus_500_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "-500 ml",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            FilledTonalButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget - 250).coerceAtLeast(1000)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("water_target_minus_250_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "-250 ml",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            FilledTonalButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget + 250).coerceAtMost(5000)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("water_target_plus_250_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "+250 ml",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = { onWaterTargetChanged((currentWaterTarget + 500).coerceAtMost(5000)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("water_target_plus_500_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "+500 ml",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = currentWaterTarget.toFloat(),
                            onValueChange = { onWaterTargetChanged((it / 50).toInt() * 50) },
                            valueRange = 1000f..5000f,
                            steps = 39,
                            colors = SliderDefaults.colors(
                                thumbColor = NutriWaterCyan,
                                activeTrackColor = NutriWaterCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Quick Hydration Log Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = NutriStrings.quickLogWaterTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.s(lang, "Select your default container to log water in 1 tap", "1 ట్యాప్‌లో నీటిని నమోదు చేయడానికి పరిమాణాన్ని ఎంచుకోండి"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            glassSizes.forEachIndexed { index, (ml, label) ->
                                SegmentedButton(
                                    selected = selectedGlassSize == ml,
                                    onClick = { selectedGlassSize = ml },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = glassSizes.size)
                                ) {
                                    Text("${ml}ml", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onQuickLogWater(selectedGlassSize)
                                showLoggedSnackbar = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriWaterCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                NutriStrings.s(lang, "Log $selectedGlassSize ml Water Now", "ఇప్పుడే $selectedGlassSize ml నీటిని నమోదు చేయండి"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Reminders Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(NutriWaterCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = NutriWaterCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = NutriStrings.waterReminderAlerts(lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = NutriStrings.s(lang, "Gentle alerts during the day", "రోజంతా సమయానికి నీటి రిమైండర్లు"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = waterNotificationEnabled,
                                onCheckedChange = onToggleWaterNotification,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NutriWaterCyan
                                )
                            )
                        }

                        if (waterNotificationEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = NutriStrings.reminderInterval(lang),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                intervalOptions.forEachIndexed { index, (hours, label) ->
                                    SegmentedButton(
                                        selected = reminderInterval == label,
                                        onClick = {
                                            reminderInterval = label
                                            onIntervalHoursChanged(hours)
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = intervalOptions.size)
                                    ) {
                                        Text(label, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = onSimulateWaterReminder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_water_reminder_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NutriWaterCyan)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(NutriStrings.simulateWaterAlert(lang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hydration Best Practices Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = NutriStrings.s(lang, "💡 Daily Hydration Tips", "💡 రోజువారీ నీటి చిట్కాలు"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = NutriStrings.s(
                                lang,
                                "• Start your morning with 1 glass of warm water.\n• Drink water 30 minutes prior to meals for healthy digestion.\n• Increase intake by 500 ml on hot or high activity days.",
                                "• రోజూ ఉదయం 1 గ్లాసు గోరువెచ్చని నీటితో ప్రారంభించండి.\n• మంచి జీర్ణక్రియ కోసం భోజనానికి 30 నిమిషాల ముందు నీరు త్రాగండి.\n• ఎక్కువ ఎండ లేదా వ్యాయామం ఉన్న రోజుల్లో అదనంగా 500 ml త్రాగండి."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
