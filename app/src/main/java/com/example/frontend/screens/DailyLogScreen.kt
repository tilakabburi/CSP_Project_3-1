package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.DailyPlanEntity
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.FoodPoolEntity
import com.example.backend.data.local.entity.MealLogEntity
import com.example.backend.service.NutritionCalculationService
import com.example.frontend.components.AddCustomFoodDialog
import com.example.frontend.components.ExpandableSearchHeader
import com.example.frontend.components.MacroAppIcon
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.trackKeyboardFocusBounds
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    selectedDate: String,
    mealLogs: List<MealLogEntity>,
    allFoods: List<FoodEntity>,
    dailyPlans: List<DailyPlanEntity> = emptyList(),
    foodPool: List<FoodPoolEntity> = emptyList(),
    onDateChanged: (String) -> Unit,
    onMarkConsumed: (Long) -> Unit,
    onMarkWillEat: (Long) -> Unit,
    onSubstitute: (Long) -> Unit,
    onLogExtraFood: (mealType: String, foodId: Long, quantity: Double, unit: String) -> Unit = { _, _, _, _ -> },
    onOpenSubstituteForMeal: ((mealType: String, initialMode: String) -> Unit)? = null,
    onDeleteSubstituteLog: (Long) -> Unit = {},
    onRestoreOriginalFood: (Long) -> Unit = {},
    onCreateCustomFood: (String, String, Double, String, Double, Double, Double, Double, Double, (Long) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val foodsMap = remember(allFoods) { allFoods.associateBy { it.id } }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showSubstituteOptionsSheet by remember { mutableStateOf(false) }

    // Filter categories: Only Breakfast, Lunch, Snacks, Dinner
    val mealFilters = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
    var selectedMealFilter by remember { mutableStateOf("Breakfast") }

    // Filter logs for selected meal category
    val logsForCategory = remember(mealLogs, selectedMealFilter) {
        mealLogs.filter { it.mealType.equals(selectedMealFilter, ignoreCase = true) }
    }

    // Logs for selected meal category, sorted by ID for chronological audit display
    val categoryMealLogs = remember(logsForCategory) {
        logsForCategory.sortedBy { it.id }
    }

    // Total substitutes used across the whole selected day
    val allDaySubstitutesCount = remember(mealLogs) {
        mealLogs.count { it.status.equals("substituted", ignoreCase = true) }
    }

    val formattedShortDate = remember(selectedDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("MMM d", Locale.getDefault())
            val parsed = sdf.parse(selectedDate)
            if (parsed != null) out.format(parsed) else selectedDate
        } catch (_: Exception) {
            selectedDate
        }
    }

    // Daily totals calculated purely from valid actual consumed items
    val dailyTotals = remember(mealLogs) {
        NutritionCalculationService.aggregateDailyNutrition(mealLogs)
    }

    // Identify meals where reminder time + 1 hour has exceeded without user action
    val overdueMealsInfo = remember(mealLogs, dailyPlans, selectedDate) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isToday = selectedDate == todayStr
        val calendar = Calendar.getInstance()
        val currentMinutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        fun parseTimeToMinutes(rawTime: String): Int {
            return try {
                val cleaned = rawTime.trim()
                if (cleaned.contains("AM", ignoreCase = true) || cleaned.contains("PM", ignoreCase = true)) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                    val parsed = sdf.parse(cleaned)
                    if (parsed != null) {
                        val cal = Calendar.getInstance().apply { time = parsed }
                        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    } else 8 * 60 + 30
                } else {
                    val parts = cleaned.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                    h * 60 + m
                }
            } catch (e: Exception) {
                8 * 60 + 30
            }
        }

        fun getDefaultMealMinutes(mealType: String): Int {
            return when (mealType.lowercase()) {
                "breakfast" -> 8 * 60 + 30
                "lunch" -> 13 * 60
                "snacks", "snack" -> 16 * 60 + 30
                "dinner" -> 20 * 60
                else -> 9 * 60
            }
        }

        val unresolvedByType = mealLogs
            .filter { it.plannedFoodId > 0L && (it.status == "scheduled" || it.status == "will_eat") }
            .groupBy { it.mealType }

        val overdueMealNames = mutableListOf<String>()

        for ((mealType, _) in unresolvedByType) {
            val plan = dailyPlans.firstOrNull { it.mealType.equals(mealType, ignoreCase = true) }
            val scheduledMinutes = if (plan != null) parseTimeToMinutes(plan.mealTime) else getDefaultMealMinutes(mealType)
            val overdueThresholdMinutes = scheduledMinutes + 60

            if (isToday) {
                if (currentMinutesOfDay >= overdueThresholdMinutes) {
                    overdueMealNames.add(mealType.replaceFirstChar { it.uppercase() })
                }
            } else {
                overdueMealNames.add(mealType.replaceFirstChar { it.uppercase() })
            }
        }

        val order = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
        overdueMealNames.sortedBy { order.indexOf(it).let { idx -> if (idx >= 0) idx else 99 } }
    }

    fun triggerSubstituteNavigation(mode: String) {
        if (onOpenSubstituteForMeal != null) {
            onOpenSubstituteForMeal(selectedMealFilter, mode)
        } else {
            onSubstitute(0L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = NutriStrings.dailyLogTitle(lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.dailyLogSubtitle(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubstituteOptionsSheet = true },
                containerColor = NutriTeal,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("fab_add_extra_food")
                    .padding(bottom = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add substitute or extra food"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Date Navigation Row
            item {
                DateNavigator(
                    currentDate = selectedDate,
                    onDateChanged = onDateChanged
                )
            }

            // Daily Review Banner
            if (overdueMealsInfo.isNotEmpty()) {
                item {
                    val mealsText = when (overdueMealsInfo.size) {
                        1 -> NutriStrings.mealName(overdueMealsInfo[0], lang)
                        2 -> "${NutriStrings.mealName(overdueMealsInfo[0], lang)} ${if (isTelugu) "మరియు" else "and"} ${NutriStrings.mealName(overdueMealsInfo[1], lang)}"
                        else -> "${overdueMealsInfo.dropLast(1).joinToString(", ") { NutriStrings.mealName(it, lang) }}, ${if (isTelugu) "మరియు" else "and"} ${NutriStrings.mealName(overdueMealsInfo.last(), lang)}"
                    }
                    val actionMessage = if (isTelugu) "నేటి లాగ్ పూర్తి చేయడానికి దయచేసి $mealsText అప్‌డేట్ చేయండి." else "Please update $mealsText to complete today's log."

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("end_of_day_review_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriAmberContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, NutriAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔔", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTelugu) "రోజువారీ సమీక్ష" else "Daily Review",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = actionMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Day's Valid Consumed Totals Snapshot Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isTelugu) "నేటి పోషకాల మొత్తం" else "Actual Day Totals",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal
                                )
                                if (allDaySubstitutesCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NutriTeal.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = if (isTelugu) "$allDaySubstitutesCount సబ్‌స్టిట్యూట్ ఉపయోగించారు" else "$allDaySubstitutesCount substitute${if (allDaySubstitutesCount > 1) "s" else ""} used",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NutriTeal,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${dailyTotals.calories.toInt()} kcal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NutriSnapshotPill(NutriStrings.protein(lang), "${dailyTotals.protein}g", NutriProteinRed, Modifier.weight(1f))
                            NutriSnapshotPill(NutriStrings.carbs(lang), "${dailyTotals.carbs}g", NutriCarbGold, Modifier.weight(1f))
                            NutriSnapshotPill(NutriStrings.fats(lang), "${dailyTotals.fat}g", NutriFatOrange, Modifier.weight(1f))
                            NutriSnapshotPill(NutriStrings.fiber(lang), "${dailyTotals.fiber}g", NutriFiberGreen, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Meal Category Filter Chips: Breakfast, Lunch, Snacks, Dinner
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(mealFilters, key = { "filter_$it" }) { filter ->
                        FilterChip(
                            selected = selectedMealFilter == filter,
                            onClick = { selectedMealFilter = filter },
                            label = { Text(NutriStrings.mealName(filter, lang), fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NutriTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // =========================================================================
            // MEAL LOG ITEMS (Shows scheduled, substituted, and consumed replacement items)
            // =========================================================================
            item(key = "scheduled_routine_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${NutriStrings.mealName(selectedMealFilter, lang)} " + if (isTelugu) "లాగ్" else "Log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTelugu) "ప్రణాళిక చేసిన ఆహారాలు మరియు మార్పుల వివరాలు" else "Planned items & logged substitutes with audit trail",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        val consumedCount = categoryMealLogs.count { it.status.equals("consumed", ignoreCase = true) }
                        val activeTotal = categoryMealLogs.count { !it.status.equals("substituted", ignoreCase = true) }
                        Text(
                            text = if (isTelugu) "$activeTotal లో $consumedCount తిన్నారు" else "$consumedCount/$activeTotal eaten",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (categoryMealLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🍽️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTelugu) "${NutriStrings.mealName(selectedMealFilter, lang)} కి ఆహారాలు లేవు" else "No items scheduled for $selectedMealFilter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTelugu) "ఈ పూటకు కొత్త ఆహారం లేదా సబ్‌స్టిట్యూట్ చేర్చడానికి కింద ఉన్న + బటన్ నొక్కండి." else "Tap the + button below to log a meal or substitute for $selectedMealFilter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(categoryMealLogs, key = { "daily_log_${it.id}" }) { log ->
                    DailyLogItemCard(
                        log = log,
                        foodsMap = foodsMap,
                        onMarkConsumed = { onMarkConsumed(log.id) },
                        onMarkWillEat = { onMarkWillEat(log.id) },
                        onSubstitute = { onSubstitute(log.id) },
                        onRestoreOriginalFood = { onRestoreOriginalFood(log.id) }
                    )
                }
            }
        }
    }

    // Modal chooser when clicking on a Substitute:
    // "When clicking on a substitute, there should be an option either to add a new item or to select one directly from the food pool."
    if (showSubstituteOptionsSheet) {
        SubstituteActionChooserDialog(
            mealType = selectedMealFilter,
            formattedDate = formattedShortDate,
            poolCount = allFoods.size,
            onDismiss = { showSubstituteOptionsSheet = false },
            onSelectFromFoodPool = {
                showSubstituteOptionsSheet = false
                triggerSubstituteNavigation("picker")
            },
            onAddNewItemToPool = {
                showSubstituteOptionsSheet = false
                triggerSubstituteNavigation("form")
            }
        )
    }

    if (showAddFoodDialog) {
        LogExtraFoodFromPoolDialog(
            selectedDate = selectedDate,
            allFoods = allFoods,
            foodPool = foodPool,
            initialMealType = selectedMealFilter,
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { mealType, foodId, qty, unit ->
                onLogExtraFood(mealType, foodId, qty, unit)
                showAddFoodDialog = false
            },
            onCreateCustomFood = onCreateCustomFood,
            onOpenFullSubstituteScreen = { mealType, mode ->
                showAddFoodDialog = false
                if (onOpenSubstituteForMeal != null) {
                    onOpenSubstituteForMeal(mealType, mode)
                } else {
                    onSubstitute(0L)
                }
            }
        )
    }
}

@Composable
private fun StandaloneSubstituteSectionCard(
    mealType: String,
    formattedDate: String,
    substituteLogs: List<MealLogEntity>,
    foodsMap: Map<Long, FoodEntity>,
    poolCount: Int,
    onClickSubstituteCard: () -> Unit,
    onSelectFromPool: () -> Unit,
    onAddNewItem: () -> Unit,
    onDeleteSubstituteLog: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("standalone_substitute_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = NutriTealContainer.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.5.dp, NutriTeal.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Clickable Substitute header + day-only badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClickSubstituteCard() }
                    .testTag("substitute_header_click_area"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NutriTeal.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Substitute Food",
                            tint = NutriTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$mealType Substitutes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NutriTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$formattedDate only",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Saved in Food Pool • Independent of your meal routine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Substitute Options",
                    tint = NutriTeal
                )
            }

            // Two direct action buttons: Select from Food Pool OR Add a New Item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectFromPool,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_substitute_select_pool"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NutriTeal,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "From Food Pool ($poolCount)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onAddNewItem,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_substitute_add_new"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, NutriTeal),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NutriTeal
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add New Item",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Logged substitutes for this meal on this date (if any)
            if (substituteLogs.isNotEmpty()) {
                HorizontalDivider(color = NutriTeal.copy(alpha = 0.2f))
                Text(
                    text = "Substitutes Used on $formattedDate (${substituteLogs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NutriTeal
                )
                substituteLogs.forEach { subLog ->
                    val foodId = if (subLog.actualFoodId > 0) subLog.actualFoodId else subLog.plannedFoodId
                    val food = foodsMap[foodId]
                    val scaledCals = if (food != null && food.referenceQuantity > 0) {
                        (food.calories * (subLog.actualQuantity / food.referenceQuantity)).toInt()
                    } else 0

                    Surface(
                        onClick = onClickSubstituteCard,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logged_substitute_item_${subLog.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NutriTeal.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MacroAppIcon(
                                        macroOrCategory = food?.category ?: "food",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = food?.name ?: "Substitute Food",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NutriFiberGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "✓ Used Today",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NutriFiberGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${subLog.actualQuantity.toInt()} ${subLog.quantityUnit} • $scaledCals kcal • In Food Pool",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteSubstituteLog(subLog.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove substitute log",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubstituteActionChooserDialog(
    mealType: String,
    formattedDate: String,
    poolCount: Int,
    onDismiss: () -> Unit,
    onSelectFromFoodPool: () -> Unit,
    onAddNewItemToPool: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NutriTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = NutriTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Use a Substitute",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged for $mealType on $formattedDate only",
                        style = MaterialTheme.typography.labelSmall,
                        color = NutriTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How would you like to choose your substitute? Substitutes are saved in your Food Pool and do not alter your recurring meal routine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Select Directly from Food Pool
                Card(
                    onClick = onSelectFromFoodPool,
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NutriTealContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.5.dp, NutriTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chooser_select_from_pool")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NutriTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select from Food Pool",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pick ANY item from the food pool ($poolCount items)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NutriTeal
                        )
                    }
                }

                // Option 2: Add a New Item (Placed in Food Pool)
                Card(
                    onClick = onAddNewItemToPool,
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chooser_add_new_item")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NutriTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Add a New Item",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create custom food and save directly into your Food Pool",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NutriTeal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DateNavigator(
    currentDate: String,
    onDateChanged: (String) -> Unit
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val todayStr = remember { sdf.format(Date()) }

    val formattedDisplay = remember(currentDate, isTelugu) {
        try {
            val date = sdf.parse(currentDate) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            val teluguDayNames = listOf("సోమవారం", "మంగళవారం", "బుధవారం", "గురువారం", "శుక్రవారం", "శనివారం", "ఆదివారం")
            val monthDayFormat = SimpleDateFormat("d MMM", Locale.getDefault())
            if (isTelugu) {
                "${teluguDayNames[dow]}, ${monthDayFormat.format(date)}"
            } else {
                displayFormat.format(date)
            }
        } catch (_: Exception) {
            currentDate
        }
    }

    val isToday = currentDate == todayStr

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("date_navigator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = sdf.parse(currentDate) ?: Date()
                    } catch (_: Exception) {}
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    onDateChanged(sdf.format(cal.time))
                },
                modifier = Modifier.testTag("prev_date_btn")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = NutriStrings.back(lang))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (!isToday) {
                            onDateChanged(todayStr)
                        }
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = NutriTeal)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isToday) (if (isTelugu) "ఈరోజు (${formattedDisplay})" else "Today (${formattedDisplay})") else formattedDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NutriTeal.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { onDateChanged(todayStr) }
                    ) {
                        Text(
                            text = if (isTelugu) "ఈరోజు" else "Today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = sdf.parse(currentDate) ?: Date()
                    } catch (_: Exception) {}
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    onDateChanged(sdf.format(cal.time))
                },
                modifier = Modifier.testTag("next_date_btn")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = if (isTelugu) "తదుపరి రోజు" else "Next Day")
            }
        }
    }
}

@Composable
fun DailyLogItemCard(
    log: MealLogEntity,
    foodsMap: Map<Long, FoodEntity>,
    onMarkConsumed: () -> Unit,
    onMarkWillEat: () -> Unit,
    onSubstitute: () -> Unit = {},
    onRestoreOriginalFood: () -> Unit = {}
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val isSubstituted = log.status.equals("substituted", ignoreCase = true)
    val isConsumed = log.status.equals("consumed", ignoreCase = true)
    val isWillEat = log.status.equals("will_eat", ignoreCase = true)
    val isScheduled = log.status.equals("scheduled", ignoreCase = true)

    val plannedFood = foodsMap[log.plannedFoodId]
    val actualFood = foodsMap[log.actualFoodId]

    // For a substituted item, displayed food is the original planned food.
    // For a consumed or pending item, displayed food is actualFood ?: plannedFood.
    val displayedFood = if (isSubstituted) {
        plannedFood ?: actualFood
    } else {
        actualFood ?: plannedFood
    }

    val foodName = displayedFood?.name ?: if (isSubstituted) (if (isTelugu) "ప్రణాళిక చేసిన ఆహారం" else "Planned Item") else "Food Item"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_log_card_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSubstituted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                isConsumed -> NutriFiberGreen.copy(alpha = 0.08f)
                isWillEat -> NutriAmberContainer.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isSubstituted -> Color(0xFF90CAF9)
                isConsumed -> NutriFiberGreen.copy(alpha = 0.35f)
                isWillEat -> NutriAmber.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSubstituted -> Color(0xFFE3F2FD)
                                    isConsumed -> NutriFiberGreen.copy(alpha = 0.12f)
                                    isWillEat -> NutriAmber.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        MacroAppIcon(
                            macroOrCategory = displayedFood?.category ?: "food",
                            modifier = Modifier.size(26.dp),
                            overrideColor = when {
                                isSubstituted -> Color(0xFF1976D2)
                                isConsumed -> NutriFiberGreen
                                isWillEat -> NutriAmber
                                else -> NutriTeal
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = foodName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubstituted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface
                        )

                        if (isSubstituted) {
                            val originalFoodName = (plannedFood ?: displayedFood)?.name ?: "Food Item"
                            val qtyStr = if (log.plannedQuantity % 1.0 == 0.0) log.plannedQuantity.toInt().toString() else log.plannedQuantity.toString()
                            Text(
                                text = if (isTelugu) "ప్రణాళిక చేసినది: $originalFoodName ($qtyStr ${log.quantityUnit})" else "Originally planned: $originalFoodName ($qtyStr ${log.quantityUnit})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.testTag("subtitle_originally_planned_${log.id}")
                            )
                        } else {
                            val qtyStr = if (log.actualQuantity % 1.0 == 0.0) log.actualQuantity.toInt().toString() else log.actualQuantity.toString()
                            val calsStr = if (log.calories > 0) "${log.calories.toInt()} kcal" else ""
                            Text(
                                text = (if (isTelugu) "పరిమాణం: " else "Serving: ") + "$qtyStr ${log.quantityUnit}${if (calsStr.isNotEmpty()) " • $calsStr" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSubstituted -> Color(0xFFE3F2FD)
                        isConsumed -> NutriFiberGreen.copy(alpha = 0.15f)
                        isWillEat -> NutriAmber.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isSubstituted -> Color(0xFF90CAF9)
                            isConsumed -> NutriFiberGreen.copy(alpha = 0.4f)
                            isWillEat -> NutriAmber.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier.testTag("badge_status_${log.id}")
                ) {
                    Text(
                        text = when {
                            isSubstituted -> if (isTelugu) "మార్చబడింది" else "Replaced"
                            isConsumed -> if (isTelugu) "✓ తిన్నాను" else "✓ Consumed"
                            isWillEat -> if (isTelugu) "⏳ తింటాను" else "⏳ Will Eat"
                            else -> if (isTelugu) "షెడ్యూల్" else "Scheduled"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSubstituted -> Color(0xFF1565C0)
                            isConsumed -> NutriFiberGreen
                            isWillEat -> NutriAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isScheduled || isWillEat) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMarkConsumed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_consumed_${log.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(NutriStrings.markConsumed(lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    if (isScheduled) {
                        OutlinedButton(
                            onClick = onMarkWillEat,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_will_eat_${log.id}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(NutriStrings.markWillEat(lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = onSubstitute,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_substitute_${log.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NutriTeal),
                        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(NutriStrings.substitute(lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (isSubstituted) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRestoreOriginalFood,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_restore_${log.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2)),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTelugu) "అసలు ఆహారాన్ని పునరుద్ధరించండి" else "Restore original planned food",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun NutriSnapshotPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MacroAppIcon(macroOrCategory = label, modifier = Modifier.size(22.dp), overrideColor = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExtraFoodFromPoolDialog(
    selectedDate: String,
    allFoods: List<FoodEntity>,
    foodPool: List<FoodPoolEntity>,
    initialMealType: String,
    onDismiss: () -> Unit,
    onConfirm: (mealType: String, foodId: Long, quantity: Double, unit: String) -> Unit,
    onCreateCustomFood: (String, String, Double, String, Double, Double, Double, Double, Double, (Long) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onOpenFullSubstituteScreen: ((mealType: String, mode: String) -> Unit)? = null
) {
    var selectedMealType by remember {
        mutableStateOf(if (initialMealType.isBlank()) "Snacks" else initialMealType)
    }
    var searchQuery by remember { mutableStateOf("") }
    val foodFilterCategories = listOf("All", "Protein", "Carbs", "Fat", "Fiber")
    var selectedFoodFilter by remember { mutableStateOf("All") }
    var showCreateCustomDialog by remember { mutableStateOf(false) }

    val poolFoodIds = remember(foodPool) { foodPool.map { it.foodId }.toSet() }

    fun isFoodInCategory(food: FoodEntity, cat: String): Boolean {
        return when (cat) {
            "All" -> true
            "Protein" -> food.category.equals("Protein", ignoreCase = true)
            "Carbs" -> food.category.contains("Carb", ignoreCase = true)
            "Fat" -> food.category.contains("Fat", ignoreCase = true)
            "Fiber" -> food.category.contains("Fiber", ignoreCase = true) ||
                food.category.contains("Veg", ignoreCase = true) ||
                food.category.contains("Fruit", ignoreCase = true)
            else -> true
        }
    }

    val poolCategoryCounts = remember(allFoods) {
        foodFilterCategories.associateWith { cat ->
            if (cat == "All") allFoods.size else allFoods.count { isFoodInCategory(it, cat) }
        }
    }

    val filteredFoods = remember(allFoods, selectedFoodFilter, searchQuery) {
        val baseList = if (selectedFoodFilter == "All") {
            allFoods
        } else {
            allFoods.filter { isFoodInCategory(it, selectedFoodFilter) }
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.iconKey.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var selectedFoodId by remember(filteredFoods) {
        mutableStateOf(filteredFoods.firstOrNull()?.id ?: (allFoods.firstOrNull()?.id ?: -1L))
    }

    val selectedFood = remember(allFoods, selectedFoodId) {
        allFoods.firstOrNull { it.id == selectedFoodId } ?: filteredFoods.firstOrNull()
    }

    var quantityInput by remember(selectedFood) {
        mutableStateOf(selectedFood?.referenceQuantity?.toInt()?.toString() ?: "100")
    }

    val parsedQuantity = quantityInput.toDoubleOrNull() ?: (selectedFood?.referenceQuantity ?: 100.0)

    val scaledNutrition = remember(selectedFood, parsedQuantity) {
        if (selectedFood != null && parsedQuantity > 0) {
            NutritionCalculationService.scaleNutrition(selectedFood, parsedQuantity)
        } else null
    }

    if (showCreateCustomDialog) {
        AddCustomFoodDialog(
            onDismiss = { showCreateCustomDialog = false },
            onSave = { name, cat, refQty, refUnit, cals, p, c, f, fib ->
                onCreateCustomFood(name, cat, refQty, refUnit, cals, p, c, f, fib) { newId ->
                    selectedFoodId = newId
                    quantityInput = refQty.toInt().toString()
                    showCreateCustomDialog = false
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NutriTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = NutriTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Log Substitute / Extra Food",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged only for $selectedDate • Placed in Food Pool",
                        style = MaterialTheme.typography.bodySmall,
                        color = NutriTeal,
                        fontWeight = FontWeight.Medium
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
                // Quick chooser: Select from Pool vs Add New Item
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (onOpenFullSubstituteScreen != null) {
                                onOpenFullSubstituteScreen(selectedMealType, "new")
                            } else {
                                showCreateCustomDialog = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NutriTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = NutriTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add a New Item to Food Pool", color = NutriTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Meal Type selector
                Text(
                    text = "Meal Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Breakfast", "Lunch", "Snacks", "Dinner").forEach { meal ->
                        FilterChip(
                            selected = selectedMealType.equals(meal, ignoreCase = true),
                            onClick = { selectedMealType = meal },
                            label = { Text(meal, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NutriTeal,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExpandableSearchHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    placeholderText = "Item name...",
                    titleContent = {
                        Text(
                            text = "Select from Food Pool ($selectedFoodFilter)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Food Items Category Filters: Protein, Carbs, Fat, Fiber (Food Pool items)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    foodFilterCategories.forEach { filterName ->
                        val isSelected = selectedFoodFilter == filterName
                        val count = poolCategoryCounts[filterName] ?: 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFoodFilter = filterName
                            },
                            leadingIcon = {
                                MacroAppIcon(
                                    macroOrCategory = filterName,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "$filterName ($count)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NutriTealContainer,
                                selectedLabelColor = NutriTeal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Food Pool: $selectedFoodFilter (${filteredFoods.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedFood != null) {
                        Text(
                            text = "Selected: ${selectedFood.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (filteredFoods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No $selectedFoodFilter items found in food pool.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFoods, key = { it.id }) { food ->
                            val isSelected = food.id == selectedFoodId
                            val isInPool = poolFoodIds.contains(food.id)
                            Card(
                                onClick = {
                                    selectedFoodId = food.id
                                    quantityInput = food.referenceQuantity.toInt().toString()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) NutriTealContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, NutriTeal) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("food_item_${food.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MacroAppIcon(
                                        macroOrCategory = food.category,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val scaleFactor = if (food.referenceQuantity > 0.0) 100.0 / food.referenceQuantity else 1.0
                                    val calsPer100g = kotlin.math.round(food.calories * scaleFactor).toInt()
                                    val relatedMacroText = when {
                                        food.category.equals("Protein", ignoreCase = true) -> "Protein: ${String.format(Locale.US, "%.1f", food.protein * scaleFactor)}g"
                                        food.category.contains("Carb", ignoreCase = true) -> "Carbs: ${String.format(Locale.US, "%.1f", food.carbs * scaleFactor)}g"
                                        food.category.contains("Fat", ignoreCase = true) -> "Fat: ${String.format(Locale.US, "%.1f", food.fat * scaleFactor)}g"
                                        food.category.contains("Fib", ignoreCase = true) ||
                                            food.category.contains("Veg", ignoreCase = true) ||
                                            food.category.contains("Fruit", ignoreCase = true) -> "Fiber: ${String.format(Locale.US, "%.1f", food.fiber * scaleFactor)}g"
                                        else -> "Protein: ${String.format(Locale.US, "%.1f", food.protein * scaleFactor)}g"
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$calsPer100g kcal / 100g",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = " • ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = relatedMacroText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NutriTeal,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedFoodId = food.id
                                            quantityInput = food.referenceQuantity.toInt().toString()
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = NutriTeal),
                                        modifier = Modifier.testTag("radio_food_${food.id}")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedFood != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() || ch == '.' }) quantityInput = it
                            },
                            label = { Text("Quantity (${selectedFood.referenceUnit})") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .trackKeyboardFocusBounds()
                                .testTag("input_extra_food_qty"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        FilledTonalButton(
                            onClick = {
                                val current = quantityInput.toDoubleOrNull() ?: selectedFood.referenceQuantity
                                quantityInput = (current + 50).toInt().toString()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+50")
                        }

                        FilledTonalButton(
                            onClick = {
                                val current = quantityInput.toDoubleOrNull() ?: selectedFood.referenceQuantity
                                quantityInput = (current + selectedFood.referenceQuantity).toInt().toString()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+${selectedFood.referenceQuantity.toInt()}")
                        }
                    }

                    if (scaledNutrition != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NutriTealContainer.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${scaledNutrition.calories.toInt()} kcal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = NutriTeal
                            )
                            Text(
                                text = "P: ${scaledNutrition.protein}g | C: ${scaledNutrition.carbs}g | F: ${scaledNutrition.fat}g | Fib: ${scaledNutrition.fiber}g",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Only logged for $selectedDate. Independent of meal routine items.",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val food = selectedFood
                    if (food != null && parsedQuantity > 0) {
                        onConfirm(selectedMealType, food.id, parsedQuantity, food.referenceUnit)
                    }
                },
                enabled = selectedFood != null && parsedQuantity > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_log_extra_food")
            ) {
                Text("Log for $selectedMealType", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
