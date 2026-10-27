package com.example.frontend.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.service.SimulatedNotificationInfo
import com.example.frontend.theme.NutriAmber
import com.example.frontend.theme.NutriTeal
import com.example.frontend.theme.NutriWaterCyan

@Composable
fun NotificationSimulatorDialog(
    onDismiss: () -> Unit,
    onTriggerStage1: () -> Unit,
    onTriggerStage2: () -> Unit,
    onTriggerStage3: () -> Unit,
    onTriggerWaterReminder: () -> Unit = {},
    onTriggerFullDay: () -> Unit = {},
    getFullDayList: () -> List<SimulatedNotificationInfo> = { emptyList() },
    onTriggerSingleItem: (SimulatedNotificationInfo) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Simulator Launcher, 1: 24h Verification Timeline
    var notificationsSentMessage by remember { mutableStateOf<String?>(null) }

    val fullDayItems = remember { getFullDayList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NutriTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = NutriTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Simulator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Test & verify your daily alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // Tab Row for easy switching between quick triggers and full-day verification timeline
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = NutriTeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Simulate Triggers",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Verify 24h Flow (12)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notificationsSentMessage != null) {
                    Surface(
                        color = NutriTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notificationsSentMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = NutriTeal
                            )
                        }
                    }
                }

                // Scrollable container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        // TAB 0: SIMULATE TRIGGERS
                        // 1. Featured Simulation: Overall One Day (All 12 Notifications)
                        Card(
                            onClick = {
                                onTriggerFullDay()
                                notificationsSentMessage = "All 12 daily notifications fired! Pull down status bar to verify."
                                Toast.makeText(
                                    context,
                                    "🔔 12 Notifications sent! Check your notification bar.",
                                    Toast.LENGTH_LONG
                                ).show()
                                selectedTab = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulate_overall_one_day_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = NutriTeal.copy(alpha = 0.14f)
                            ),
                            border = BorderStroke(1.5.dp, NutriTeal)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = NutriTeal,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ALL 12 ALERTS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = "🌟", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Simulate Overall One Day",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal
                                )
                                Text(
                                    text = "Fires all daily notifications at once: Pre-meal alerts, Breakfast, Lunch, Snacks, Dinner, Water reminders & Evening review.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onTriggerFullDay()
                                        notificationsSentMessage = "All 12 daily notifications fired! Pull down status bar to verify."
                                        Toast.makeText(
                                            context,
                                            "🔔 12 Notifications sent! Check your notification bar.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        selectedTab = 1
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fire_all_12_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fire All 12 Daily Notifications", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "Or test individual notifications:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Stage 1 Button (Pre-Meal)
                        Card(
                            onClick = {
                                onTriggerStage1()
                                notificationsSentMessage = "Pre-Meal Preparation alert fired to status bar."
                                Toast.makeText(context, "Pre-Meal alert sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_stage_1_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⏰", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "1. Pre-Meal Preparation Alert",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Alerts you ahead of time with planned food items to prepare.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Stage 2 Button (Meal Time)
                        Card(
                            onClick = {
                                onTriggerStage2()
                                notificationsSentMessage = "Mealtime alert fired with [Consumed] and [Will] action buttons."
                                Toast.makeText(context, "Mealtime alert sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_stage_2_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = NutriTeal.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🍽️", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "2. Mealtime Check (Action Buttons)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriTeal
                                    )
                                    Text(
                                        text = "Alerts at mealtime with [Consumed] and [Will Eat] actions.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Water Reminder Button
                        Card(
                            onClick = {
                                onTriggerWaterReminder()
                                notificationsSentMessage = "Water reminder alert fired with [+250 ml] and [+500 ml] buttons."
                                Toast.makeText(context, "Water reminder sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_water_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = NutriWaterCyan.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💧", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "3. Water / Hydration Reminder",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriWaterCyan
                                    )
                                    Text(
                                        text = "\"You have to drink water\" with [+250 ml] & [+500 ml] buttons.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Stage 3 Button (Evening Review)
                        Card(
                            onClick = {
                                onTriggerStage3()
                                notificationsSentMessage = "End-of-day recovery review alert fired to status bar."
                                Toast.makeText(context, "Evening review alert sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_stage_3_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = NutriAmber.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌙", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "4. Evening Review Alert",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriAmber
                                    )
                                    Text(
                                        text = "Reminds you to check and log any missing meals from today.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        // TAB 1: 24-HOUR VERIFICATION TIMELINE (OVERALL ONE DAY)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1-Day Notification Schedule (${fullDayItems.size} Alerts)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalButton(
                                onClick = {
                                    onTriggerFullDay()
                                    notificationsSentMessage = "Re-sent all 12 notifications to status bar!"
                                    Toast.makeText(context, "Re-fired all 12 daily notifications!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("refire_all_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-Fire All", fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Here is how all notifications come throughout the day. Pull down your phone's notification bar to inspect the real notifications, or test any one individually below:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        fullDayItems.forEachIndexed { index, item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("timeline_item_${item.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (item.stageType) {
                                        1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        2 -> NutriTeal.copy(alpha = 0.08f)
                                        3 -> NutriAmber.copy(alpha = 0.08f)
                                        4 -> NutriWaterCyan.copy(alpha = 0.08f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    when (item.stageType) {
                                        1 -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        2 -> NutriTeal.copy(alpha = 0.4f)
                                        3 -> NutriAmber.copy(alpha = 0.4f)
                                        4 -> NutriWaterCyan.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(item.iconEmoji, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.timeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = when (item.stageType) {
                                                1 -> MaterialTheme.colorScheme.secondaryContainer
                                                2 -> NutriTeal.copy(alpha = 0.2f)
                                                3 -> NutriAmber.copy(alpha = 0.2f)
                                                4 -> NutriWaterCyan.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.stageCategory,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (item.stageType) {
                                                    2 -> NutriTeal
                                                    3 -> NutriAmber
                                                    4 -> NutriWaterCyan
                                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = {
                                                onTriggerSingleItem(item)
                                                Toast.makeText(context, "Alert fired: ${item.title}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.NotificationsActive,
                                                contentDescription = "Test this notification",
                                                tint = NutriTeal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = item.detailText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (item.actions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Actions:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            item.actions.forEach { action ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Text(
                                                        text = action,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_simulator_btn")
            ) {
                Text("Close")
            }
        }
    )
}
