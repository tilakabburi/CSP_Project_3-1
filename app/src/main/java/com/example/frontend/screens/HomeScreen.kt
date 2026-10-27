package com.example.frontend.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.MealLogEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.service.CalculatedNutrition
import com.example.frontend.components.*
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    consumedNutrition: CalculatedNutrition,
    targets: NutritionTargetEntity?,
    totalWaterMl: Int,
    streakDays: Int,
    mealLogs: List<MealLogEntity>,
    allFoods: List<FoodEntity>,
    onAddWater: (Int) -> Unit,
    onMarkConsumed: (Long) -> Unit,
    onNavigateToSubstitute: (Long) -> Unit,
    onOpenSimulator: () -> Unit,
    onSimulateWaterReminder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val foodsMap = remember(allFoods) { allFoods.associateBy { it.id } }
    val targetCals = targets?.caloriesTarget ?: 2100.0
    val targetProtein = targets?.proteinTarget ?: 120.0
    val targetCarbs = targets?.carbsTarget ?: 220.0
    val targetFat = targets?.fatTarget ?: 50.0
    val targetFiber = targets?.fiberTarget ?: 28.0
    val targetWater = targets?.waterTargetMl ?: 2200

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "NutriPath Logo",
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NutriPath",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NutriTeal
                            )
                            Text(
                                text = if (isTelugu) "ఆహార ప్రణాళిక • రోజువారీ మార్గదర్శనం" else "Plan once • Guided daily",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSimulateWaterReminder,
                        modifier = Modifier.testTag("home_water_simulator_icon_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = NutriWaterCyan) {
                                    Text("💧", fontSize = 8.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Simulate Water Reminder",
                                tint = NutriWaterCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSimulator,
                        modifier = Modifier.testTag("notification_bell_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = NutriTeal) {
                                    Text("12", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Simulate All Daily Notifications & Verification",
                                tint = NutriTeal
                            )
                        }
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Streak & Quote Bar
            item {
                StreakBar(streakDays = streakDays)
            }

            // Hero Calories Summary Banner
            item {
                CaloriesHeroBanner(
                    consumed = consumedNutrition.calories,
                    target = targetCals
                )
            }

            // Macro Cards 2x2 Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroCard(
                            label = NutriStrings.protein(lang),
                            consumed = consumedNutrition.protein,
                            target = targetProtein,
                            unit = "g",
                            accentColor = NutriProteinRed,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            label = NutriStrings.carbs(lang),
                            consumed = consumedNutrition.carbs,
                            target = targetCarbs,
                            unit = "g",
                            accentColor = NutriCarbGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroCard(
                            label = NutriStrings.fats(lang),
                            consumed = consumedNutrition.fat,
                            target = targetFat,
                            unit = "g",
                            accentColor = NutriFatOrange,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            label = NutriStrings.fiber(lang),
                            consumed = consumedNutrition.fiber,
                            target = targetFiber,
                            unit = "g",
                            accentColor = NutriFiberGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Macro Breakdown (Nutrition Consumption Division Pie Chart)
            item {
                NutritionPieDiagramCard(
                    consumed = consumedNutrition,
                    target = targets
                )
            }

            // Water Tracker (Daily Hydration)
            item {
                WaterTrackerCard(
                    currentMl = totalWaterMl,
                    targetMl = targetWater,
                    onAddWater = onAddWater
                )
            }

            // Today's Routine Meals Section
            if (mealLogs.isNotEmpty()) {
                item {
                    TodayRoutineMealsCard(
                        mealLogs = mealLogs,
                        foodsMap = foodsMap,
                        onMarkConsumed = onMarkConsumed,
                        onNavigateToSubstitute = onNavigateToSubstitute
                    )
                }
            }
        }
    }
}

@Composable
fun CaloriesHeroBanner(consumed: Double, target: Double) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val progress = if (target > 0) (consumed / target).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (target - consumed).coerceAtLeast(0.0).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calories_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NutriTeal),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = NutriAmberLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = NutriStrings.calorieEnergy(lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NutriAmberLight,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${consumed.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = " / ${target.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (remaining > 0) {
                        if (isTelugu) "$remaining kcal మిగిలినవి" else "$remaining kcal remaining"
                    } else {
                        NutriStrings.targetAchieved(lang)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(70.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 6.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = NutriAmberLight,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${((consumed / target) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun NutritionPieDiagramCard(
    consumed: CalculatedNutrition,
    target: NutritionTargetEntity?
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU

    val consumedP = consumed.protein.coerceAtLeast(0.0)
    val consumedC = consumed.carbs.coerceAtLeast(0.0)
    val consumedF = consumed.fat.coerceAtLeast(0.0)
    val consumedFib = consumed.fiber.coerceAtLeast(0.0)
    val consumedTotalGrams = consumedP + consumedC + consumedF + consumedFib

    val isLiveConsumption = consumedTotalGrams > 0.0

    val targetP = target?.proteinTarget ?: 120.0
    val targetC = target?.carbsTarget ?: 230.0
    val targetF = target?.fatTarget ?: 55.0
    val targetFib = target?.fiberTarget ?: 28.0

    // If no consumption yet, show target division so the pie chart is always meaningful and instructive
    val displayP = if (isLiveConsumption) consumedP else targetP
    val displayC = if (isLiveConsumption) consumedC else targetC
    val displayF = if (isLiveConsumption) consumedF else targetF
    val displayFib = if (isLiveConsumption) consumedFib else targetFib
    val totalGrams = (displayP + displayC + displayF + displayFib).coerceAtLeast(1.0)

    val pPct = ((displayP / totalGrams) * 100).toInt()
    val cPct = ((displayC / totalGrams) * 100).toInt()
    val fPct = ((displayF / totalGrams) * 100).toInt()
    val fibPct = (100 - pPct - cPct - fPct).coerceAtLeast(0)

    // Compute sweep angles for pie diagram
    val pSweep = (displayP / totalGrams * 360.0).toFloat()
    val cSweep = (displayC / totalGrams * 360.0).toFloat()
    val fSweep = (displayF / totalGrams * 360.0).toFloat()
    val fibSweep = (360f - pSweep - cSweep - fSweep).coerceAtLeast(0f)

    // Calculate total calories for display in the middle of pie chart
    val proteinKcal = (displayP * 4).toInt()
    val carbsKcal = (displayC * 4).toInt()
    val fatKcal = (displayF * 9).toInt()
    val fiberKcal = (displayFib * 2).toInt()
    val totalCalories = if (isLiveConsumption) {
        if (consumed.calories > 0) consumed.calories.toInt() else (proteinKcal + carbsKcal + fatKcal + fiberKcal)
    } else {
        target?.caloriesTarget?.toInt() ?: (proteinKcal + carbsKcal + fatKcal + fiberKcal)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nutrition_pie_diagram_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
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
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = NutriTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isTelugu) "పోషకాల విభజన" else "Macro Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLiveConsumption) {
                                if (isTelugu) "నేడు తిన్న పోషకాలు" else "Today's logged macros"
                            } else {
                                if (isTelugu) "మీ లక్ష్య పోషకాలు" else "Your target macro goals"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pie Diagram Canvas and Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Circular Pie / Donut Diagram
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidthPx = 22.dp.toPx()
                        var startAngle = -90f

                        val gap = 3f

                        // Protein slice
                        if (pSweep > gap) {
                            drawArc(
                                color = NutriProteinRed,
                                startAngle = startAngle + gap / 2f,
                                sweepAngle = pSweep - gap,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                        startAngle += pSweep

                        // Carbs slice
                        if (cSweep > gap) {
                            drawArc(
                                color = NutriCarbGold,
                                startAngle = startAngle + gap / 2f,
                                sweepAngle = cSweep - gap,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                        startAngle += cSweep

                        // Fat slice
                        if (fSweep > gap) {
                            drawArc(
                                color = NutriFatOrange,
                                startAngle = startAngle + gap / 2f,
                                sweepAngle = fSweep - gap,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                        startAngle += fSweep

                        // Fiber slice
                        if (fibSweep > gap) {
                            drawArc(
                                color = NutriFiberGreen,
                                startAngle = startAngle + gap / 2f,
                                sweepAngle = fibSweep - gap,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center text in doughnut: Total Calories
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "$totalCalories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                        Text(
                            text = if (isLiveConsumption) {
                                if (isTelugu) "తిన్నవి" else "Consumed"
                            } else {
                                if (isTelugu) "లక్ష్యం" else "Target"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Percentage breakdown list beside the pie chart
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PieLegendRow(
                        label = NutriStrings.protein(lang),
                        grams = displayP,
                        targetGrams = if (isLiveConsumption) targetP else null,
                        pct = pPct,
                        color = NutriProteinRed
                    )
                    PieLegendRow(
                        label = NutriStrings.carbs(lang),
                        grams = displayC,
                        targetGrams = if (isLiveConsumption) targetC else null,
                        pct = cPct,
                        color = NutriCarbGold
                    )
                    PieLegendRow(
                        label = NutriStrings.fats(lang),
                        grams = displayF,
                        targetGrams = if (isLiveConsumption) targetF else null,
                        pct = fPct,
                        color = NutriFatOrange
                    )
                    PieLegendRow(
                        label = NutriStrings.fiber(lang),
                        grams = displayFib,
                        targetGrams = if (isLiveConsumption) targetFib else null,
                        pct = fibPct,
                        color = NutriFiberGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun PieLegendRow(
    label: String,
    grams: Double,
    targetGrams: Double? = null,
    pct: Int,
    color: Color
) {
    val containerBg = when {
        label.contains("pro", ignoreCase = true) -> NutriProteinContainerLight
        label.contains("carb", ignoreCase = true) -> NutriCarbContainerLight
        label.contains("fat", ignoreCase = true) -> NutriFatContainerLight
        label.contains("fib", ignoreCase = true) -> NutriFiberContainerLight
        else -> color.copy(alpha = 0.12f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                MacroAppIcon(
                    macroOrCategory = label,
                    modifier = Modifier.size(22.dp),
                    overrideColor = color
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = if (targetGrams != null && targetGrams > 0) {
                "${grams.toInt()}/${targetGrams.toInt()}g ($pct%)"
            } else {
                "${grams.toInt()}g ($pct%)"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TodayRoutineMealsCard(
    mealLogs: List<MealLogEntity>,
    foodsMap: Map<Long, FoodEntity>,
    onMarkConsumed: (Long) -> Unit,
    onNavigateToSubstitute: (Long) -> Unit
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val routineLogs = remember(mealLogs) {
        mealLogs.sortedBy { it.id }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_routine_meals_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NutriTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = NutriStrings.todaysMeals(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val consumedCount = routineLogs.count { it.status.equals("consumed", ignoreCase = true) }
                            val activeTotal = routineLogs.count { !it.status.equals("substituted", ignoreCase = true) }
                            Text(
                                text = if (isTelugu) "$activeTotal లో $consumedCount ఆహారాలు పూర్తయ్యాయి" else "$consumedCount of $activeTotal foods eaten",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Group routine items by mealType: Breakfast, Lunch, Snacks, Dinner
                val mealTypes = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
                mealTypes.forEach { type ->
                    val logsForType = routineLogs.filter { it.mealType.equals(type, ignoreCase = true) }
                    if (logsForType.isNotEmpty()) {
                        val (typeIcon, typeColor) = when (type) {
                            "Breakfast" -> Icons.Default.WbSunny to NutriAmber
                            "Lunch" -> Icons.Default.LunchDining to NutriTeal
                            "Snacks" -> Icons.Default.Coffee to NutriCarbGold
                            else -> Icons.Default.Nightlife to NutriProteinRed
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Meal Subheader
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        tint = typeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = NutriStrings.mealName(type, lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = typeColor
                                    )
                                }

                                // Routine food items in this meal
                                logsForType.forEach { log ->
                                    val isConsumed = log.status.equals("consumed", ignoreCase = true)
                                    val isSubstituted = log.status.equals("substituted", ignoreCase = true)
                                    val plannedFood = foodsMap[log.plannedFoodId]
                                    val actualFood = foodsMap[if (log.actualFoodId > 0) log.actualFoodId else log.plannedFoodId]
                                    val food = if (isSubstituted) (plannedFood ?: actualFood) else (actualFood ?: plannedFood)

                                    val containerBg = when {
                                        isSubstituted -> Color(0xFFE3F2FD)
                                        food?.category?.contains("pro", ignoreCase = true) == true -> NutriProteinContainerLight
                                        food?.category?.contains("carb", ignoreCase = true) == true -> NutriCarbContainerLight
                                        food?.category?.contains("fat", ignoreCase = true) == true -> NutriFatContainerLight
                                        food?.category?.contains("fib", ignoreCase = true) == true -> NutriFiberContainerLight
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(containerBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MacroAppIcon(
                                                    macroOrCategory = food?.category ?: "food",
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = food?.name ?: "Food Item",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isConsumed || isSubstituted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSubstituted) {
                                                    val origName = (plannedFood ?: food)?.name ?: "Food"
                                                    Text(
                                                        text = if (isTelugu) "ప్రణాళిక చేసినది: $origName (${log.plannedQuantity.toInt()} ${log.quantityUnit})" else "Originally planned: $origName (${log.plannedQuantity.toInt()} ${log.quantityUnit})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF1976D2)
                                                    )
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val scaledKcal = if (log.calories > 0) log.calories.toInt() else (food?.calories ?: 0.0).toInt()
                                                        Text(
                                                            text = "${log.actualQuantity.toInt()} ${log.quantityUnit} • $scaledKcal kcal",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Mark consumed action / Status
                                        if (isConsumed) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = NutriFiberGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (isTelugu) "✓ తిన్నాను" else "✓ Consumed",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NutriFiberGreen,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else if (isSubstituted) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFE3F2FD)
                                            ) {
                                                Text(
                                                    text = if (isTelugu) "మార్చబడింది" else "Replaced",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1565C0),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { onMarkConsumed(log.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = NutriStrings.markConsumed(lang),
                                                    tint = NutriTeal,
                                                    modifier = Modifier.size(22.dp)
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
    }
}

