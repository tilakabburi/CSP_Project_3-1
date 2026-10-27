package com.example.frontend.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.dismissKeyboardOnTap
import com.example.frontend.util.trackKeyboardFocusBounds

object FoodIconHelper {
    fun getEmoji(iconKey: String): String {
        val key = iconKey.lowercase().trim()
        return when {
            key.contains("banana") -> "🍌"
            key.contains("grain/cereal") || key.contains("cereal") || key.contains("oats") || key.contains("oatmeal") || key.contains("cornflakes") || key.contains("bran") -> "🥣"
            key.contains("grain") || key.contains("starch") || key.contains("bread") || key.contains("rice") || key.contains("flour") || key.contains("ravva") || key.contains("ooka") || key.contains("sooji") || key.contains("biscuit") || key.contains("pasta") -> "🍞"
            key.contains("dairy") || key.contains("milk") || key.contains("curd") || key.contains("cheese") || key.contains("buttermilk") -> "🥛"
            key.contains("poultry") || key.contains("chicken") || key.contains("turkey") || key.contains("duck") -> "🍗"
            key.contains("soup") || key.contains("chowder") || key.contains("stew") -> "🍲"
            key.contains("vegetable") || key.contains("veg") || key.contains("salad") || key.contains("spinach") || key.contains("broccoli") || key.contains("carrot") || key.contains("cucumber") || key.contains("brinjal") || key.contains("cabbage") || key.contains("radish") || key.contains("ladies finger") || key.contains("coriander") || key.contains("corn") || key.contains("pea") || key.contains("potato") || key.contains("sprouts") -> "🥦"
            key.contains("fish") || key.contains("seafood") || key.contains("prawn") || key.contains("crab") || key.contains("tuna") || key.contains("korrameenu") -> "🐟"
            key.contains("baked") || key.contains("dessert") || key.contains("pie") || key.contains("custard") || key.contains("cake") || key.contains("waffle") || key.contains("pancake") -> "🧁"
            key.contains("egg") -> "🥚"
            key.contains("meat") || key.contains("mutton") || key.contains("beef") || key.contains("keema") || key.contains("lamb") || key.contains("pork") || key.contains("steak") -> "🥩"
            key.contains("legume") || key.contains("beans") || key.contains("rajma") || key.contains("soybean") || key.contains("dal") || key.contains("lentil") -> "🫘"
            key.contains("dry fruit") || key.contains("nut") || key.contains("almond") || key.contains("cashew") || key.contains("walnut") || key.contains("peanut") || key.contains("seed") || key.contains("nuvvulu") || key.contains("date") || key.contains("kishmish") || key.contains("draksha") || key.contains("coconut") -> "🥜"
            key.contains("beverage") || key.contains("juice") || key.contains("drink") || key.contains("lemonade") -> "🥤"
            key.contains("fruit") || key.contains("apple") || key.contains("melon") || key.contains("grape") || key.contains("papaya") || key.contains("pineapple") || key.contains("orange") || key.contains("berry") || key.contains("berries") || key.contains("watermelon") || key.contains("anjeer") || key.contains("avocado") || key.contains("fig") -> "🍎"
            key.contains("sugar") || key.contains("sweet") || key.contains("honey") || key.contains("syrup") || key.contains("jelly") -> "🍯"
            key.contains("fat/oil") || key.contains("fat") || key.contains("oil") || key.contains("butter") || key.contains("lard") || key.contains("margarine") -> "🫒"
            key.contains("condiment") || key.contains("sauce") || key.contains("pickle") || key.contains("catsup") || key.contains("mayo") -> "🥫"
            else -> "🍽️"
        }
    }
}

