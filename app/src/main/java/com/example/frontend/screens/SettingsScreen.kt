package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.theme.NutriAmber
import com.example.frontend.theme.NutriTeal
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onLanguageChange: (AppLanguage) -> Unit = {},
    is24HourFormat: Boolean = false,
    onTimeFormatChange: (Boolean) -> Unit = {},
    weightUnit: String = "kg (Metric)",
    onWeightUnitChange: (String) -> Unit = {},
    heightUnit: String = "cm (Metric)",
    onHeightUnitChange: (String) -> Unit = {},
    onOpenSimulator: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenVideoTour: () -> Unit = {},
    onResetApp: () -> Unit = {},
    onClearInputs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hapticFeedback by remember { mutableStateOf(true) }
    var autoSyncReminders by remember { mutableStateOf(true) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearInputsDialog by remember { mutableStateOf(false) }

    val isTelugu = currentLanguage == AppLanguage.TELUGU

    BackHandler {
        if (showResetDialog) {
            showResetDialog = false
        } else if (showClearInputsDialog) {
            showClearInputsDialog = false
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTelugu) "సెట్టింగులు" else "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTelugu) "ప్రాధాన్యతలు & యాప్ అమరికలు" else "Preferences & app settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
            // Language Selection Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("language_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTelugu) "భాష ఎంపిక (Language)" else "Language Preference",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTelugu)
                                "యాప్‌లోని శీర్షికలు, ప్లాన్‌లు మరియు కొలతలకు భాషను ఎంచుకోండి"
                            else
                                "Select your preferred language for meal plans, metrics, and navigation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val languages = listOf(AppLanguage.ENGLISH, AppLanguage.TELUGU)
                            languages.forEachIndexed { index, lang ->
                                val isSelected = currentLanguage == lang
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { onLanguageChange(lang) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
                                    modifier = Modifier.testTag("lang_option_${lang.code}")
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.TELUGU) "తెలుగు (Telugu)" else "English",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Time & Clock Format Preference Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("time_format_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NutriTeal.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = NutriTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = NutriStrings.timeFormatPreference(currentLanguage),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isTelugu) "గడియార సమయ శైలి" else "Clock & schedule style",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Active format badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NutriTeal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (is24HourFormat) "24H" else "12H AM/PM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = NutriStrings.timeFormatDesc(currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented Button: 12-Hour (AM/PM) vs 24-Hour (24h)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(
                                Pair(false, NutriStrings.timeFormat12Hour(currentLanguage)),
                                Pair(true, NutriStrings.timeFormat24Hour(currentLanguage))
                            )
                            options.forEachIndexed { index, (is24hOption, label) ->
                                val isSelected = is24HourFormat == is24hOption
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { onTimeFormatChange(is24hOption) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                    modifier = Modifier.testTag(if (is24hOption) "time_format_24h_btn" else "time_format_12h_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (is24hOption) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Units Preference Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTelugu) "కొలతల యూనిట్లు" else "Measurement Units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTelugu) "మీ ప్రమాణాలను ఎంచుకోండి" else "Choose your units",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isTelugu) "బరువు యూనిట్" else "Weight Unit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val units = listOf("kg (Metric)", "lbs (Imperial)")
                            units.forEachIndexed { index, u ->
                                SegmentedButton(
                                    selected = weightUnit == u,
                                    onClick = { onWeightUnitChange(u) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size)
                                ) {
                                    Text(u, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isTelugu) "ఎత్తు యూనిట్" else "Height Unit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val units = listOf("cm (Metric)", "ft/in (Imperial)")
                            units.forEachIndexed { index, u ->
                                SegmentedButton(
                                    selected = heightUnit == u,
                                    onClick = { onHeightUnitChange(u) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size)
                                ) {
                                    Text(u, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Notifications & Reminders System
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTelugu) "రిమైండర్లు & నోటిఫికేషన్లు" else "Reminders & Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTelugu) "ఆటో-రీషెడ్యూల్ అలారాలు" else "Auto-Reschedule Alarms",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isTelugu) "షెడ్యూల్ చేసిన ఆహార సమయాలతో అలారాలను సమన్వయం చేయండి" else "Sync alarms with scheduled daily food times",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSyncReminders,
                                onCheckedChange = { autoSyncReminders = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NutriTeal)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTelugu) "హాప్టిక్ ఫీడ్‌బ్యాక్" else "Haptic Feedback",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isTelugu) "ఆహారం లాగ్ చేసినప్పుడు వైబ్రేషన్ సంకేతం" else "Vibrate when logging meals and hitting targets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = hapticFeedback,
                                onCheckedChange = { hapticFeedback = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NutriTeal)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Watch Video Showcase
                        Button(
                            onClick = onOpenVideoTour,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTelugu) "ప్రొడక్ట్ వీడియో షోకేస్ చూడండి" else "Watch Product Video Showcase",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Test notifications button
                        OutlinedButton(
                            onClick = onOpenSimulator,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NutriAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTelugu) "రిమైండర్లను పరీక్షించండి" else "Test Reminders",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // App About & Version Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NutriTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥗", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "NutriPath",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isTelugu) "వెర్షన్ 1.0.0 • ఆఫ్‌లైన్ ఫస్ట్" else "Version 1.0.0 • Offline First",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isTelugu) "మీ వ్యక్తిగత పోషకాహార, భోజన దినచర్య మరియు హైడ్రేషన్ గైడ్." else "Your personal nutrition, meal routine, and hydration coach.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Reset App Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTelugu) "డేటా & రీసెట్" else "Data & Reset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isTelugu) "మీ లాగ్ చేసిన డేటాను తొలగించండి లేదా యాప్‌ను మొదటి నుండి రీసెట్ చేయండి." else "Clear your logged data or reset the app from the start.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showClearInputsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("clear_inputs_btn")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTelugu) "ఆహార ఎంపికలు & లాగ్‌లను తొలగించు" else "Clear Food Selections & Logs",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("reset_app_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTelugu) "యాప్‌ను మొదటి నుండి రీసెట్ చేయి" else "Reset App to Beginning",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearInputsDialog) {
        AlertDialog(
            onDismissRequest = { showClearInputsDialog = false },
            icon = {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
            },
            title = { Text(if (isTelugu) "అన్ని ఎంపికలను తీసివేయాలా?" else "Clear All Input Data?", fontWeight = FontWeight.Bold) },
            text = { Text(if (isTelugu) "ఇది మీ ఫుడ్ పూల్, భోజన దినచర్య మరియు లాగ్ హిస్టరీని తీసివేస్తుంది." else "This will clear your food pool, meal routine, and logged history.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearInputsDialog = false
                        onClearInputs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isTelugu) "డేటాను తొలగించు" else "Clear All Data", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearInputsDialog = false }) {
                    Text(if (isTelugu) "రద్దు చేయి" else "Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
            },
            title = { Text(if (isTelugu) "మొదటి నుండి రీసెట్ చేయాలా?" else "Reset to Beginning?", fontWeight = FontWeight.Bold) },
            text = { Text(if (isTelugu) "ఇది మీ ప్రొఫైల్‌ను తొలగించి, ఆరంభ సెటప్‌కు తీసుకెళ్తుంది." else "This will clear your profile and restart the setup from the beginning.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetApp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isTelugu) "ఇప్పుడే రీసెట్ చేయి" else "Reset Now", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isTelugu) "రద్దు చేయి" else "Cancel")
                }
            }
        )
    }
}

