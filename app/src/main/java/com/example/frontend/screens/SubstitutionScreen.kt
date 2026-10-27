package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.FoodPoolEntity
import com.example.backend.data.local.entity.MealLogEntity
import com.example.backend.data.repository.NutriPathRepository
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
fun SubstitutionScreen(
    logId: Long,
    mealLogs: List<MealLogEntity>,
    allFoods: List<FoodEntity>,
    foodPool: List<FoodPoolEntity>,
    onConfirmSubstitution: (replacementFoodId: Long, quantity: Double, unit: String) -> Unit,
    onCreateCustomFood: (String, String, Double, String, Double, Double, Double, Double, Double, (Long) -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    selectedDate: String = NutriPathRepository.getTodayDateString(),
    initialMealType: String? = null,
    initialMode: String = "form",
    onLogSubstituteForMeal: ((mealType: String, replacementFoodId: Long, quantity: Double, unit: String) -> Unit)? = null,
    onAddFoodToPool: ((Long) -> Unit)? = null
) {
    val lang = LocalAppLanguage.current
    // Current active screen in this 2-screen workflow: "form" (First Screen) vs "picker" (Select from Food Pool Screen)
    var currentScreenState by remember { mutableStateOf(if (initialMode == "picker" || initialMode == "pool") "picker" else "form") }

    // Meal selection: Breakfast, Lunch, Evening Snack, Dinner
    val mealTypes = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
    val resolvedMeal = remember(initialMealType, logId, mealLogs) {
        if (!initialMealType.isNullOrBlank()) {
            initialMealType
        } else {
            val log = mealLogs.firstOrNull { it.id == logId }
            log?.mealType ?: "Breakfast"
        }
    }
    var selectedMealType by remember(resolvedMeal) { mutableStateOf(resolvedMeal) }

    // Form inputs on First Screen
    var foodName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Protein") }
    var quantityInput by remember { mutableStateOf("100") }
    var unitInput by remember { mutableStateOf("g") }
    var caloriesInput by remember { mutableStateOf("150") }
    var proteinInput by remember { mutableStateOf("12") }
    var carbsInput by remember { mutableStateOf("15") }
    var fatInput by remember { mutableStateOf("5") }
    var fiberInput by remember { mutableStateOf("3") }

    // Selected item tracking from Food Pool
    var selectedFoodFromPoolId by remember { mutableStateOf<Long?>(null) }
    var baseReferenceQuantity by remember { mutableDoubleStateOf(100.0) }
    var baseCalories by remember { mutableDoubleStateOf(150.0) }
    var baseProtein by remember { mutableDoubleStateOf(12.0) }
    var baseCarbs by remember { mutableDoubleStateOf(15.0) }
    var baseFat by remember { mutableDoubleStateOf(5.0) }
    var baseFiber by remember { mutableDoubleStateOf(3.0) }
    var formErrorMessage by remember { mutableStateOf<String?>(null) }

    // Track locally added pool IDs
    val poolFoodIds = remember(foodPool) { foodPool.map { it.foodId }.toSet() }

    // Food Pool Picker Screen category & search
    val poolCategories = listOf("Protein", "Carbs", "Fiber", "Fats")
    var selectedCategory by remember { mutableStateOf("Protein") }
    var pickerSearchQuery by remember { mutableStateOf("") }

    // Helper to categorize every food item strictly into one of the four categories
    fun getNormalizedMacroCategory(food: FoodEntity): String {
        val cat = food.category.trim()
        return when {
            cat.equals("Protein", ignoreCase = true) -> "Protein"
            cat.contains("Carb", ignoreCase = true) -> "Carbs"
            cat.contains("Fat", ignoreCase = true) -> "Fats"
            cat.contains("Fib", ignoreCase = true) ||
                cat.contains("Veg", ignoreCase = true) ||
                cat.contains("Fruit", ignoreCase = true) -> "Fiber"
            else -> {
                val p = food.protein
                val c = food.carbs
                val f = food.fat
                val fib = food.fiber
                when {
                    p >= c && p >= f && p >= fib -> "Protein"
                    c >= p && c >= f && c >= fib -> "Carbs"
                    f >= p && f >= c && f >= fib -> "Fats"
                    else -> "Fiber"
                }
            }
        }
    }

    // Complete Food Pool List organized under the four categories only, filtered by search query
    val filteredPoolFoods = remember(allFoods, pickerSearchQuery, selectedCategory) {
        val trimmed = pickerSearchQuery.trim()
        allFoods.filter { food ->
            val cat = getNormalizedMacroCategory(food)
            val matchesCategory = cat.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = trimmed.isBlank() ||
                food.name.contains(trimmed, ignoreCase = true) ||
                food.iconKey.contains(trimmed, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedBy { it.name }
    }

    // Temp selection in Food Pool picker
    var tempSelectedPoolFood by remember(filteredPoolFoods) {
        mutableStateOf(filteredPoolFoods.firstOrNull())
    }

    // Redirect to quantity adjustment screen before logging
    fun selectAndRedirectToAdjustQuantity(food: FoodEntity) {
        selectedFoodFromPoolId = food.id
        baseReferenceQuantity = if (food.referenceQuantity > 0.0) food.referenceQuantity else 100.0
        baseCalories = food.calories
        baseProtein = food.protein
        baseCarbs = food.carbs
        baseFat = food.fat
        baseFiber = food.fiber

        foodName = food.name
        category = getNormalizedMacroCategory(food)
        unitInput = food.referenceUnit.ifBlank { "g" }
        quantityInput = if (food.referenceQuantity % 1.0 == 0.0)
            food.referenceQuantity.toInt().toString()
        else food.referenceQuantity.toString()
        caloriesInput = if (food.calories % 1.0 == 0.0)
            food.calories.toInt().toString()
        else String.format(Locale.US, "%.1f", food.calories)
        proteinInput = String.format(Locale.US, "%.1f", food.protein)
        carbsInput = String.format(Locale.US, "%.1f", food.carbs)
        fatInput = String.format(Locale.US, "%.1f", food.fat)
        fiberInput = String.format(Locale.US, "%.1f", food.fiber)
        formErrorMessage = null
        currentScreenState = "form"
    }

    // Scale calories and macros dynamically when quantity is adjusted
    fun updateQuantityAndScaleMacros(newQtyStr: String) {
        if (newQtyStr.all { ch -> ch.isDigit() || ch == '.' }) {
            quantityInput = newQtyStr
            val newQty = newQtyStr.toDoubleOrNull()
            if (newQty != null && newQty > 0.0 && baseReferenceQuantity > 0.0 && selectedFoodFromPoolId != null) {
                val ratio = newQty / baseReferenceQuantity
                val c = baseCalories * ratio
                val p = baseProtein * ratio
                val cb = baseCarbs * ratio
                val f = baseFat * ratio
                val fib = baseFiber * ratio
                caloriesInput = if (c % 1.0 == 0.0) c.toInt().toString() else String.format(Locale.US, "%.1f", c)
                proteinInput = String.format(Locale.US, "%.1f", p)
                carbsInput = String.format(Locale.US, "%.1f", cb)
                fatInput = String.format(Locale.US, "%.1f", f)
                fiberInput = String.format(Locale.US, "%.1f", fib)
            }
        }
    }

    // Date display string
    val formattedDate = remember(selectedDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val parsed = sdf.parse(selectedDate)
            if (parsed != null) out.format(parsed) else selectedDate
        } catch (_: Exception) {
            selectedDate
        }
    }

    BackHandler {
        if (currentScreenState == "picker") {
            currentScreenState = "form"
        } else {
            onNavigateBack()
        }
    }

    // Final Commit function: Adds/saves to Food Pool and Logs item to Daily Meal Log
    fun commitAndSaveFood() {
        if (foodName.isBlank()) {
            formErrorMessage = "Please enter a food name or pick from Food Pool"
            return
        }

        val qty = quantityInput.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 100.0
        val unit = unitInput.trim().ifBlank { "g" }
        val cals = caloriesInput.toDoubleOrNull() ?: 150.0
        val p = proteinInput.toDoubleOrNull() ?: 0.0
        val c = carbsInput.toDoubleOrNull() ?: 0.0
        val f = fatInput.toDoubleOrNull() ?: 0.0
        val fib = fiberInput.toDoubleOrNull() ?: 0.0

        formErrorMessage = null

        // If an existing food from pool was selected and unchanged
        val existingFood = selectedFoodFromPoolId?.let { id -> allFoods.firstOrNull { it.id == id } }
        if (existingFood != null && existingFood.name.equals(foodName.trim(), ignoreCase = true)) {
            onAddFoodToPool?.invoke(existingFood.id)
            if (logId > 0L) {
                onConfirmSubstitution(existingFood.id, qty, unit)
            } else if (onLogSubstituteForMeal != null) {
                onLogSubstituteForMeal(selectedMealType, existingFood.id, qty, unit)
            } else {
                onConfirmSubstitution(existingFood.id, qty, unit)
            }
            onNavigateBack()
        } else {
            // Create / update food in Food Pool and then log it
            onCreateCustomFood(
                foodName.trim(),
                category,
                qty,
                unit,
                cals,
                p,
                c,
                f,
                fib
            ) { newFoodId ->
                onAddFoodToPool?.invoke(newFoodId)
                if (logId > 0L) {
                    onConfirmSubstitution(newFoodId, qty, unit)
                } else if (onLogSubstituteForMeal != null) {
                    onLogSubstituteForMeal(selectedMealType, newFoodId, qty, unit)
                } else {
                    onConfirmSubstitution(newFoodId, qty, unit)
                }
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                currentScreenState == "picker" -> NutriStrings.s(lang, "Select from Food Pool", "ఆహారాన్ని ఎంచుకోండి")
                                selectedFoodFromPoolId != null -> NutriStrings.s(lang, "Adjust Quantity", "పరిమాణం సర్దుబాటు")
                                else -> NutriStrings.s(lang, "Add / Substitute Food", "ఆహారాన్ని మార్చుకోండి")
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                currentScreenState == "picker" -> NutriStrings.s(lang, "Choose item to adjust portion & log", "పరిమాణం సర్దుబాటు చేయడానికి ఆహారాన్ని ఎంచుకోండి")
                                selectedFoodFromPoolId != null -> NutriStrings.s(lang, "Adjust portion for $foodName before logging", "లాగ్ చేయడానికి ముందు $foodName పరిమాణాన్ని మార్చండి")
                                else -> NutriStrings.s(lang, "Logged for $formattedDate • Placed in Food Pool", "$formattedDate కోసం లాగ్ చేయబడింది • ఫుడ్ పూల్‌లో ఉంది")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = NutriTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentScreenState == "picker") {
                            currentScreenState = "form"
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = NutriStrings.back(lang))
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
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (currentScreenState == "picker") {
                        val selected = tempSelectedPoolFood
                        Button(
                            onClick = {
                                if (selected != null) {
                                    selectAndRedirectToAdjustQuantity(selected)
                                }
                            },
                            enabled = selected != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_confirm_food_pool_selection"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = NutriStrings.s(lang, "Adjust Quantity", "పరిమాణం సర్దుబాటు"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        val localizedMealName = NutriStrings.mealName(selectedMealType, lang)
                        Button(
                            onClick = { commitAndSaveFood() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_save_log_food"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NutriTeal)
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = NutriStrings.s(lang, "Save & Log to $selectedMealType", "$localizedMealName కి సేవ్ చేసి లాగ్ చేయండి"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreenState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            if (screen == "picker") {
                // SECOND SCREEN: SELECT FROM FOOD POOL (COMPLETE CATALOG)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action: Add New Custom Item Shortcut (placed right at top)
                    Card(
                        onClick = {
                            foodName = pickerSearchQuery.trim()
                            selectedFoodFromPoolId = null
                            currentScreenState = "form"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriTealContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.2.dp, NutriTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_picker_add_new_custom")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (pickerSearchQuery.isNotBlank())
                                        NutriStrings.s(lang, "Add \"$pickerSearchQuery\" as New Item", "\"$pickerSearchQuery\" ని కొత్త ఆహారంగా చేర్చండి")
                                    else
                                        NutriStrings.s(lang, "Can't find your item? Add New Food", "మీ ఆహారం లేదా? కొత్తది చేర్చండి"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal
                                )
                                Text(
                                    text = NutriStrings.s(lang, "Creates custom food, adds to Food Pool & logs it", "కొత్త ఆహారాన్ని సృష్టించి, పూల్‌కి చేర్చి లాగ్ చేస్తుంది"),
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

                    // 4 Categories Filter Chips (Protein, Carbs, Fiber, Fats)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        poolCategories.forEach { cat ->
                            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                            val count = allFoods.count { getNormalizedMacroCategory(it) == cat }
                            val catLabel = when (cat.lowercase()) {
                                "protein" -> NutriStrings.protein(lang)
                                "carbs" -> NutriStrings.carbs(lang)
                                "fiber" -> NutriStrings.fiber(lang)
                                "fats" -> NutriStrings.fats(lang)
                                else -> cat
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategory = cat
                                    tempSelectedPoolFood = filteredPoolFoods.firstOrNull()
                                },
                                leadingIcon = {
                                    MacroAppIcon(
                                        macroOrCategory = cat,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "$catLabel ($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NutriTeal,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Search Header: Icon-based expandable search matching other screens
                    val currentCatLocalized = when (selectedCategory.lowercase()) {
                        "protein" -> NutriStrings.protein(lang)
                        "carbs" -> NutriStrings.carbs(lang)
                        "fiber" -> NutriStrings.fiber(lang)
                        "fats" -> NutriStrings.fats(lang)
                        else -> selectedCategory
                    }
                    ExpandableSearchHeader(
                        searchQuery = pickerSearchQuery,
                        onSearchQueryChange = { pickerSearchQuery = it },
                        placeholderText = NutriStrings.s(lang, "Search $selectedCategory foods...", "$currentCatLocalized ఆహారాలను వెతకండి..."),
                        titleContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$currentCatLocalized (${filteredPoolFoods.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )

                    if (filteredPoolFoods.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = NutriStrings.s(lang, "No items match \"$pickerSearchQuery\".", "\"$pickerSearchQuery\" తో ఎలాంటి ఆహారాలు లేవు."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        foodName = pickerSearchQuery.trim()
                                        selectedFoodFromPoolId = null
                                        currentScreenState = "form"
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(NutriStrings.s(lang, "Add \"$pickerSearchQuery\" as Custom Food", "\"$pickerSearchQuery\" ని కస్టమ్ ఆహారంగా చేర్చండి"))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPoolFoods, key = { it.id }) { food ->
                                val isSelected = tempSelectedPoolFood?.id == food.id
                                val scaleFactor = if (food.referenceQuantity > 0.0) 100.0 / food.referenceQuantity else 1.0
                                val calsPer100g = kotlin.math.round(food.calories * scaleFactor).toInt()
                                val relatedMacroText = when (selectedCategory) {
                                    "Protein" -> "Protein: ${String.format(Locale.US, "%.1f", food.protein * scaleFactor)}g"
                                    "Carbs" -> "Carbs: ${String.format(Locale.US, "%.1f", food.carbs * scaleFactor)}g"
                                    "Fats" -> "Fat: ${String.format(Locale.US, "%.1f", food.fat * scaleFactor)}g"
                                    "Fiber" -> "Fiber: ${String.format(Locale.US, "%.1f", food.fiber * scaleFactor)}g"
                                    else -> "Protein: ${String.format(Locale.US, "%.1f", food.protein * scaleFactor)}g"
                                }

                                Card(
                                    onClick = {
                                        tempSelectedPoolFood = food
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            NutriTealContainer.copy(alpha = 0.55f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NutriTeal else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pool_item_${food.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        MacroAppIcon(
                                            macroOrCategory = food.category,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = food.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "$calsPer100g kcal / 100g",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = " • ",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = relatedMacroText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NutriTeal,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { tempSelectedPoolFood = food },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = NutriTeal,
                                                unselectedColor = MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier.testTag("radio_pool_item_${food.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // FIRST SCREEN: ADD / SUBSTITUTE FOOD FORM
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
                ) {
                    // Meal Category Selector
                    item(key = "meal_type_selector") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = NutriStrings.s(lang, "Select Meal Category", "భోజన సమయం ఎంచుకోండి"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mealTypes.forEach { meal ->
                                    val isSelected = selectedMealType.equals(meal, ignoreCase = true)
                                    val icon = when (meal) {
                                        "Breakfast" -> Icons.Default.WbSunny
                                        "Lunch" -> Icons.Default.LunchDining
                                        "Snacks" -> Icons.Default.Coffee
                                        else -> Icons.Default.Nightlife
                                    }
                                    Surface(
                                        onClick = { selectedMealType = meal },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) NutriTeal else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) NutriTeal else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("form_meal_chip_${meal.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else NutriTeal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = NutriStrings.mealName(meal, lang),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Button to open Food Pool Picker
                    item(key = "select_from_pool_button") {
                        OutlinedButton(
                            onClick = { currentScreenState = "picker" },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, NutriTeal),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NutriTealContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_select_from_food_pool")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = NutriTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = NutriStrings.s(lang, "Select Item from Food Pool", "ఫుడ్ పూల్ నుండి ఆహారాన్ని ఎంచుకోండి"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NutriTeal
                            )
                        }
                    }

                    // Error Banner if form invalid
                    if (formErrorMessage != null) {
                        item(key = "form_error_banner") {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = formErrorMessage!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Food Details Form Fields
                    item(key = "food_details_form") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = NutriStrings.s(lang, "Food Information", "ఆహార వివరాలు"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = foodName,
                                    onValueChange = { foodName = it },
                                    label = { Text(NutriStrings.s(lang, "Food Item Name", "ఆహార పేరు")) },
                                    placeholder = { Text("e.g. Grilled Chicken Breast") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .trackKeyboardFocusBounds()
                                        .testTag("input_food_name")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = NutriStrings.s(lang, "Category", "వర్గం"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        poolCategories.forEach { cat ->
                                            val isCatSelected = category.equals(cat, ignoreCase = true)
                                            val displayCat = when (cat.lowercase()) {
                                                "all" -> NutriStrings.s(lang, "All", "అన్నీ")
                                                "breakfast" -> NutriStrings.mealName("breakfast", lang)
                                                "meals" -> NutriStrings.s(lang, "Meals", "భోజనం")
                                                "snacks" -> NutriStrings.mealName("snacks", lang)
                                                else -> cat
                                            }
                                            FilterChip(
                                                selected = isCatSelected,
                                                onClick = { category = cat },
                                                label = { Text(displayCat, style = MaterialTheme.typography.labelSmall) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = NutriTealContainer,
                                                    selectedLabelColor = NutriTeal
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(50, 100, 150, 200).forEach { preset ->
                                        FilterChip(
                                            selected = quantityInput == preset.toString(),
                                            onClick = { updateQuantityAndScaleMacros(preset.toString()) },
                                            label = { Text("${preset}g", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = NutriTealContainer,
                                                selectedLabelColor = NutriTeal
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quantityInput,
                                        onValueChange = {
                                            updateQuantityAndScaleMacros(it)
                                        },
                                        label = { Text(NutriStrings.s(lang, "Quantity", "పరిమాణం")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_food_quantity")
                                    )

                                    OutlinedTextField(
                                        value = unitInput,
                                        onValueChange = { unitInput = it },
                                        label = { Text(NutriStrings.s(lang, "Unit", "ప్రమాణం")) },
                                        singleLine = true,
                                        placeholder = { Text("g / ml") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_food_unit")
                                    )
                                }
                            }
                        }
                    }

                    // Nutrition Values Form Fields
                    item(key = "nutrition_values_form") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = NutriStrings.s(
                                        lang,
                                        "Nutrition per Serving ($quantityInput $unitInput)",
                                        "ఒక సర్వింగ్ పోషకాలు ($quantityInput $unitInput)"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = caloriesInput,
                                        onValueChange = {
                                            if (it.all { ch -> ch.isDigit() || ch == '.' }) caloriesInput = it
                                        },
                                        label = { Text(NutriStrings.s(lang, "Calories (kcal)", "క్యాలరీలు (kcal)")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_calories")
                                    )

                                    OutlinedTextField(
                                        value = proteinInput,
                                        onValueChange = {
                                            if (it.all { ch -> ch.isDigit() || ch == '.' }) proteinInput = it
                                        },
                                        label = { Text(NutriStrings.s(lang, "Protein (g)", "ప్రోటీన్ (g)")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_protein")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = carbsInput,
                                        onValueChange = {
                                            if (it.all { ch -> ch.isDigit() || ch == '.' }) carbsInput = it
                                        },
                                        label = { Text(NutriStrings.s(lang, "Carbs (g)", "కార్బ్స్ (g)")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_carbs")
                                    )

                                    OutlinedTextField(
                                        value = fatInput,
                                        onValueChange = {
                                            if (it.all { ch -> ch.isDigit() || ch == '.' }) fatInput = it
                                        },
                                        label = { Text(NutriStrings.s(lang, "Fat (g)", "కొవ్వులు (g)")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_fat")
                                    )

                                    OutlinedTextField(
                                        value = fiberInput,
                                        onValueChange = {
                                            if (it.all { ch -> ch.isDigit() || ch == '.' }) fiberInput = it
                                        },
                                        label = { Text(NutriStrings.s(lang, "Fiber (g)", "ఫైబర్ (g)")) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .trackKeyboardFocusBounds()
                                            .testTag("input_fiber")
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