@Composable
fun MacroCard(
    label: String,
    consumed: Double,
    target: Double,
    unit: String,
    accentColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (consumed / target).toFloat().coerceIn(0f, 1f) else 0f
    val percent = if (target > 0) ((consumed / target) * 100).toInt() else 0

    Card(
        modifier = modifier
            .testTag("macro_card_${label.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val containerBg = when {
                        label.contains("pro", ignoreCase = true) -> NutriProteinContainerLight
                        label.contains("carb", ignoreCase = true) -> NutriCarbContainerLight
                        label.contains("fat", ignoreCase = true) -> NutriFatContainerLight
                        label.contains("fib", ignoreCase = true) -> NutriFiberContainerLight
                        else -> accentColor.copy(alpha = 0.15f)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        MacroAppIcon(
                            macroOrCategory = label,
                            modifier = Modifier.size(30.dp),
                            overrideColor = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (percent >= 100) NutriFiberGreen else accentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${consumed.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                Text(
                    text = " / ${target.toInt()} $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    targetMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val progress = if (targetMl > 0) (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = if (targetMl > 0) ((currentMl.toDouble() / targetMl.toDouble()) * 100).toInt() else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NutriWaterCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Hydration",
                            tint = NutriWaterCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = NutriStrings.s(lang, "Daily Hydration", "రోజువారీ నీరు"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.s(lang, "Essential for nutrient absorption & energy", "శరీరానికి మరియు జీర్ణక్రియకు అవసరం"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NutriWaterCyan.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = NutriWaterCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", currentMl / 1000.0),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = NutriWaterCyan
                )
                Text(
                    text = " / ${String.format(java.util.Locale.US, "%.1f", targetMl / 1000.0)} " + NutriStrings.s(lang, "Liters", "లీటర్లు"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NutriWaterCyan,
                trackColor = NutriWaterCyan.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddWater(250) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_250"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+250 ml", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAddWater(500) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_500"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+500 ml", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = { onAddWater(300) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_cup"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NutriWaterCyan),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(NutriStrings.s(lang, "+1 Glass", "+1 గ్లాసు"), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun StreakBar(streakDays: Int, modifier: Modifier = Modifier) {
    val lang = LocalAppLanguage.current
    val isTelugu = lang == AppLanguage.TELUGU
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val teluguDayNames = listOf("సోమవారం", "మంగళవారం", "బుధవారం", "గురువారం", "శుక్రవారం", "శనివారం", "ఆదివారం")

    // Determine current day of week (Monday = 0, Tuesday = 1, Wednesday = 2, Thursday = 3, Friday = 4, Saturday = 5, Sunday = 6)
    val todayDow = remember {
        val cal = java.util.Calendar.getInstance()
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            java.util.Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    // A logged in user has at least 1 day streak
    val effectiveStreak = streakDays.coerceAtLeast(1)
    // The active streak days in the current week window ending on today
    val streakThisWeek = effectiveStreak.coerceAtMost(todayDow + 1)
    val activeIndices = ((todayDow - streakThisWeek + 1)..todayDow).toSet()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_bar_card"),
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
                    Text(text = "🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = NutriStrings.streakDays(effectiveStreak, lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTelugu) "నేడు ${teluguDayNames[todayDow]}" else "Today is ${dayNames[todayDow]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NutriTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = if (isTelugu) "\"మంచి అలవాట్లే ఆరోగ్యానికి మూలం\"" else "\"Small choices create habits.\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    val isToday = index == todayDow
                    val isAchieved = index in activeIndices
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium,
                            color = if (isToday) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAchieved) NutriTeal else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isToday) 2.dp else 1.dp,
                                    color = when {
                                        isToday && isAchieved -> NutriAmberLight
                                        isToday -> NutriTeal
                                        isAchieved -> NutriTeal
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAchieved) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Achieved",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
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
fun NutriTag(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            val macroKey = when {
                label.startsWith("P:", ignoreCase = true) || label.contains("protein", ignoreCase = true) -> "protein"
                label.startsWith("C:", ignoreCase = true) || label.contains("carb", ignoreCase = true) -> "carbs"
                label.startsWith("F:", ignoreCase = true) || label.contains("fat", ignoreCase = true) -> "fat"
                label.startsWith("Fib", ignoreCase = true) || label.contains("fiber", ignoreCase = true) -> "fiber"
                else -> null
            }
            if (macroKey != null) {
                MacroAppIcon(
                    macroOrCategory = macroKey,
                    modifier = Modifier.size(13.dp),
                    overrideColor = color
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AddCustomFoodDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, refQty: Double, refUnit: String, cals: Double, p: Double, c: Double, f: Double, fib: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Protein") }
    var refQty by remember { mutableStateOf("100") }
    var refUnit by remember { mutableStateOf("g") }
    var calories by remember { mutableStateOf("150") }
    var protein by remember { mutableStateOf("10") }
    var carbs by remember { mutableStateOf("15") }
    var fat by remember { mutableStateOf("5") }
    var fiber by remember { mutableStateOf("2") }

    val categories = listOf("Protein", "Carbohydrates", "Fiber", "Fats")
    val quickUnits = listOf("g", "ml", "piece", "bowl")
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val scrollState = androidx.compose.foundation.rememberScrollState()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Custom Food",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Saved directly to your Food Pool",
                    style = MaterialTheme.typography.labelSmall,
                    color = NutriTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .dismissKeyboardOnTap(focusManager, keyboardController)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    placeholder = { Text("e.g., Sprouts Bowl") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackKeyboardFocusBounds()
                        .testTag("custom_food_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Clean 2x2 Grid so all 4 categories fit comfortably without clipping
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.chunked(2).forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCats.forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = {
                                        Text(
                                            text = if (cat == "Carbohydrates") "Carbs" else cat,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    },
                                    leadingIcon = {
                                        MacroAppIcon(
                                            macroOrCategory = cat,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NutriTealContainer,
                                        selectedLabelColor = NutriTeal
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = refQty,
                        onValueChange = { refQty = it },
                        label = { Text("Serving Size") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = refUnit,
                        onValueChange = { refUnit = it },
                        label = { Text("Unit (g/ml/pc)") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Quick unit selector pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickUnits.forEach { unitOption ->
                        val selectedUnit = refUnit.equals(unitOption, ignoreCase = true)
                        Surface(
                            onClick = {
                                refUnit = unitOption
                                if (unitOption == "piece" || unitOption == "bowl") {
                                    if (refQty == "100") refQty = "1"
                                } else if (refQty == "1") {
                                    refQty = "100"
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedUnit) NutriTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (selectedUnit) NutriTeal else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = unitOption,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedUnit) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedUnit) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Nutrition (per serving)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber (g)") },
                        modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            category,
                            refQty.toDoubleOrNull() ?: 100.0,
                            refUnit.trim().ifBlank { "g" },
                            calories.toDoubleOrNull() ?: 0.0,
                            protein.toDoubleOrNull() ?: 0.0,
                            carbs.toDoubleOrNull() ?: 0.0,
                            fat.toDoubleOrNull() ?: 0.0,
                            fiber.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save to Food Pool", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
