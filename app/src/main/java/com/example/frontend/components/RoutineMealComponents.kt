package com.example.frontend.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.frontend.screens.MutableMealSchedule
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

data class MealProgressMeta(
    val mealName: String,
    val targetPct: Double,
    val icon: ImageVector,
    val accentColor: Color
)

data class AssignedFoodDisplay(
    val id: Long,
    val name: String,
    val emoji: String,
    val calories: Int,
    val quantityText: String
)

/**
 * Ultra-compact, space-saving 4 Meal Progress Card for Breakfast, Lunch, Snacks, and Dinner.
 * Fits in minimal vertical height (~64dp) with side-by-side chips while sticky at the top,
 * and expandable for detailed food breakdown.
 */
@Composable
fun RoutineMealProgressBarsCard(
    currentMeals: List<MutableMealSchedule>,
    allFoods: List<FoodEntity>,
    target: NutritionTargetEntity?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val dailyTargetCals = (target?.caloriesTarget ?: 2100.0).coerceAtLeast(1000.0)

    val mealMetas = listOf(
        MealProgressMeta("Breakfast", 0.25, Icons.Default.WbSunny, Color(0xFFF59E0B)),
        MealProgressMeta("Lunch", 0.35, Icons.Default.LunchDining, NutriTeal),
        MealProgressMeta("Snacks", 0.15, Icons.Default.Coffee, Color(0xFFFB923C)),
        MealProgressMeta("Dinner", 0.25, Icons.Default.Nightlife, Color(0xFF8B5CF6))
    )

    val totalPlannedCalories = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.calories ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_meal_progress_bars_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title, Total Badge & Expand/Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(NutriTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NutriTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calorie Goal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            totalPlannedCalories > dailyTargetCals * 1.05 -> NutriAmber.copy(alpha = 0.15f)
                            totalPlannedCalories < dailyTargetCals * 0.90 -> NutriWaterCyan.copy(alpha = 0.15f)
                            else -> NutriTeal.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = "${totalPlannedCalories.toInt()} / ${dailyTargetCals.toInt()} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                totalPlannedCalories > dailyTargetCals * 1.05 -> NutriAmber
                                totalPlannedCalories < dailyTargetCals * 0.90 -> NutriWaterCyan
                                else -> NutriTeal
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compute statistics for all 4 meals
            val mealStats = mealMetas.map { meta ->
                val schedule = currentMeals.firstOrNull { it.mealType.equals(meta.mealName, ignoreCase = true) }
                val assignedFoods = schedule?.foods?.mapNotNull { f ->
                    val food = allFoods.firstOrNull { it.id == f.foodId }
                    if (food != null) {
                        val factor = f.plannedQuantity / food.referenceQuantity.coerceAtLeast(1.0)
                        AssignedFoodDisplay(
                            id = food.id,
                            name = food.name,
                            emoji = FoodIconHelper.getEmoji(food.iconKey),
                            calories = (food.calories * factor).toInt(),
                            quantityText = "${f.plannedQuantity.toInt()} ${f.quantityUnit}"
                        )
                    } else null
                } ?: emptyList()

                val mealCals = assignedFoods.sumOf { it.calories }
                val mealBudget = (dailyTargetCals * meta.targetPct).toInt().coerceAtLeast(1)
                val ratio = (mealCals.toFloat() / mealBudget.toFloat())
                val pct = (ratio * 100).toInt()

                Triple(meta, assignedFoods, Pair(mealCals, pct))
            }

            // Compact Side-by-Side 4 Meal Row (Ultra Space-Saving)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mealStats.forEach { (meta, _, calsPct) ->
                    val (mealCals, pct) = calsPct
                    val ratio = (pct / 100f).coerceIn(0f, 1f)
                    val shortName = when (meta.mealName) {
                        "Breakfast" -> "Bfast"
                        "Snacks" -> "Snack"
                        else -> meta.mealName
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = meta.accentColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, meta.accentColor.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = meta.icon,
                                    contentDescription = null,
                                    tint = meta.accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = shortName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pct > 110) NutriAmber else meta.accentColor,
                                fontSize = 12.sp
                            )

                            // Mini progress bar fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = ratio)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (pct > 110) NutriAmber else meta.accentColor)
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Detailed View (Only shown when expanded)
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealStats.forEach { (meta, assignedFoods, calsPct) ->
                        val (mealCals, pct) = calsPct
                        val mealBudget = (dailyTargetCals * meta.targetPct).toInt().coerceAtLeast(1)
                        val ratio = (mealCals.toFloat() / mealBudget.toFloat()).coerceIn(0f, 1f)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = meta.icon,
                                        contentDescription = null,
                                        tint = meta.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${meta.mealName} (~${(meta.targetPct * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "$mealCals / $mealBudget kcal ($pct%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pct > 110) NutriAmber else meta.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = ratio)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (pct > 110) NutriAmber else meta.accentColor)
                                )
                            }

                            if (assignedFoods.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    contentPadding = PaddingValues(vertical = 2.dp)
                                ) {
                                    items(assignedFoods, key = { it.id }) { item ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = meta.accentColor.copy(alpha = 0.08f),
                                            border = BorderStroke(0.5.dp, meta.accentColor.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = item.emoji, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${item.name} (${item.calories}k)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
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

/**
 * Compact static mini version of the Calorie Goal tracker designed to stay pinned at the top
 * of the page while scrolling food items during routine setup.
 * Displays total planned calories vs daily goal and 4 meal budget percentages with quick expansion.
 */
@Composable
fun MiniCalorieGoalStickyTracker(
    currentMeals: List<MutableMealSchedule>,
    allFoods: List<FoodEntity>,
    target: NutritionTargetEntity?,
    onScrollToTop: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dailyTargetCals = (target?.caloriesTarget ?: 2100.0).coerceAtLeast(1000.0)

    val mealMetas = listOf(
        MealProgressMeta("Breakfast", 0.25, Icons.Default.WbSunny, Color(0xFFF59E0B)),
        MealProgressMeta("Lunch", 0.35, Icons.Default.LunchDining, NutriTeal),
        MealProgressMeta("Snacks", 0.15, Icons.Default.Coffee, Color(0xFFFB923C)),
        MealProgressMeta("Dinner", 0.25, Icons.Default.Nightlife, Color(0xFF8B5CF6))
    )

    val totalPlannedCalories = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.calories ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }

    val totalRatio = (totalPlannedCalories / dailyTargetCals).toFloat().coerceIn(0f, 1.2f)

    val mealStats = mealMetas.map { meta ->
        val schedule = currentMeals.firstOrNull { it.mealType.equals(meta.mealName, ignoreCase = true) }
        val assignedFoods = schedule?.foods?.mapNotNull { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            if (food != null) {
                val factor = f.plannedQuantity / food.referenceQuantity.coerceAtLeast(1.0)
                AssignedFoodDisplay(
                    id = food.id,
                    name = food.name,
                    emoji = FoodIconHelper.getEmoji(food.iconKey),
                    calories = (food.calories * factor).toInt(),
                    quantityText = "${f.plannedQuantity.toInt()} ${f.quantityUnit}"
                )
            } else null
        } ?: emptyList()

        val mealCals = assignedFoods.sumOf { it.calories }
        val mealBudget = (dailyTargetCals * meta.targetPct).toInt().coerceAtLeast(1)
        val ratio = (mealCals.toFloat() / mealBudget.toFloat())
        val pct = (ratio * 100).toInt()

        Triple(meta, assignedFoods, Pair(mealCals, pct))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mini_calorie_goal_sticky_tracker"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Icon + "Calorie Goal" on left, "0 / 2389 kcal" + Expand chevron on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = onScrollToTop != null) { onScrollToTop?.invoke() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NutriTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Calorie Goal",
                            tint = NutriTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calorie Goal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (onScrollToTop != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Scroll to top",
                            tint = NutriTeal,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${totalPlannedCalories.toInt()} / ${dailyTargetCals.toInt()} kcal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Row: 4 Meal Chips (Bfast, Lunch, Snack, Dinner) matching screenshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                mealStats.forEach { (meta, _, calsPct) ->
                    val (_, pct) = calsPct
                    val shortName = when (meta.mealName) {
                        "Breakfast" -> "Bfast"
                        "Lunch" -> "Lunch"
                        "Snacks" -> "Snack"
                        else -> "Dinner"
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = meta.accentColor.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, meta.accentColor.copy(alpha = 0.30f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 6.dp, horizontal = 2.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = meta.icon,
                                    contentDescription = null,
                                    tint = meta.accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = shortName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pct}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (pct > 110) NutriAmber else meta.accentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Slim Total Progress Bar along bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = totalRatio.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            when {
                                totalPlannedCalories > dailyTargetCals * 1.05 -> NutriAmber
                                totalPlannedCalories < dailyTargetCals * 0.90 -> NutriWaterCyan
                                else -> NutriTeal
                            }
                        )
                )
            }

            // Expanded view when tapped
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    mealStats.forEach { (meta, assignedFoods, calsPct) ->
                        val (mealCals, pct) = calsPct
                        val mealBudget = (dailyTargetCals * meta.targetPct).toInt().coerceAtLeast(1)
                        val ratio = (mealCals.toFloat() / mealBudget.toFloat()).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = meta.icon,
                                    contentDescription = null,
                                    tint = meta.accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = meta.mealName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "$mealCals / $mealBudget kcal ($pct%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pct > 110) NutriAmber else meta.accentColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = ratio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (pct > 110) NutriAmber else meta.accentColor)
                            )
                        }

                        if (assignedFoods.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(vertical = 1.dp)
                            ) {
                                items(assignedFoods, key = { it.id }) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = meta.accentColor.copy(alpha = 0.08f),
                                        border = BorderStroke(0.5.dp, meta.accentColor.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = "${item.emoji} ${item.name} (${item.calories}k)",
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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

data class MacroSuggestionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * Centered Routine Save Confirmation Dialog:
 * Shows amount of individual macros, actionable suggestions for protein, carbs (COP), fiber, and fats,
 * with clear Edit and Continue buttons.
 */
@Composable
fun RoutineSaveReviewDialog(
    totalCalories: Double,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
    totalFiber: Double,
    target: NutritionTargetEntity?,
    onEdit: () -> Unit,
    onContinue: () -> Unit,
    lang: AppLanguage = LocalAppLanguage.current
) {
    val targetCals = target?.caloriesTarget ?: 2100.0
    val targetP = target?.proteinTarget ?: 120.0
    val targetC = target?.carbsTarget ?: 220.0
    val targetF = target?.fatTarget ?: 50.0
    val targetFib = (target?.fiberTarget ?: 28.0).coerceAtLeast(20.0)

    val isTe = lang == AppLanguage.TELUGU
    val suggestions = mutableListOf<MacroSuggestionItem>()

    // 1. Protein Check
    if (totalProtein < targetP * 0.85) {
        val shortfall = (targetP - totalProtein).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "ప్రోటీన్ తక్కువగా ఉంది" else "Protein is Low",
                description = if (isTe) "లక్ష్యాన్ని చేరడానికి గుడ్లు, పనీర్, పప్పులు లేదా చికెన్ వంటి ఆహారాలను చేర్చండి." else "Add foods like eggs, paneer, lentils, or chicken to reach your protein goal.",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                color = NutriAmber
            )
        )
    } else if (totalProtein > targetP * 1.30) {
        val surplus = (totalProtein - targetP).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "ప్రోటీన్ ఎక్కువగా ఉంది" else "Protein is High",
                description = if (isTe) "ప్రోటీన్ కాస్త ఎక్కువ (+${surplus}g). తగినంత నీరు తాగడం గుర్తుంచుకోండి." else "Protein is a bit high (+${surplus}g). Remember to drink plenty of water.",
                icon = Icons.Default.Info,
                color = NutriWaterCyan
            )
        )
    }

    // 2. Carbs (COP) Check
    if (totalCarbs > targetC * 1.15) {
        val surplus = (totalCarbs - targetC).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "కార్బోహైడ్రేట్లు ఎక్కువగా ఉన్నాయి" else "Carbs are High",
                description = if (isTe) "కార్బోహైడ్రేట్లు కాస్త ఎక్కువ (+${surplus}g). అన్నం లేదా రొట్టె పరిమాణాన్ని కొద్దిగా తగ్గించవచ్చు." else "Carbs are a bit high (+${surplus}g). You can reduce rice or bread portions slightly.",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = NutriAmber
            )
        )
    } else if (totalCarbs < targetC * 0.70) {
        val shortfall = (targetC - totalCarbs).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "కార్బోహైడ్రేట్లు తక్కువగా ఉన్నాయి" else "Carbs are Low",
                description = if (isTe) "కార్బోహైడ్రేట్లు తక్కువగా ఉన్నాయి (${shortfall}g తక్కువ). స్థిరమైన శక్తి కోసం ఓట్స్, పండ్లు లేదా అన్నం చేర్చండి." else "Carbs are low (${shortfall}g below goal). Add oats, fruit, or rice for steady energy.",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                color = NutriCarbGold
            )
        )
    }

    // 3. Fiber Check
    if (totalFiber > (targetFib * 1.5).coerceAtLeast(42.0)) {
        val surplus = (totalFiber - targetFib).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "ఫైబర్ ఎక్కువగా ఉంది" else "Fiber is High",
                description = if (isTe) "ఫైబర్ కాస్త ఎక్కువ (+${surplus}g). కడుపు నిండినట్లు అనిపిస్తే పచ్చి సలాడ్లను కొద్దిగా తగ్గించవచ్చు." else "Fiber is a bit high (+${surplus}g). You can reduce raw salads if you feel too full.",
                icon = Icons.Default.WarningAmber,
                color = NutriAmber
            )
        )
    } else if (totalFiber < targetFib * 0.70) {
        val shortfall = (targetFib - totalFiber).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "ఫైబర్ తక్కువగా ఉంది" else "Fiber is Low",
                description = if (isTe) "ఫైబర్ తక్కువగా ఉంది (${shortfall}g తక్కువ). తాజా పండ్లు, కూరగాయలు లేదా ఓట్స్ చేర్చండి." else "Fiber is low (${shortfall}g below goal). Add fresh fruits, veggies, or oats.",
                icon = Icons.Default.Info,
                color = NutriWaterCyan
            )
        )
    }

    // 4. Fats Check
    if (totalFat > targetF * 1.25) {
        val surplus = (totalFat - targetF).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "కొవ్వులు ఎక్కువగా ఉన్నాయి" else "Fats are High",
                description = if (isTe) "కొవ్వులు కాస్త ఎక్కువ (+${surplus}g). వంట నూనె లేదా వెన్న వాడకాన్ని కొద్దిగా తగ్గించండి." else "Fats are a bit high (+${surplus}g). Try using less cooking oil or butter.",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = NutriAmber
            )
        )
    } else if (totalFat < targetF * 0.65) {
        val shortfall = (targetF - totalFat).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "కొవ్వులు తక్కువగా ఉన్నాయి" else "Fats are Low",
                description = if (isTe) "కొవ్వులు తక్కువగా ఉన్నాయి (${shortfall}g తక్కువ). కొన్ని నట్స్ లేదా విత్తనాలు చేర్చండి." else "Fats are low (${shortfall}g below goal). Add a few nuts or seeds.",
                icon = Icons.Default.Info,
                color = NutriFatOrange
            )
        )
    }

    // 5. Overall Calorie Alignment
    if (totalCalories > targetCals * 1.08) {
        val surplus = (totalCalories - targetCals).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "క్యాలరీలు కాస్త ఎక్కువ (+${surplus} kcal)" else "Calories are a bit High (+${surplus} kcal)",
                description = if (isTe) "మొత్తం ప్రణాళిక మీ లక్ష్యం కంటే ${surplus} kcal ఎక్కువ. ఆహార భాగాన్ని కొద్దిగా తగ్గించవచ్చు." else "Total planned is ${surplus} kcal above your goal. You can trim a food portion slightly.",
                icon = Icons.Default.LocalFireDepartment,
                color = NutriAmber
            )
        )
    } else if (totalCalories < targetCals * 0.88) {
        val shortfall = (targetCals - totalCalories).toInt()
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "క్యాలరీలు తక్కువగా ఉన్నాయి (-${shortfall} kcal)" else "Calories are Low (-${shortfall} kcal)",
                description = if (isTe) "మొత్తం ప్రణాళిక మీ లక్ష్యం కంటే ${shortfall} kcal తక్కువ. లక్ష్యాన్ని చేరడానికి ఆహారాన్ని చేర్చవచ్చు." else "Total planned is ${shortfall} kcal below your goal. You can add more food to hit your target.",
                icon = Icons.Default.LocalFireDepartment,
                color = NutriWaterCyan
            )
        )
    }

    if (suggestions.isEmpty()) {
        suggestions.add(
            MacroSuggestionItem(
                title = if (isTe) "చాలా బాగుంది!" else "Looks Great!",
                description = if (isTe) "మీ భోజనాలు రోజువారీ పోషకాహార లక్ష్యాలకు చక్కగా సరిపోలుతున్నాయి." else "Your meals match your daily calorie and nutrition targets nicely.",
                icon = Icons.Default.CheckCircle,
                color = NutriTeal
            )
        )
    }

    AlertDialog(
        onDismissRequest = onEdit,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NutriTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = NutriTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NutriStrings.reviewPlanTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NutriStrings.reviewPlanSubtitle(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Amount of Individual Macros
                Text(
                    text = if (isTe) "రోజువారీ మొత్తం vs లక్ష్యాలు" else "Daily Totals vs Targets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MacroRowComparison(
                            label = NutriStrings.caloriesShortLabel(lang),
                            amountText = "${totalCalories.toInt()} kcal",
                            targetText = "${targetCals.toInt()} kcal",
                            color = NutriAmber
                        )
                        MacroRowComparison(
                            label = NutriStrings.proteinShortLabel(lang),
                            amountText = "${totalProtein.toInt()}g",
                            targetText = "${targetP.toInt()}g",
                            color = NutriProteinRed
                        )
                        MacroRowComparison(
                            label = NutriStrings.carbsShortLabel(lang),
                            amountText = "${totalCarbs.toInt()}g",
                            targetText = "${targetC.toInt()}g",
                            color = NutriCarbGold
                        )
                        MacroRowComparison(
                            label = NutriStrings.fatsShortLabel(lang),
                            amountText = "${totalFat.toInt()}g",
                            targetText = "${targetF.toInt()}g",
                            color = NutriFatOrange
                        )
                        MacroRowComparison(
                            label = NutriStrings.fiberShortLabel(lang),
                            amountText = "${totalFiber.toInt()}g",
                            targetText = "${targetFib.toInt()}g",
                            color = NutriTeal
                        )
                    }
                }

                // Section 2: Smart Suggestions
                Text(
                    text = if (isTe) "ఉపయోగకరమైన చిట్కాలు" else "Helpful Tips",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                suggestions.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = item.color.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, item.color.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val isMacro = item.title.contains("Protein", ignoreCase = true) ||
                                        item.title.contains("Carb", ignoreCase = true) ||
                                        item.title.contains("Fat", ignoreCase = true) ||
                                        item.title.contains("Fiber", ignoreCase = true)
                                if (isMacro) {
                                    MacroAppIcon(
                                        macroOrCategory = item.title,
                                        modifier = Modifier.size(20.dp),
                                        overrideColor = item.color
                                    )
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = item.color
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                modifier = Modifier.testTag("dialog_continue_save_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isTe) "కొనసాగించు" else "Continue",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_edit_routine_btn")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isTe) "మార్పు చేయి" else "Edit", maxLines = 1)
            }
        }
    )
}

