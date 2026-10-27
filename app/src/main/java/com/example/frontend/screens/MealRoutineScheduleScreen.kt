package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.backend.data.local.entity.DailyPlanEntity
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.FoodPoolEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.data.model.MealConfigItem
import com.example.backend.data.model.MealFoodSelection
import com.example.backend.data.model.RoutineScheduleConfig
import com.example.frontend.components.ExpandableSearchHeader
import com.example.frontend.components.organizeMealRoutineFoods
import com.example.frontend.components.orderFoodsByMacroCycle
import com.example.frontend.components.FoodIconHelper
import com.example.frontend.components.MacroAppIcon
import com.example.frontend.components.MiniCalorieGoalStickyTracker
import com.example.frontend.components.RoutineMealProgressBarsCard
import com.example.frontend.components.RoutineSaveReviewDialog
import com.example.frontend.theme.*
import com.example.frontend.util.dismissKeyboardOnTap
import com.example.frontend.util.trackKeyboardFocusBounds

data class MutableMealFood(
    val foodId: Long,
    var plannedQuantity: Double,
    val quantityUnit: String
)

class MutableMealSchedule(
    val mealType: String,
    var mealTime: String,
    initialFoods: List<MutableMealFood> = emptyList(),
    var reminderEnabled: Boolean = true
) {
    val foods: SnapshotStateList<MutableMealFood> =
        mutableStateListOf<MutableMealFood>().apply {
            addAll(initialFoods)
        }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MealRoutineScheduleScreen(
    dailyPlans: List<DailyPlanEntity>,
    allFoods: List<FoodEntity>,
    foodPool: List<FoodPoolEntity>,
    reminderLeadMinutes: Int,
    onSaveRoutine: (RoutineScheduleConfig) -> Unit,
    onOpenSimulator: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    target: NutritionTargetEntity? = null
) {
    val poolFoodIds = remember(foodPool) { foodPool.map { it.foodId }.toSet() }
    val poolFoods = remember(allFoods, poolFoodIds) {
        val base = allFoods.filter { it.id in poolFoodIds }
        orderFoodsByMacroCycle(base)
    }

    var poolSearchQuery by remember { mutableStateOf("") }
    var showRoutineReviewDialog by remember { mutableStateOf(false) }

    val filteredPoolFoods = remember(poolFoods, poolSearchQuery) {
        if (poolSearchQuery.isBlank()) poolFoods
        else poolFoods.filter { it.name.contains(poolSearchQuery, ignoreCase = true) }
    }

    var selectedLeadMinutes by remember(reminderLeadMinutes) { mutableIntStateOf(reminderLeadMinutes) }
    var separateWeekendSchedule by remember(dailyPlans) {
        mutableStateOf(dailyPlans.any { it.dayType == "WEEKEND" })
    }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Weekdays, 1 = Weekends
    var showSaveSnackbar by remember { mutableStateOf(false) }
    var sortTrigger by remember { mutableIntStateOf(0) }

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

    // Weekday schedules state
    val weekdaySchedules = remember(dailyPlans) {
        val list = mutableStateListOf<MutableMealSchedule>()
        val weekdayPlans = dailyPlans.filter { it.dayType == "WEEKDAY" || it.dayType == "ALL" }
        mealTypes.forEach { type ->
            val matchingPlans = weekdayPlans.filter { it.mealType.equals(type, ignoreCase = true) }
            val time = matchingPlans.firstOrNull()?.mealTime ?: defaultTimes[type] ?: "08:00"
            val foods = matchingPlans.map {
                MutableMealFood(it.foodId, it.plannedQuantity, it.quantityUnit)
            }.toMutableList()
            list.add(MutableMealSchedule(type, time, foods))
        }
        list
    }

    // Weekend schedules state (seeds from weekday/daily plans if no separate weekend plans exist yet)
    val weekendSchedules = remember(dailyPlans) {
        val list = mutableStateListOf<MutableMealSchedule>()
        val weekendPlans = dailyPlans.filter { it.dayType == "WEEKEND" }
        val fallbackPlans = if (weekendPlans.isEmpty()) {
            dailyPlans.filter { it.dayType == "WEEKDAY" || it.dayType == "ALL" }
        } else {
            emptyList()
        }
        mealTypes.forEach { type ->
            val matchingPlans = weekendPlans.filter { it.mealType.equals(type, ignoreCase = true) }
            val sourcePlans = if (matchingPlans.isNotEmpty() || weekendPlans.isNotEmpty()) {
                matchingPlans
            } else {
                fallbackPlans.filter { it.mealType.equals(type, ignoreCase = true) }
            }
            val time = matchingPlans.firstOrNull()?.mealTime
                ?: defaultWeekendTimes[type] ?: defaultTimes[type] ?: "09:00"
            val foods = sourcePlans.map {
                MutableMealFood(it.foodId, it.plannedQuantity, it.quantityUnit)
            }.toMutableList()
            list.add(MutableMealSchedule(type, time, foods))
        }
        list
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val isCurrentWeekend = separateWeekendSchedule && activeTab == 1
    val currentSchedules = if (isCurrentWeekend) weekendSchedules else weekdaySchedules

    val totalPlannedCalories = remember(currentSchedules.map { it.foods.toList() }) {
        currentSchedules.sumOf { schedule ->
            schedule.foods.sumOf { f ->
                val food = allFoods.firstOrNull { it.id == f.foodId }
                (food?.calories ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
            }
        }
    }
    val totalPlannedProtein = remember(currentSchedules.map { it.foods.toList() }) {
        currentSchedules.sumOf { schedule ->
            schedule.foods.sumOf { f ->
                val food = allFoods.firstOrNull { it.id == f.foodId }
                (food?.protein ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
            }
        }
    }
    val totalPlannedCarbs = remember(currentSchedules.map { it.foods.toList() }) {
        currentSchedules.sumOf { schedule ->
            schedule.foods.sumOf { f ->
                val food = allFoods.firstOrNull { it.id == f.foodId }
                (food?.carbs ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
            }
        }
    }
    val totalPlannedFat = remember(currentSchedules.map { it.foods.toList() }) {
        currentSchedules.sumOf { schedule ->
            schedule.foods.sumOf { f ->
                val food = allFoods.firstOrNull { it.id == f.foodId }
                (food?.fat ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
            }
        }
    }
    val totalPlannedFiber = remember(currentSchedules.map { it.foods.toList() }) {
        currentSchedules.sumOf { schedule ->
            schedule.foods.sumOf { f ->
                val food = allFoods.firstOrNull { it.id == f.foodId }
                (food?.fiber ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
            }
        }
    }

    val sortedAssignedFoodIds = remember(dailyPlans, sortTrigger, isCurrentWeekend) {
        currentSchedules.flatMap { it.foods.map { f -> f.foodId } }.toSet()
    }

    val displayPoolFoods = remember(poolFoods, poolSearchQuery, sortedAssignedFoodIds) {
        val matches = if (poolSearchQuery.isBlank()) poolFoods
        else poolFoods.filter { it.name.contains(poolSearchQuery, ignoreCase = true) }
        organizeMealRoutineFoods(matches, sortedAssignedFoodIds)
    }

    fun commitSave() {
        val config = RoutineScheduleConfig(
            weekdayPlans = weekdaySchedules.map { schedule ->
                MealConfigItem(
                    mealType = schedule.mealType,
                    mealTime = schedule.mealTime,
                    foods = schedule.foods.map {
                        MealFoodSelection(it.foodId, it.plannedQuantity, it.quantityUnit)
                    },
                    reminderEnabled = schedule.reminderEnabled
                )
            },
            weekendPlans = if (separateWeekendSchedule) {
                weekendSchedules.map { schedule ->
                    MealConfigItem(
                        mealType = schedule.mealType,
                        mealTime = schedule.mealTime,
                        foods = schedule.foods.map {
                            MealFoodSelection(it.foodId, it.plannedQuantity, it.quantityUnit)
                        },
                        reminderEnabled = schedule.reminderEnabled
                    )
                }
            } else emptyList(),
            separateWeekendSchedule = separateWeekendSchedule,
            reminderLeadMinutes = selectedLeadMinutes
        )
        onSaveRoutine(config)
        sortTrigger++
        showSaveSnackbar = true
    }

    if (showRoutineReviewDialog) {
        RoutineSaveReviewDialog(
            totalCalories = totalPlannedCalories,
            totalProtein = totalPlannedProtein,
            totalCarbs = totalPlannedCarbs,
            totalFat = totalPlannedFat,
            totalFiber = totalPlannedFiber,
            target = target,
            onEdit = { showRoutineReviewDialog = false },
            onContinue = {
                showRoutineReviewDialog = false
                commitSave()
            }
        )
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Meal Food Routine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pick pantry foods & quantities for each meal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("routine_schedule_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSimulator,
                        modifier = Modifier.testTag("open_simulator_btn")
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (showSaveSnackbar) "Done" else "Cancel")
                    }

                    Button(
                        onClick = {
                            commitSave()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_meal_routine_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Routine", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(showSaveSnackbar) {
            if (showSaveSnackbar) {
                snackbarHostState.showSnackbar("Meal routine & reminders updated successfully!")
                showSaveSnackbar = false
            }
        }

        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }

        val isScrolledPastCard by remember {
            derivedStateOf {
                val fullCardIndex = if (separateWeekendSchedule) 2 else 1
                listState.firstVisibleItemIndex > fullCardIndex ||
                    (listState.firstVisibleItemIndex == fullCardIndex && listState.firstVisibleItemScrollOffset > 25)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dismissKeyboardOnTap(focusManager, keyboardController)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                // Title Header
                item {
                    Text(
                        text = "Meal Routine & Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Assign foods from your pool to each daily meal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab Row: Weekday vs Weekend (if separate weekend enabled)
                if (separateWeekendSchedule) {
                    item {
                        TabRow(
                            selectedTabIndex = activeTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = NutriTeal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                text = {
                                    Text(
                                        text = "Weekdays (Mon - Fri)",
                                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(Icons.Default.WorkOutline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                text = {
                                    Text(
                                        text = "Weekends (Sat - Sun)",
                                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(Icons.Default.Weekend, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                // Full Rich Calorie Goal & 4 Meal Progress Card
                item {
                    RoutineMealProgressBarsCard(
                        currentMeals = currentSchedules,
                        allFoods = allFoods,
                        target = target
                    )
                }

                // Search Pool Items Bar & Section Header
                item {
                    ExpandableSearchHeader(
                        searchQuery = poolSearchQuery,
                        onSearchQueryChange = { poolSearchQuery = it },
                        placeholderText = "Item name...",
                        titleContent = {
                            Column {
                                Text(
                                    text = "Choose Foods for Each Meal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Select meal assignments and portions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                // Status & quick Move Selected Up action
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sortedAssignedFoodIds.size} of ${displayPoolFoods.size} items assigned",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sortedAssignedFoodIds.isNotEmpty()) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (sortedAssignedFoodIds.isNotEmpty()) {
                            TextButton(
                                onClick = { sortTrigger++ },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = NutriTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Move Selected Up",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NutriTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            if (displayPoolFoods.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (poolSearchQuery.isBlank()) "No foods selected yet. Add foods from the Food Pool first." else "No matching foods in your pool for \"$poolSearchQuery\".",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(displayPoolFoods, key = { "routine_pool_food_${if (isCurrentWeekend) "wknd" else "wkday"}_${it.id}" }) { food ->
                    Box(modifier = Modifier.animateItem()) {
                        RoutineFoodAssignmentRow(
                            food = food,
                            currentMeals = currentSchedules
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Static / Sticky Mini Version of Calorie Goal Tracker when scrolled past card
        AnimatedVisibility(
            visible = isScrolledPastCard,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp)
        ) {
            MiniCalorieGoalStickyTracker(
                currentMeals = currentSchedules,
                allFoods = allFoods,
                target = target,
                onScrollToTop = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineMealTimingCard(
    mealSchedule: MutableMealSchedule,
    isWeekend: Boolean
) {
    var mealTime by remember(mealSchedule.mealTime) {
        mutableStateOf(format12HourTime(mealSchedule.mealTime, mealSchedule.mealType))
    }
    var reminderEnabled by remember(mealSchedule.reminderEnabled) {
        mutableStateOf(mealSchedule.reminderEnabled)
    }
    var showTimePicker by remember { mutableStateOf(false) }

    val timePresets = when (mealSchedule.mealType) {
        "Breakfast" -> if (isWeekend) listOf("09:00 AM", "09:30 AM", "10:00 AM") else listOf("07:30 AM", "08:00 AM", "08:30 AM")
        "Lunch" -> if (isWeekend) listOf("01:30 PM", "02:00 PM", "02:30 PM") else listOf("12:30 PM", "01:00 PM", "01:30 PM")
        "Snacks" -> if (isWeekend) listOf("05:00 PM", "05:30 PM") else listOf("04:30 PM", "05:00 PM", "05:30 PM")
        else -> if (isWeekend) listOf("08:30 PM", "09:00 PM") else listOf("07:30 PM", "08:00 PM", "08:30 PM")
    }

    if (showTimePicker) {
        val (initHour, initMin) = parse24HourParts(mealTime, mealSchedule.mealType)
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMin,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Set ${mealSchedule.mealType} Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val raw24 = String.format(java.util.Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        val formatted = format12HourTime(raw24, mealSchedule.mealType)
                        mealTime = formatted
                        mealSchedule.mealTime = formatted
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                ) {
                    Text("Set Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_meal_card_${mealSchedule.mealType.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Meal Icon + Name, Custom Time Box, Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val icon = when (mealSchedule.mealType) {
                        "Breakfast" -> Icons.Default.WbSunny
                        "Lunch" -> Icons.Default.LunchDining
                        "Snacks" -> Icons.Default.Coffee
                        else -> Icons.Default.Nightlife
                    }
                    val iconTint = when (mealSchedule.mealType) {
                        "Breakfast" -> NutriAmber
                        "Lunch" -> NutriTeal
                        "Snacks" -> NutriWaterCyan
                        else -> Color(0xFF8B5CF6)
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealSchedule.mealType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Custom time selection box
                    Surface(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = NutriTeal.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("routine_time_box_${mealSchedule.mealType.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Edit Time",
                                tint = NutriTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = format12HourTime(mealTime, mealSchedule.mealType),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NutriTeal
                            )
                        }
                    }

                    // Alert Toggle Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (reminderEnabled) "Alert ON" else "Alert OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (reminderEnabled) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = {
                                reminderEnabled = it
                                mealSchedule.reminderEnabled = it
                            },
                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recommended Timings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recommended:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timePresets) { preset ->
                        val isSelected = mealTime == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                mealTime = preset
                                mealSchedule.mealTime = preset
                            },
                            label = { Text(preset, fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NutriTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineFoodAssignmentRow(
    food: FoodEntity,
    currentMeals: List<MutableMealSchedule>
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val assignedMeal = currentMeals.firstOrNull { meal ->
        meal.foods.any { it.foodId == food.id }
    }
    val assignedFood = assignedMeal?.foods?.firstOrNull { it.foodId == food.id }
    val isChecked = assignedMeal != null

    var selectedMealType by remember(assignedMeal?.mealType) {
        mutableStateOf(assignedMeal?.mealType ?: "Select")
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentDisplayMeal = assignedMeal?.mealType ?: if (selectedMealType != "Select") selectedMealType else "Select"
    val isAssigned = isChecked && currentDisplayMeal != "Select"

    // Custom grams / quantity state for this food item
    var customQuantityText by remember(assignedFood?.plannedQuantity) {
        val qty = assignedFood?.plannedQuantity ?: food.referenceQuantity
        mutableStateOf(if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString())
    }

    val parsedQty = customQuantityText.toDoubleOrNull()
    val effectiveQuantity = if (parsedQty != null && parsedQty > 0) parsedQty else (assignedFood?.plannedQuantity ?: food.referenceQuantity)
    val factor = effectiveQuantity / food.referenceQuantity.coerceAtLeast(1.0)

    val calcCalories = (food.calories * factor).toInt()
    val calcProtein = (food.protein * factor).toInt()
    val calcCarbs = (food.carbs * factor).toInt()
    val calcFat = (food.fat * factor).toInt()

    fun updateQuantity(newText: String) {
        val filtered = newText.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1 && filtered.length <= 5) {
            customQuantityText = filtered
            val parsed = filtered.toDoubleOrNull()
            if (parsed != null && parsed > 0.0 && assignedMeal != null) {
                val idx = assignedMeal.foods.indexOfFirst { it.foodId == food.id }
                if (idx >= 0) {
                    assignedMeal.foods[idx] = assignedMeal.foods[idx].copy(plannedQuantity = parsed)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) NutriTealContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isChecked) NutriTeal.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Icon using custom macro shape
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAssigned) NutriTeal.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                MacroAppIcon(
                    macroOrCategory = food.category,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Item Name and below that dynamically calculated Nutrition based on custom grams
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$calcCalories kcal • ${if (effectiveQuantity % 1.0 == 0.0) effectiveQuantity.toInt().toString() else effectiveQuantity.toString()}${food.referenceUnit.ifBlank { "g" }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "P:${calcProtein}g • C:${calcCarbs}g • F:${calcFat}g",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Small box for each item to enter custom grams
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAssigned) NutriTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    if (isAssigned) NutriTeal.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .width(68.dp)
                    .height(38.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                ) {
                    BasicTextField(
                        value = customQuantityText,
                        onValueChange = { updateQuantity(it) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .trackKeyboardFocusBounds()
                            .testTag("custom_grams_input_${food.id}")
                    )
                    Text(
                        text = if (food.referenceUnit.equals("piece", ignoreCase = true) || food.referenceUnit.equals("pcs", ignoreCase = true)) "pc" else food.referenceUnit.ifBlank { "g" },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Dropdown selection box to select Breakfast, Lunch, Snacks, Dinner, or None
            Box {
                Surface(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        dropdownExpanded = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAssigned) NutriTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isAssigned) NutriTeal else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentDisplayMeal,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Meal",
                            tint = if (isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        clippingEnabled = true
                    )
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "None (Unassigned)",
                                fontWeight = if (!isAssigned) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isAssigned) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            dropdownExpanded = false
                            selectedMealType = "Select"
                            currentMeals.forEach { it.foods.removeAll { f -> f.foodId == food.id } }
                        }
                    )
                    listOf("Breakfast", "Lunch", "Snacks", "Dinner").forEach { mealName ->
                        val isCurrent = isAssigned && currentDisplayMeal == mealName
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mealName,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) NutriTeal else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                dropdownExpanded = false
                                selectedMealType = mealName
                                currentMeals.forEach { it.foods.removeAll { f -> f.foodId == food.id } }
                                val targetMeal = currentMeals.firstOrNull { it.mealType.equals(mealName, ignoreCase = true) }
                                val qtyToUse = customQuantityText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: food.referenceQuantity
                                targetMeal?.foods?.add(
                                    MutableMealFood(
                                        foodId = food.id,
                                        plannedQuantity = qtyToUse,
                                        quantityUnit = food.referenceUnit
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
