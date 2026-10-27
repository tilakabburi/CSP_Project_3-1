package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.frontend.components.ExpandableSearchHeader
import com.example.frontend.components.FoodIconHelper
import com.example.frontend.components.MacroAppIcon
import com.example.frontend.components.MealScheduleConfigContent
import com.example.frontend.components.MiniCalorieGoalStickyTracker
import com.example.frontend.components.RoutineMealProgressBarsCard
import com.example.frontend.components.orderFoodsByMacroCycle
import com.example.frontend.components.organizeMealRoutineFoods
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.dismissKeyboardOnTap
import com.example.frontend.util.trackKeyboardFocusBounds

/**
 * Onboarding Step 6: Meal Routine & Items Selection
 * Allows users to choose foods from their selected food pool and assign them to meals with portion sizes.
 */
@Composable
fun Step6CustomizeMealItems(
    poolFoodIds: List<Long>,
    allFoods: List<FoodEntity>,
    weekdayMeals: List<MutableMealSchedule>,
    weekendMeals: List<MutableMealSchedule>,
    separateWeekendSchedule: Boolean,
    activeTab: Int,
    target: NutritionTargetEntity? = null,
    lang: AppLanguage = LocalAppLanguage.current
) {
    val poolFoods = remember(allFoods, poolFoodIds) {
        val base = allFoods.filter { it.id in poolFoodIds }
        orderFoodsByMacroCycle(base)
    }

    var poolSearchQuery by remember { mutableStateOf("") }
    var sortTrigger by remember { mutableIntStateOf(0) }

    val currentMeals = if (separateWeekendSchedule && activeTab == 1) weekendMeals else weekdayMeals
    val isWeekend = separateWeekendSchedule && activeTab == 1

    val assignedFoodIds = remember(sortTrigger, isWeekend) {
        currentMeals.flatMap { it.foods.map { f -> f.foodId } }.toSet()
    }

    val displayPoolFoods = remember(poolFoods, poolSearchQuery, assignedFoodIds) {
        val matches = if (poolSearchQuery.isBlank()) poolFoods
        else poolFoods.filter { it.name.contains(poolSearchQuery, ignoreCase = true) }
        organizeMealRoutineFoods(matches, assignedFoodIds)
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // When the user scrolls past the full Calorie Goal card, the mini version remains static at the top
    val isScrolledPastCard by remember {
        derivedStateOf {
            val fullCardIndex = if (separateWeekendSchedule) 2 else 1
            listState.firstVisibleItemIndex > fullCardIndex ||
                (listState.firstVisibleItemIndex == fullCardIndex && listState.firstVisibleItemScrollOffset > 30)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dismissKeyboardOnTap(focusManager, keyboardController)
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .dismissKeyboardOnTap(focusManager, keyboardController),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Step Header
        item {
            Text(
                text = NutriStrings.step6Title(lang),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = NutriStrings.step6Subtitle(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weekend Tab Row (if separate weekend enabled)
        if (separateWeekendSchedule) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (activeTab == 1)
                            (if (lang == AppLanguage.TELUGU) "వారాంతపు ఆహార ఎంపికలను సవరిస్తున్నారు" else "Editing Weekend Food Selection")
                        else
                            (if (lang == AppLanguage.TELUGU) "వారపు రోజుల ఆహార ఎంపికలను సవరిస్తున్నారు" else "Editing Weekday Food Selection"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NutriTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Section: Four Meal Progress Bars (Breakfast, Lunch, Snacks, Dinner)
        item {
            RoutineMealProgressBarsCard(
                currentMeals = currentMeals,
                allFoods = allFoods,
                target = target
            )
        }

        // Section Header & Search Bar
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ExpandableSearchHeader(
                searchQuery = poolSearchQuery,
                onSearchQueryChange = { poolSearchQuery = it },
                placeholderText = "Item name...",
                titleContent = {
                    Column {
                        Text(
                            text = "Choose Foods for Each Meal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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


        if (displayPoolFoods.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (poolSearchQuery.isBlank()) "No foods selected yet in Step 4. You can go back to Step 4 to select foods." else "No matching foods in your pool for \"$poolSearchQuery\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(displayPoolFoods, key = { "step6_pool_food_${if (isWeekend) "wknd" else "wkday"}_${it.id}" }) { food ->
                Box(modifier = Modifier.animateItem()) {
                    OnboardingFoodAssignmentRow(
                        food = food,
                        currentMeals = currentMeals
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Static / Sticky Mini Version of Calorie Goal Tracker when scrolled past header
    AnimatedVisibility(
        visible = isScrolledPastCard,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        MiniCalorieGoalStickyTracker(
            currentMeals = currentMeals,
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

/**
 * Onboarding Step 7: Meal Timings & Schedule
 * Allows users to set meal hours, 12-hour AM/PM schedules, separate weekend switches, and alert lead times.
 */
@Composable
fun Step7MealTimingsSchedule(
    weekdayMeals: List<MutableMealSchedule>,
    weekendMeals: List<MutableMealSchedule>,
    separateWeekendSchedule: Boolean,
    onToggleSeparateWeekend: (Boolean) -> Unit,
    activeTab: Int,
    onTabChange: (Int) -> Unit,
    reminderLeadMinutes: Int,
    onLeadMinutesChange: (Int) -> Unit,
    lang: AppLanguage = LocalAppLanguage.current
) {
    var changeToken by remember { mutableIntStateOf(0) }
    val weekdayTimes = remember(weekdayMeals, changeToken) {
        weekdayMeals.associate { it.mealType to it.mealTime }
    }
    val weekendTimes = remember(weekendMeals, changeToken) {
        weekendMeals.associate { it.mealType to it.mealTime }
    }

    MealScheduleConfigContent(
        separateWeekendSchedule = separateWeekendSchedule,
        onToggleSeparateWeekend = onToggleSeparateWeekend,
        activeTab = activeTab,
        onTabChange = onTabChange,
        weekdayTimes = weekdayTimes,
        weekendTimes = weekendTimes,
        onTimeChanged = { mealType, isWeekend, newTime ->
            val list = if (isWeekend) weekendMeals else weekdayMeals
            list.firstOrNull { it.mealType.equals(mealType, ignoreCase = true) }?.let {
                it.mealTime = newTime
            }
            changeToken++
        },
        reminderLeadMinutes = reminderLeadMinutes,
        onLeadMinutesChange = onLeadMinutesChange,
        headerContent = {
            Column {
                Text(
                    text = NutriStrings.step7Title(lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = NutriStrings.step7Subtitle(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        contentPadding = PaddingValues(bottom = 24.dp)
    )
}

/**
 * Legacy wrapper function for backwards compatibility
 */
@Composable
fun Step5CustomizeMealRoutine(
    poolFoodIds: List<Long>,
    allFoods: List<FoodEntity>,
    weekdayMeals: List<MutableMealSchedule>,
    weekendMeals: List<MutableMealSchedule>,
    separateWeekendSchedule: Boolean,
    onToggleSeparateWeekend: (Boolean) -> Unit,
    activeTab: Int,
    onTabChange: (Int) -> Unit,
    reminderLeadMinutes: Int,
    onLeadMinutesChange: (Int) -> Unit,
    target: NutritionTargetEntity? = null
) {
    Step6CustomizeMealItems(
        poolFoodIds = poolFoodIds,
        allFoods = allFoods,
        weekdayMeals = weekdayMeals,
        weekendMeals = weekendMeals,
        separateWeekendSchedule = separateWeekendSchedule,
        activeTab = activeTab,
        target = target
    )
}

fun format12HourTime(rawTime: String, defaultMealType: String = ""): String {
    val clean = rawTime.trim()
    if (clean.contains("AM", ignoreCase = true) || clean.contains("PM", ignoreCase = true)) {
        return clean
    }
    val parts = clean.split(":")
    if (parts.size >= 2) {
        val rawH = parts[0].trim().toIntOrNull() ?: 8
        val m = parts[1].trim().toIntOrNull() ?: 0
        val isPm = rawH >= 12
        val displayH = when {
            rawH == 0 -> 12
            rawH > 12 -> rawH - 12
            else -> rawH
        }
        val period = if (isPm) "PM" else "AM"
        return String.format(java.util.Locale.US, "%02d:%02d %s", displayH, m, period)
    }
    val defaultPeriod = if (defaultMealType.equals("Breakfast", ignoreCase = true)) "AM" else "PM"
    return "$clean $defaultPeriod"
}

fun parse24HourParts(rawTime: String, defaultMealType: String = ""): Pair<Int, Int> {
    val clean = rawTime.trim()
    val isExplicitPm = clean.endsWith("PM", ignoreCase = true)
    val isExplicitAm = clean.endsWith("AM", ignoreCase = true)
    val stripped = clean.replace("(?i)am|pm".toRegex(), "").trim()
    val parts = stripped.split(":")
    var h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: if (defaultMealType.equals("Breakfast", ignoreCase = true)) 8 else 13
    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

    if (isExplicitPm && h < 12) h += 12
    if (isExplicitAm && h == 12) h = 0
    return Pair(h.coerceIn(0, 23), m.coerceIn(0, 59))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingMealTimingCard(
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
        modifier = Modifier.fillMaxWidth(),
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
                    // Small Time Selection Box (user can tap to pick custom time via Material 3 TimePicker)
                    Surface(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = NutriTeal.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("onboard_time_box_${mealSchedule.mealType.lowercase()}")
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
fun OnboardingFoodAssignmentRow(
    food: FoodEntity,
    currentMeals: List<MutableMealSchedule>
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Find which meal this food is currently assigned to (if any)
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
                            .testTag("onboarding_custom_grams_${food.id}")
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
                                coroutineScope.launch {
                                    delay(50)
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
                            }
                        )
                    }
                }
            }
        }
    }
}
