package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.DailyPlanEntity
import com.example.backend.data.model.MealConfigItem
import com.example.backend.data.model.MealFoodSelection
import com.example.backend.data.model.RoutineScheduleConfig
import com.example.frontend.components.MealScheduleConfigContent
import com.example.frontend.theme.NutriTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTimingsScheduleScreen(
    dailyPlans: List<DailyPlanEntity>,
    reminderLeadMinutes: Int,
    is24HourFormat: Boolean = false,
    onSaveSchedule: (RoutineScheduleConfig) -> Unit,
    onOpenSimulator: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
    val defaultTimes = mapOf(
        "Breakfast" to "08:00",
        "Lunch" to "13:00",
        "Snacks" to "17:00",
        "Dinner" to "20:30"
    )
    val defaultWeekendTimes = mapOf(
        "Breakfast" to "09:30",
        "Lunch" to "14:00",
        "Snacks" to "17:30",
        "Dinner" to "21:00"
    )

    var separateWeekendSchedule by remember(dailyPlans) {
        mutableStateOf(dailyPlans.any { it.dayType == "WEEKEND" })
    }

    val weekdayTimes = remember(dailyPlans) {
        val map = mutableStateMapOf<String, String>()
        mealTypes.forEach { type ->
            val existing = dailyPlans.firstOrNull { 
                (it.dayType == "WEEKDAY" || it.dayType == "ALL") && it.mealType.equals(type, ignoreCase = true) 
            }?.mealTime
            map[type] = existing ?: defaultTimes[type] ?: "08:00"
        }
        map
    }

    val weekendTimes = remember(dailyPlans) {
        val map = mutableStateMapOf<String, String>()
        mealTypes.forEach { type ->
            val existing = dailyPlans.firstOrNull { 
                it.dayType == "WEEKEND" && it.mealType.equals(type, ignoreCase = true) 
            }?.mealTime
            map[type] = existing ?: defaultWeekendTimes[type] ?: defaultTimes[type] ?: "09:00"
        }
        map
    }

    var selectedLeadMinutes by remember(reminderLeadMinutes) { mutableIntStateOf(reminderLeadMinutes) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Weekdays, 1 = Weekends

    BackHandler {
        onNavigateBack()
    }

    fun handleSave() {
        val weekdayPlans = mealTypes.map { type ->
            val foods = dailyPlans.filter { 
                (it.dayType == "WEEKDAY" || it.dayType == "ALL") && it.mealType.equals(type, ignoreCase = true) 
            }.map { MealFoodSelection(it.foodId, it.plannedQuantity, it.quantityUnit) }

            MealConfigItem(
                mealType = type,
                mealTime = weekdayTimes[type] ?: "08:00",
                foods = foods,
                reminderEnabled = true
            )
        }

        val hasExistingWeekendPlans = dailyPlans.any { it.dayType == "WEEKEND" }
        val weekendPlans = if (separateWeekendSchedule) {
            mealTypes.map { type ->
                val matchingWeekend = dailyPlans.filter { 
                    it.dayType == "WEEKEND" && it.mealType.equals(type, ignoreCase = true) 
                }
                val sourcePlans = if (matchingWeekend.isNotEmpty() || hasExistingWeekendPlans) {
                    matchingWeekend
                } else {
                    dailyPlans.filter { 
                        (it.dayType == "WEEKDAY" || it.dayType == "ALL") && it.mealType.equals(type, ignoreCase = true) 
                    }
                }
                val foods = sourcePlans.map { MealFoodSelection(it.foodId, it.plannedQuantity, it.quantityUnit) }

                MealConfigItem(
                    mealType = type,
                    mealTime = weekendTimes[type] ?: "09:00",
                    foods = foods,
                    reminderEnabled = true
                )
            }
        } else emptyList()

        val config = RoutineScheduleConfig(
            weekdayPlans = weekdayPlans,
            weekendPlans = weekendPlans,
            separateWeekendSchedule = separateWeekendSchedule,
            reminderLeadMinutes = selectedLeadMinutes
        )
        onSaveSchedule(config)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Meal Timings & Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure meal hours & advance alerts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("meal_timings_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSimulator,
                        modifier = Modifier.testTag("open_simulator_timings_btn")
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Simulate Reminders",
                            tint = NutriTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { handleSave() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_meal_timings_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Meal Timings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        MealScheduleConfigContent(
            separateWeekendSchedule = separateWeekendSchedule,
            onToggleSeparateWeekend = { separateWeekendSchedule = it },
            activeTab = activeTab,
            onTabChange = { activeTab = it },
            weekdayTimes = weekdayTimes,
            weekendTimes = weekendTimes,
            onTimeChanged = { mealType, isWeekend, newTime ->
                if (isWeekend) {
                    weekendTimes[mealType] = newTime
                } else {
                    weekdayTimes[mealType] = newTime
                }
            },
            reminderLeadMinutes = selectedLeadMinutes,
            onLeadMinutesChange = { selectedLeadMinutes = it },
            is24Hour = is24HourFormat,
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        )
    }
}