@Composable
private fun MacroRowComparison(
    label: String,
    amountText: String,
    targetText: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            MacroAppIcon(
                macroOrCategory = label,
                modifier = Modifier.size(18.dp),
                overrideColor = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            Text(
                text = " / $targetText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Orders a list of foods in the alternating macro cycle requested by user:
 * 1st Protein, 2nd Carbs, 3rd Fiber, 4th Fats, repeating continuously:
 * Protein -> Carbs -> Fiber -> Fats -> Protein -> Carbs -> Fiber -> Fats...
 */
fun orderFoodsByMacroCycle(foods: List<FoodEntity>): List<FoodEntity> {
    val proteins = foods.filter { it.category.equals("Protein", ignoreCase = true) }
        .sortedBy { it.name }.toMutableList()
    val carbs = foods.filter { it.category.startsWith("Carb", ignoreCase = true) }
        .sortedBy { it.name }.toMutableList()
    val fiber = foods.filter { it.category.startsWith("Fib", ignoreCase = true) }
        .sortedBy { it.name }.toMutableList()
    val fats = foods.filter { it.category.startsWith("Fat", ignoreCase = true) }
        .sortedBy { it.name }.toMutableList()
    val others = foods.filter {
        !it.category.equals("Protein", ignoreCase = true) &&
        !it.category.startsWith("Carb", ignoreCase = true) &&
        !it.category.startsWith("Fib", ignoreCase = true) &&
        !it.category.startsWith("Fat", ignoreCase = true)
    }.sortedBy { it.name }.toMutableList()

    val result = ArrayList<FoodEntity>(foods.size)
    while (proteins.isNotEmpty() || carbs.isNotEmpty() || fiber.isNotEmpty() || fats.isNotEmpty()) {
        if (proteins.isNotEmpty()) result.add(proteins.removeAt(0))
        if (carbs.isNotEmpty()) result.add(carbs.removeAt(0))
        if (fiber.isNotEmpty()) result.add(fiber.removeAt(0))
        if (fats.isNotEmpty()) result.add(fats.removeAt(0))
    }
    result.addAll(others)
    return result
}

/**
 * Organizes meal routine pool foods so all selected (assigned to any meal) items
 * are moved up to a single place at the top, without creating separate sections.
 * Both the assigned items at the top and unassigned items below them follow
 * the strict macro cycle: Protein -> Carbs -> Fiber -> Fats...
 */
fun organizeMealRoutineFoods(
    foods: List<FoodEntity>,
    assignedFoodIds: Set<Long>
): List<FoodEntity> {
    val assigned = foods.filter { it.id in assignedFoodIds }
    val unassigned = foods.filter { it.id !in assignedFoodIds }
    val orderedAssigned = orderFoodsByMacroCycle(assigned)
    val orderedUnassigned = orderFoodsByMacroCycle(unassigned)
    return orderedAssigned + orderedUnassigned
}
