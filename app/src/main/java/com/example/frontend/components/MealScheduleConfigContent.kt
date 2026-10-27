package com.example.frontend.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.example.frontend.theme.*
import com.example.frontend.util.TimeFormatUtil

fun parseTime24String(time24: String): Pair<Int, Int> {
    return TimeFormatUtil.parseTimeHourMinute(time24)
}

fun formatTo12HourString(time24: String): String {
    return TimeFormatUtil.formatDisplayTime(time24, false)
}

/**
 * Common Meal Schedule & Timings Layout shared across Onboarding Step 7 and Profile Meal Timings Screen.
 * Includes:
 * 1. Weekend Schedule Toggle & Weekday/Weekend Tabs
 * 2. Meal Hours List (Breakfast, Lunch, Snacks, Dinner) with 12h AM/PM or 24h format & time picker dialog
 * 3. Preparation Alert Lead Time Selector (15, 30, 45, 60 mins)
 */
@Composable
fun MealScheduleConfigContent(
    separateWeekendSchedule: Boolean,
    onToggleSeparateWeekend: (Boolean) -> Unit,
    activeTab: Int,
    onTabChange: (Int) -> Unit,
    weekdayTimes: Map<String, String>,
    weekendTimes: Map<String, String>,
    onTimeChanged: (mealType: String, isWeekend: Boolean, newTime24: String) -> Unit,
    reminderLeadMinutes: Int,
    onLeadMinutesChange: (Int) -> Unit,
    is24Hour: Boolean = false,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(top = 12.dp, bottom = 28.dp)
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
    val isWeekendActive = separateWeekendSchedule && activeTab == 1
    val currentTimesMap = if (isWeekendActive) weekendTimes else weekdayTimes
    val periodLabel = if (isWeekendActive) "Weekend" else "Weekday"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding
    ) {
        if (headerContent != null) {
            item(key = "schedule_header_item") {
                headerContent()
            }
        }

        // Card 1: Weekend Schedule Toggle & Tabs
        item(key = "schedule_weekend_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Separate Weekend Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set different meal hours for Sat & Sun",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = separateWeekendSchedule,
                            onCheckedChange = onToggleSeparateWeekend,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NutriTeal
                            ),
                            modifier = Modifier.testTag("toggle_weekend_schedule")
                        )
                    }

                    if (separateWeekendSchedule) {
                        TabRow(
                            selectedTabIndex = activeTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            contentColor = NutriTeal,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = { onTabChange(0) },
                                text = { Text("Weekdays (Mon-Fri)", fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { onTabChange(1) },
                                text = { Text("Weekends (Sat-Sun)", fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        // Card 2: Meal Hours List with 12-Hour Badges
        item(key = "schedule_meal_hours_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NutriTeal.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$periodLabel Meal Hours",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any meal time badge to edit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    mealTypes.forEachIndexed { index, type ->
                        val timeStr = currentTimesMap[type] ?: if (type == "Breakfast") "08:00" else if (type == "Lunch") "13:00" else if (type == "Snacks") "17:00" else "20:30"
                        MealTimingRowItem(
                            mealType = type,
                            time24 = timeStr,
                            is24Hour = is24Hour,
                            onTimeChanged = { newTime ->
                                onTimeChanged(type, isWeekendActive, newTime)
                            }
                        )
                        if (index < mealTypes.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }

        // Card 3: Preparation Alert Lead Time
        item(key = "schedule_alert_lead_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NutriAmber.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = NutriAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Preparation Alert Lead Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Notifies you $reminderLeadMinutes mins before mealtime to prepare food",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val leadOptions = listOf(15, 30, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        leadOptions.forEach { mins ->
                            val selected = reminderLeadMinutes == mins
                            Surface(
                                onClick = { onLeadMinutesChange(mins) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) NutriTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) NutriTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$mins",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "mins",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = if (selected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealTimingRowItem(
    mealType: String,
    time24: String,
    is24Hour: Boolean = false,
    onTimeChanged: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    val icon = when (mealType.lowercase()) {
        "breakfast" -> Icons.Outlined.WbSunny
        "lunch" -> Icons.Outlined.AccessTime
        "snacks" -> Icons.Outlined.Schedule
        else -> Icons.Outlined.NightsStay
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NutriTeal.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = NutriTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = mealType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Scheduled time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Badge (12-Hour or 24-Hour based on preference)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NutriTeal.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.35f)),
            modifier = Modifier.clickable { isEditing = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = NutriTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = TimeFormatUtil.formatDisplayTime(time24, is24Hour, mealType),
                    fontWeight = FontWeight.Bold,
                    color = NutriTeal,
                    fontSize = 15.sp
                )
            }
        }
    }

    if (isEditing) {
        ModernTimePickerDialog(
            mealType = mealType,
            initialTime24 = time24,
            is24Hour = is24Hour,
            onDismiss = { isEditing = false },
            onTimeSelected = { newTime24 ->
                onTimeChanged(newTime24)
                isEditing = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTimePickerDialog(
    mealType: String,
    initialTime24: String,
    is24Hour: Boolean = false,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val (parsedHour, parsedMinute) = remember(initialTime24) { parseTime24String(initialTime24) }

    val timePickerState = rememberTimePickerState(
        initialHour = parsedHour,
        initialMinute = parsedMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val formatted = String.format(java.util.Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(formatted)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Set Time", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NutriTeal, fontWeight = FontWeight.Medium)
            }
        },
        title = {
            Text(
                text = "Set $mealType Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = NutriTeal,
                        timeSelectorSelectedContainerColor = NutriTeal.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = NutriTeal,
                        periodSelectorSelectedContainerColor = NutriAmber.copy(alpha = 0.3f),
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
