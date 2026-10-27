package com.example.frontend.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.frontend.util.dismissKeyboardOnTap
import com.example.frontend.util.trackKeyboardFocusBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.data.model.MealConfigItem
import com.example.backend.data.model.MealFoodSelection
import com.example.backend.data.model.RoutineScheduleConfig
import com.example.backend.service.NutritionCalculationService
import com.example.frontend.components.FoodIconHelper
import com.example.frontend.components.FoodPoolSelectorContent
import com.example.frontend.components.MacroAppIcon
import com.example.frontend.components.RoutineSaveReviewDialog
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    allFoods: List<FoodEntity>,
    initialName: String = "",
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onCompleteOnboarding: (
        name: String,
        age: Int,
        biologicalSex: String,
        height: Double,
        weight: Double,
        activity: String,
        build: String,
        selectedFoodIds: List<Long>,
        routineConfig: RoutineScheduleConfig?
    ) -> Unit,
    initialStep: Int = 1,
    initialPoolIds: List<Long> = emptyList(),
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(initialStep.coerceAtLeast(1)) }

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (step > 1) {
            step--
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, NutriStrings.exitWarning(currentLanguage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // User Profile Inputs - Minimum values are shown as placeholders only
    var name by remember(initialName) { mutableStateOf(initialName) }
    var biologicalSex by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Moderate") }
    var bodyBuild by remember { mutableStateOf("Average") }

    // Selected Pool Foods - empty by default so no foods are pre-selected
    val selectedFoodIds = remember { mutableStateListOf<Long>().apply { addAll(initialPoolIds) } }

    // Step 5 & 6 Routine & Reminders State
    var reminderLeadMinutes by remember { mutableIntStateOf(30) }
    var separateWeekendSchedule by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showRoutineReviewDialog by remember { mutableStateOf(false) }

    val weekdayMeals = remember {
        mutableStateListOf(
            MutableMealSchedule("Breakfast", "08:00"),
            MutableMealSchedule("Lunch", "13:00"),
            MutableMealSchedule("Snacks", "17:00"),
            MutableMealSchedule("Dinner", "20:30")
        )
    }

    val weekendMeals = remember {
        mutableStateListOf(
            MutableMealSchedule("Breakfast", "09:30"),
            MutableMealSchedule("Lunch", "14:00"),
            MutableMealSchedule("Snacks", "17:30"),
            MutableMealSchedule("Dinner", "21:00")
        )
    }

    val calculatedTarget = remember(age, height, weight, activityLevel, bodyBuild, biologicalSex) {
        val a = age.toIntOrNull()?.coerceAtLeast(15) ?: 25
        val h = height.toDoubleOrNull()?.coerceAtLeast(100.0) ?: 170.0
        val w = weight.toDoubleOrNull()?.coerceAtLeast(30.0) ?: 65.0
        NutritionCalculationService.calculateTargets(1L, a, h, w, activityLevel, bodyBuild, biologicalSex)
    }

    val calculatedMetrics = remember(age, height, weight, activityLevel, bodyBuild, biologicalSex) {
        val a = age.toIntOrNull()?.coerceAtLeast(15) ?: 25
        val h = height.toDoubleOrNull()?.coerceAtLeast(100.0) ?: 170.0
        val w = weight.toDoubleOrNull()?.coerceAtLeast(30.0) ?: 65.0
        NutritionCalculationService.calculateBodyMetrics(a, h, w, activityLevel, bodyBuild, biologicalSex)
    }

    val currentMeals = if (separateWeekendSchedule && activeTab == 1) weekendMeals else weekdayMeals
    val totalPlannedCalories = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.calories ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }
    val totalPlannedProtein = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.protein ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }
    val totalPlannedCarbs = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.carbs ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }
    val totalPlannedFat = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.fat ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }
    val totalPlannedFiber = currentMeals.sumOf { schedule ->
        schedule.foods.sumOf { f ->
            val food = allFoods.firstOrNull { it.id == f.foodId }
            (food?.fiber ?: 0.0) * (f.plannedQuantity / (food?.referenceQuantity ?: 100.0).coerceAtLeast(1.0))
        }
    }

    fun commitCompletion() {
        val config = RoutineScheduleConfig(
            weekdayPlans = weekdayMeals.map { schedule ->
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
                weekendMeals.map { schedule ->
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
            reminderLeadMinutes = reminderLeadMinutes
        )
        val a = age.toIntOrNull()?.coerceAtLeast(15) ?: 15
        val h = height.toDoubleOrNull()?.coerceAtLeast(100.0) ?: 100.0
        val w = weight.toDoubleOrNull()?.coerceAtLeast(30.0) ?: 30.0
        val effectiveName = name.trim().ifBlank { initialName.ifBlank { "You" } }
        onCompleteOnboarding(
            effectiveName,
            a,
            biologicalSex,
            h,
            w,
            activityLevel,
            bodyBuild,
            selectedFoodIds.toList(),
            config
        )
    }

    if (showRoutineReviewDialog) {
        RoutineSaveReviewDialog(
            totalCalories = totalPlannedCalories,
            totalProtein = totalPlannedProtein,
            totalCarbs = totalPlannedCarbs,
            totalFat = totalPlannedFat,
            totalFiber = totalPlannedFiber,
            target = calculatedTarget,
            onEdit = { showRoutineReviewDialog = false },
            onContinue = {
                showRoutineReviewDialog = false
                step = 7
            },
            lang = currentLanguage
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = NutriStrings.setupTitle(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = NutriStrings.back(currentLanguage))
                        }
                    }
                },
                actions = {
                    // Quick language toggle chip
                    Surface(
                        onClick = {
                            val nextLang = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.TELUGU else AppLanguage.ENGLISH
                            onLanguageChange(nextLang)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = NutriTealContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = "Language", modifier = Modifier.size(16.dp), tint = NutriTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.ENGLISH) "తెలుగు" else "English",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NutriTeal
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = NutriStrings.stepOf(step, 7, currentLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            if (step == 6) {
                                showRoutineReviewDialog = true
                            } else if (step < 7) {
                                step++
                            } else {
                                commitCompletion()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                        modifier = Modifier.testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (step == 7) NutriStrings.startNutriPath(currentLanguage) else NutriStrings.continueBtn(currentLanguage),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .dismissKeyboardOnTap(focusManager, keyboardController)
        ) {
            // Step Progress Indicator
            LinearProgressIndicator(
                progress = { step / 7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NutriTeal,
                trackColor = NutriTeal.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(targetState = step, label = "OnboardingSteps") { currentStep ->
                when (currentStep) {
                    1 -> Step1PersonalInfo(
                        name = name,
                        onNameChange = { name = it },
                        biologicalSex = biologicalSex,
                        onBiologicalSexChange = { biologicalSex = it },
                        age = age,
                        onAgeChange = { age = it },
                        height = height,
                        onHeightChange = { height = it },
                        weight = weight,
                        onWeightChange = { weight = it },
                        activity = activityLevel,
                        onActivityChange = { activityLevel = it },
                        lang = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                    2 -> Step2BodyBuild(
                        selectedBuild = bodyBuild,
                        onSelectBuild = { bodyBuild = it },
                        lang = currentLanguage
                    )
                    3 -> Step3RequirementExplanation(
                        target = calculatedTarget,
                        metrics = calculatedMetrics,
                        lang = currentLanguage
                    )
                    4 -> Step4BuildFoodPool(
                        allFoods = allFoods,
                        selectedIds = selectedFoodIds.toSet(),
                        onToggle = { id ->
                            if (id in selectedFoodIds) selectedFoodIds.remove(id)
                            else selectedFoodIds.add(id)
                        },
                        lang = currentLanguage
                    )
                    5 -> Step5RoutineCautionAndGuidance(
                        target = calculatedTarget,
                        lang = currentLanguage
                    )
                    6 -> Step6CustomizeMealItems(
                        poolFoodIds = selectedFoodIds.toList(),
                        allFoods = allFoods,
                        weekdayMeals = weekdayMeals,
                        weekendMeals = weekendMeals,
                        separateWeekendSchedule = separateWeekendSchedule,
                        activeTab = activeTab,
                        target = calculatedTarget,
                        lang = currentLanguage
                    )
                    7 -> Step7MealTimingsSchedule(
                        weekdayMeals = weekdayMeals,
                        weekendMeals = weekendMeals,
                        separateWeekendSchedule = separateWeekendSchedule,
                        onToggleSeparateWeekend = { checked ->
                            separateWeekendSchedule = checked
                            if (checked && weekendMeals.all { it.foods.isEmpty() }) {
                                weekdayMeals.forEachIndexed { idx, wm ->
                                    wm.foods.forEach { wf ->
                                        weekendMeals.getOrNull(idx)?.foods?.add(MutableMealFood(wf.foodId, wf.plannedQuantity, wf.quantityUnit))
                                    }
                                }
                            }
                        },
                        activeTab = activeTab,
                        onTabChange = { activeTab = it },
                        reminderLeadMinutes = reminderLeadMinutes,
                        onLeadMinutesChange = { reminderLeadMinutes = it },
                        lang = currentLanguage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1PersonalInfo(
    name: String, onNameChange: (String) -> Unit,
    biologicalSex: String, onBiologicalSexChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    activity: String, onActivityChange: (String) -> Unit,
    lang: AppLanguage = LocalAppLanguage.current,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val activityOptions = listOf(
        Pair("Low", NutriStrings.activityLow(lang)),
        Pair("Moderate", NutriStrings.activityModerate(lang)),
        Pair("High", NutriStrings.activityHigh(lang))
    )

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .dismissKeyboardOnTap(focusManager, keyboardController)
    ) {
        item {
            Text(text = NutriStrings.step1Title(lang), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = NutriStrings.step1Subtitle(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            var languageDropdownExpanded by remember { mutableStateOf(false) }

            Column {
                Text(
                    text = if (lang == AppLanguage.TELUGU) "యాప్ భాష (Language)" else "App Language",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = languageDropdownExpanded,
                    onExpandedChange = { languageDropdownExpanded = !languageDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("language_dropdown_box")
                ) {
                    OutlinedTextField(
                        value = if (lang == AppLanguage.ENGLISH) "🇬🇧 English" else "🇮🇳 తెలుగు (Telugu)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (lang == AppLanguage.TELUGU) "భాష ఎంచుకోండి" else "Select Language") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language Icon",
                                tint = NutriTeal
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = languageDropdownExpanded,
                        onDismissRequest = { languageDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🇬🇧 English",
                                    fontWeight = if (lang == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == AppLanguage.ENGLISH) NutriTeal else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onLanguageChange(AppLanguage.ENGLISH)
                                languageDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🇮🇳 తెలుగు (Telugu)",
                                    fontWeight = if (lang == AppLanguage.TELUGU) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == AppLanguage.TELUGU) NutriTeal else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onLanguageChange(AppLanguage.TELUGU)
                                languageDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(NutriStrings.yourName(lang)) },
                modifier = Modifier.fillMaxWidth().trackKeyboardFocusBounds(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
        }

        item {
            Text(NutriStrings.biologicalSex(lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                NutriStrings.biologicalSexDesc(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Pair("Male", NutriStrings.male(lang)),
                    Pair("Female", NutriStrings.female(lang))
                ).forEach { (sexKey, sexLabel) ->
                    val isSelected = biologicalSex.equals(sexKey, ignoreCase = true)
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onBiologicalSexChange(sexKey)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NutriTeal else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sexLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) onAgeChange(input)
                    },
                    label = {
                        Text(
                            text = NutriStrings.ageLabel(lang),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    placeholder = { Text("15") },
                    modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) })
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) onHeightChange(input)
                    },
                    label = {
                        Text(
                            text = NutriStrings.heightLabel(lang),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    placeholder = { Text("100") },
                    modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) })
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) onWeightChange(input)
                    },
                    label = {
                        Text(
                            text = NutriStrings.weightLabel(lang),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    placeholder = { Text("30") },
                    modifier = Modifier.weight(1f).trackKeyboardFocusBounds(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )
            }
        }

        item {
            Text(NutriStrings.activityLevel(lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                NutriStrings.activityLevelDesc(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = activity == key,
                        onClick = { onActivityChange(key) },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Step2BodyBuild(
    selectedBuild: String,
    onSelectBuild: (String) -> Unit,
    lang: AppLanguage = LocalAppLanguage.current
) {
    val builds = listOf(
        Triple("Lean", NutriStrings.buildLean(lang).first, NutriStrings.buildLean(lang).second),
        Triple("Average", NutriStrings.buildAverage(lang).first, NutriStrings.buildAverage(lang).second),
        Triple("Athletic", NutriStrings.buildAthletic(lang).first, NutriStrings.buildAthletic(lang).second),
        Triple("Stocky", NutriStrings.buildStocky(lang).first, NutriStrings.buildStocky(lang).second)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(text = NutriStrings.step2Title(lang), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = NutriStrings.step2Subtitle(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(builds, key = { "build_${it.first}" }) { (key, title, desc) ->
            val isSelected = selectedBuild.equals(key, ignoreCase = true)
            Card(
                onClick = { onSelectBuild(key) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) NutriTeal else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NutriTealContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectBuild(key) },
                        colors = RadioButtonDefaults.colors(selectedColor = NutriTeal)
                    )
                }
            }
        }
    }
}

@Composable
fun Step3RequirementExplanation(
    target: com.example.backend.data.local.entity.NutritionTargetEntity,
    metrics: com.example.backend.logic.model.BodyMetricsSummary? = null,
    lang: AppLanguage = LocalAppLanguage.current
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(text = NutriStrings.step3Title(lang), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = NutriStrings.step3Subtitle(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (metrics != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NutriStrings.bmiTitle(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.bmi} kg/m²",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NutriTeal.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = metrics.bmiCategory,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NutriStrings.bmrShortLabel(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.bmrCalories} kcal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = NutriStrings.tdeeShortLabel(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.tdeeCalories} kcal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = NutriStrings.healthyWeightShortLabel(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.healthyWeightMinKg.toInt()}–${metrics.healthyWeightMaxKg.toInt()} kg",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NutriTeal)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(NutriStrings.dailyCalorieTarget(lang), style = MaterialTheme.typography.labelMedium, color = NutriAmberLight, fontWeight = FontWeight.Bold)
                    Text("${target.caloriesTarget.toInt()} kcal", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
                    Text(NutriStrings.dailyCalorieDesc(lang), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        item {
            RequirementRow(NutriStrings.proteinTitle(lang), "${target.proteinTarget.toInt()} g", NutriStrings.proteinDesc(lang), NutriProteinRed)
            Spacer(modifier = Modifier.height(8.dp))
            RequirementRow(NutriStrings.carbsTitle(lang), "${target.carbsTarget.toInt()} g", NutriStrings.carbsDesc(lang), NutriCarbGold)
            Spacer(modifier = Modifier.height(8.dp))
            RequirementRow(NutriStrings.fatsTitle(lang), "${target.fatTarget.toInt()} g", NutriStrings.fatsDesc(lang), NutriFatOrange)
            Spacer(modifier = Modifier.height(8.dp))
            RequirementRow(NutriStrings.fiberTitle(lang), "${target.fiberTarget.toInt()} g", NutriStrings.fiberDesc(lang), NutriFiberGreen)
            Spacer(modifier = Modifier.height(8.dp))
            RequirementRow(NutriStrings.waterTitle(lang), "${String.format(java.util.Locale.US, "%.1f", target.waterTargetMl / 1000.0)} L", NutriStrings.waterDesc(lang), NutriWaterCyan)
        }
    }
}

@Composable
fun RequirementRow(name: String, amount: String, explanation: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val containerBg = when {
                name.contains("pro", ignoreCase = true) || name.contains("ప్రోటీన్") -> NutriProteinContainerLight
                name.contains("carb", ignoreCase = true) || name.contains("కార్బో") -> NutriCarbContainerLight
                name.contains("fat", ignoreCase = true) || name.contains("కొవ్వు") -> NutriFatContainerLight
                name.contains("fib", ignoreCase = true) || name.contains("పీచు") || name.contains("ఫైబర్") -> NutriFiberContainerLight
                else -> color.copy(alpha = 0.15f)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                MacroAppIcon(
                    macroOrCategory = name,
                    modifier = Modifier.size(28.dp),
                    overrideColor = color
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun Step4BuildFoodPool(
    allFoods: List<FoodEntity>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    lang: AppLanguage = LocalAppLanguage.current
) {
    FoodPoolSelectorContent(
        allFoods = allFoods,
        selectedFoodIds = selectedIds,
        onToggleFood = onToggle,
        headerTitle = NutriStrings.step4Title(lang),
        headerSubtitle = NutriStrings.step4Subtitle(lang),
        showTitle = true,
        contentPadding = PaddingValues(bottom = 24.dp)
    )
}

@Composable
fun Step5RoutineCautionAndGuidance(
    target: NutritionTargetEntity,
    lang: AppLanguage = LocalAppLanguage.current
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = NutriStrings.step5Title(lang),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = NutriStrings.step5Subtitle(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Caution & Advice Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NutriAmberContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, NutriAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NutriAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = NutriAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = NutriStrings.tipsTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = NutriStrings.tipsDesc(target.caloriesTarget.toInt(), lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Target Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = NutriStrings.nutritionBudget(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroBudgetPill(
                            label = NutriStrings.caloriesShortLabel(lang),
                            amount = "${target.caloriesTarget.toInt()}",
                            unit = "kcal",
                            accent = NutriAmber,
                            iconCategory = "Calories",
                            modifier = Modifier.weight(1f)
                        )
                        MacroBudgetPill(
                            label = NutriStrings.proteinShortLabel(lang),
                            amount = "${target.proteinTarget.toInt()}",
                            unit = "g",
                            accent = NutriProteinRed,
                            iconCategory = "Protein",
                            modifier = Modifier.weight(1f)
                        )
                        MacroBudgetPill(
                            label = NutriStrings.carbsShortLabel(lang),
                            amount = "${target.carbsTarget.toInt()}",
                            unit = "g",
                            accent = NutriCarbGold,
                            iconCategory = "Carbohydrates",
                            modifier = Modifier.weight(1f)
                        )
                        MacroBudgetPill(
                            label = NutriStrings.fatsShortLabel(lang),
                            amount = "${target.fatTarget.toInt()}",
                            unit = "g",
                            accent = NutriFatOrange,
                            iconCategory = "Fats",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Recommended Meal Distribution
        item {
            Text(
                text = NutriStrings.recommendedDivision(lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = NutriStrings.balancedMealsDesc(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            val totalCals = target.caloriesTarget
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MealDistributionCard(
                    mealTitle = NutriStrings.breakfastTitle(lang),
                    calories = (totalCals * 0.25).toInt(),
                    advice = NutriStrings.breakfastAdvice(lang),
                    accent = NutriAmber
                )
                MealDistributionCard(
                    mealTitle = NutriStrings.lunchTitle(lang),
                    calories = (totalCals * 0.35).toInt(),
                    advice = NutriStrings.lunchAdvice(lang),
                    accent = NutriTeal
                )
                MealDistributionCard(
                    mealTitle = NutriStrings.snacksTitle(lang),
                    calories = (totalCals * 0.15).toInt(),
                    advice = NutriStrings.snacksAdvice(lang),
                    accent = NutriFatOrange
                )
                MealDistributionCard(
                    mealTitle = NutriStrings.dinnerTitle(lang),
                    calories = (totalCals * 0.25).toInt(),
                    advice = NutriStrings.dinnerAdvice(lang),
                    accent = NutriWaterCyan
                )
            }
        }

        // Reminder Setup Advice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = NutriTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = NutriStrings.mealReminders(lang),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.mealRemindersSub(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroBudgetPill(
    label: String,
    amount: String,
    unit: String,
    accent: Color,
    iconCategory: String = label,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MacroAppIcon(macroOrCategory = iconCategory, modifier = Modifier.size(20.dp), overrideColor = accent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount $unit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MealDistributionCard(
    mealTitle: String,
    calories: Int,
    advice: String,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$calories\nkcal",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mealTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = advice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
