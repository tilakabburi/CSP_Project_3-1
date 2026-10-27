package com.example.frontend.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.frontend.MainViewModel
import com.example.frontend.components.NotificationSimulatorDialog
import com.example.frontend.screens.*
import com.example.frontend.theme.NutriTeal
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.dismissKeyboardOnTap

enum class NutriDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Default.Home, Icons.Outlined.Home),
    LOG("Log", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    INSIGHT("Insight", Icons.Default.BarChart, Icons.Outlined.BarChart),
    PROFILE("Profile", Icons.Default.Person, Icons.Outlined.Person)
}

@Composable
fun NutriPathNav(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val targets by viewModel.targets.collectAsStateWithLifecycle()
    val allFoods by viewModel.allFoods.collectAsStateWithLifecycle()
    val foodPool by viewModel.foodPool.collectAsStateWithLifecycle()
    val dailyPlans by viewModel.dailyPlans.collectAsStateWithLifecycle()
    val mealLogs by viewModel.mealLogsForDate.collectAsStateWithLifecycle()
    val totalWater by viewModel.totalWaterMl.collectAsStateWithLifecycle()
    val consumedNutrition by viewModel.consumedNutrition.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val streakDays by viewModel.streak.collectAsStateWithLifecycle()

    val activeGoal by viewModel.activeDietaryGoal.collectAsStateWithLifecycle()
    val reminderLead by viewModel.reminderLeadMinutes.collectAsStateWithLifecycle()
    val breakfastAlert by viewModel.breakfastNotification.collectAsStateWithLifecycle()
    val lunchAlert by viewModel.lunchNotification.collectAsStateWithLifecycle()
    val dinnerAlert by viewModel.dinnerNotification.collectAsStateWithLifecycle()
    val waterAlert by viewModel.waterNotification.collectAsStateWithLifecycle()
    val waterInterval by viewModel.waterReminderIntervalHours.collectAsStateWithLifecycle()
    val is24HourFormat by viewModel.is24HourFormat.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val heightUnit by viewModel.heightUnit.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(NutriDestination.HOME) }
    var activeSubstituteLogId by remember { mutableStateOf<Long?>(null) }
    var activeSubstituteMealType by remember { mutableStateOf<String?>(null) }
    var activeSubstituteInitialMode by remember { mutableStateOf("pool") }
    var isViewingFoodPool by remember { mutableStateOf(false) }
    var isViewingMealRoutine by remember { mutableStateOf(false) }
    var isViewingMealTimings by remember { mutableStateOf(false) }
    var isViewingMealReminders by remember { mutableStateOf(false) }
    var isViewingEditProfile by remember { mutableStateOf(false) }
    var isViewingWaterSettings by remember { mutableStateOf(false) }
    var isViewingExercise by remember { mutableStateOf(false) }
    var isViewingAppSettings by remember { mutableStateOf(false) }
    var isViewingVideoTour by remember { mutableStateOf(false) }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    // Check deep link events (e.g. from notification "Substitute" action or "Log" review)
    val deepLinkEvent by viewModel.deepLinkEvent.collectAsStateWithLifecycle()
    val deepLinkLogId by viewModel.deepLinkLogId.collectAsStateWithLifecycle()

    LaunchedEffect(deepLinkEvent, deepLinkLogId) {
        when (deepLinkEvent) {
            "substitute" -> {
                activeSubstituteLogId = deepLinkLogId ?: mealLogs.firstOrNull()?.id ?: 1L
                viewModel.clearDeepLink()
            }
            "log" -> {
                currentDestination = NutriDestination.LOG
                viewModel.clearDeepLink()
            }
            "water" -> {
                isViewingWaterSettings = true
                viewModel.clearDeepLink()
            }
            "home" -> {
                currentDestination = NutriDestination.HOME
                viewModel.clearDeepLink()
            }
        }
    }

    LaunchedEffect(currentDestination) {
        viewModel.setSelectedDate(com.example.backend.data.repository.NutriPathRepository.getTodayDateString())
    }

    var showLoadingScreen by remember { mutableStateOf(true) }

    if (showLoadingScreen) {
        NutriPathLoadingScreen(
            onLoadingFinished = {
                showLoadingScreen = false
            }
        )
        return
    }

    // 1. Authentication Layer: If not logged in, show AuthScreen (Login / Register)
    if (user == null || !user!!.isLoggedIn) {
        AuthScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onClearError = { viewModel.clearAuthError() },
            onLogin = { email, password -> viewModel.login(email, password) },
            onRegister = { name, email, password -> viewModel.register(name, email, password) },
            onDemoLogin = { viewModel.login("demo@nutripath.com", "demo") }
        )
        return
    }

    // 2. Profile Setup Questionnaire: If logged in but hasn't completed profile questions, show OnboardingScreen
    if (!user!!.isOnboarded) {
        OnboardingScreen(
            allFoods = allFoods,
            initialName = user?.name ?: "",
            currentLanguage = currentLanguage,
            onLanguageChange = { viewModel.setLanguage(it) },
            onCompleteOnboarding = { name, age, biologicalSex, height, weight, activity, build, selectedFoodIds, routineConfig ->
                viewModel.completeProfileSetup(
                    name = name,
                    age = age,
                    biologicalSex = biologicalSex,
                    height = height,
                    weight = weight,
                    activity = activity,
                    build = build,
                    selectedFoodIds = selectedFoodIds,
                    routineConfig = routineConfig
                )
            }
        )
        return
    }

    // Check if in Substitution Screen
    if (activeSubstituteLogId != null) {
        BackHandler {
            activeSubstituteLogId = null
            activeSubstituteMealType = null
        }
        SubstitutionScreen(
            logId = activeSubstituteLogId!!,
            mealLogs = mealLogs,
            allFoods = allFoods,
            foodPool = foodPool,
            selectedDate = selectedDate,
            initialMealType = activeSubstituteMealType,
            initialMode = activeSubstituteInitialMode,
            onConfirmSubstitution = { replFoodId, qty, unit ->
                viewModel.substituteMeal(
                    logId = activeSubstituteLogId!!,
                    replacementFoodId = replFoodId,
                    quantity = qty,
                    unit = unit,
                    mealTypeOverride = activeSubstituteMealType
                )
                activeSubstituteLogId = null
                activeSubstituteMealType = null
            },
            onLogSubstituteForMeal = { mealType, replFoodId, qty, unit ->
                viewModel.logSubstituteForMeal(mealType, replFoodId, qty, unit)
                activeSubstituteLogId = null
                activeSubstituteMealType = null
            },
            onAddFoodToPool = { foodId ->
                viewModel.addFoodToPool(foodId)
            },
            onCreateCustomFood = { name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated ->
                viewModel.createCustomFood(name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated)
            },
            onNavigateBack = {
                activeSubstituteLogId = null
                activeSubstituteMealType = null
            }
        )
        return
    }

    // Check if in Food Pool Manager Screen
    if (isViewingFoodPool) {
        BackHandler { isViewingFoodPool = false }
        FoodPoolScreen(
            allFoods = allFoods,
            foodPool = foodPool,
            onToggleFoodPool = { foodId, inPool -> viewModel.toggleFoodPoolItem(foodId, inPool) },
            onCreateCustomFood = { name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated ->
                viewModel.createCustomFood(name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated)
            },
            onNavigateBack = { isViewingFoodPool = false },
            onClearAllPool = { viewModel.clearAllFoodPool() }
        )
        return
    }

    // Check if in Meal Routine & Food Selection Screen
    if (isViewingMealRoutine) {
        BackHandler { isViewingMealRoutine = false }
        MealRoutineScheduleScreen(
            dailyPlans = dailyPlans,
            allFoods = allFoods,
            foodPool = foodPool,
            reminderLeadMinutes = reminderLead,
            onSaveRoutine = { config ->
                viewModel.saveCustomMealRoutine(config)
            },
            onOpenSimulator = { showSimulatorDialog = true },
            onNavigateBack = { isViewingMealRoutine = false },
            target = targets
        )
        return
    }

    // Check if in Meal Timings & Schedule Screen
    if (isViewingMealTimings) {
        BackHandler { isViewingMealTimings = false }
        MealTimingsScheduleScreen(
            dailyPlans = dailyPlans,
            reminderLeadMinutes = reminderLead,
            is24HourFormat = is24HourFormat,
            onSaveSchedule = { config ->
                viewModel.saveCustomMealRoutine(config)
            },
            onOpenSimulator = { showSimulatorDialog = true },
            onNavigateBack = { isViewingMealTimings = false }
        )
        return
    }

    // Check if in Meal Reminders Screen
    if (isViewingMealReminders) {
        BackHandler { isViewingMealReminders = false }
        MealRemindersScreen(
            reminderLeadMinutes = reminderLead,
            breakfastNotification = breakfastAlert,
            lunchNotification = lunchAlert,
            dinnerNotification = dinnerAlert,
            waterNotification = waterAlert,
            onReminderLeadChanged = { viewModel.setReminderLeadMinutes(it) },
            onToggleNotification = { type, enabled -> viewModel.toggleMealNotification(type, enabled) },
            onOpenSimulator = { showSimulatorDialog = true },
            onOpenMealRoutineSchedule = {
                isViewingMealReminders = false
                isViewingMealRoutine = true
            },
            onNavigateBack = { isViewingMealReminders = false }
        )
        return
    }

    // Check if in Edit Profile Screen
    if (isViewingEditProfile) {
        BackHandler { isViewingEditProfile = false }
        EditProfileScreen(
            user = user,
            onSaveProfileWithSex = { name, age, height, weight, activity, build, biologicalSex ->
                viewModel.saveUserProfile(name, age, height, weight, activity, build, biologicalSex)
            },
            onNavigateBack = { isViewingEditProfile = false }
        )
        return
    }

    // Check if in Water Settings Screen
    if (isViewingWaterSettings) {
        BackHandler { isViewingWaterSettings = false }
        WaterSettingsScreen(
            currentWaterTarget = targets?.waterTargetMl ?: 2200,
            waterNotificationEnabled = waterAlert,
            onWaterTargetChanged = { viewModel.updateWaterTarget(it) },
            onToggleWaterNotification = { viewModel.toggleMealNotification("water", it) },
            onQuickLogWater = { ml -> viewModel.logWater(ml) },
            onNavigateBack = { isViewingWaterSettings = false },
            reminderIntervalHours = waterInterval,
            onIntervalHoursChanged = { viewModel.setWaterReminderInterval(it) },
            onSimulateWaterReminder = { viewModel.triggerWaterReminderSimulator() }
        )
        return
    }

    // Check if in Exercise / Daily Movement Screen
    if (isViewingExercise) {
        BackHandler { isViewingExercise = false }
        ExerciseScreen(
            onNavigateBack = { isViewingExercise = false }
        )
        return
    }

    // Check if in App Settings Screen
    if (isViewingAppSettings) {
        BackHandler { isViewingAppSettings = false }
        SettingsScreen(
            currentLanguage = currentLanguage,
            onLanguageChange = { lang -> viewModel.setLanguage(lang) },
            is24HourFormat = is24HourFormat,
            onTimeFormatChange = { is24h -> viewModel.setTimeFormat(is24h) },
            weightUnit = weightUnit,
            onWeightUnitChange = { u -> viewModel.setWeightUnit(u) },
            heightUnit = heightUnit,
            onHeightUnitChange = { u -> viewModel.setHeightUnit(u) },
            onOpenSimulator = { showSimulatorDialog = true },
            onNavigateBack = { isViewingAppSettings = false },
            onOpenVideoTour = {
                isViewingAppSettings = false
                isViewingVideoTour = true
            },
            onResetApp = {
                viewModel.resetAppToBeginning()
                isViewingAppSettings = false
                currentDestination = NutriDestination.HOME
            },
            onClearInputs = {
                viewModel.clearAllInputs()
                isViewingAppSettings = false
                currentDestination = NutriDestination.HOME
            }
        )
        return
    }

    // Check if in Video Walkthrough Showcase Screen
    if (isViewingVideoTour) {
        BackHandler { isViewingVideoTour = false }
        VideoWalkthroughScreen(
            onNavigateBack = { isViewingVideoTour = false }
        )
        return
    }

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (showSimulatorDialog) {
            showSimulatorDialog = false
        } else if (currentDestination != NutriDestination.HOME) {
            viewModel.setSelectedDate(com.example.backend.data.repository.NutriPathRepository.getTodayDateString())
            currentDestination = NutriDestination.HOME
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NutriDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    val title = when (destination) {
                        NutriDestination.HOME -> NutriStrings.navHome(currentLanguage)
                        NutriDestination.LOG -> NutriStrings.navLog(currentLanguage)
                        NutriDestination.INSIGHT -> NutriStrings.navInsight(currentLanguage)
                        NutriDestination.PROFILE -> NutriStrings.navProfile(currentLanguage)
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            viewModel.setSelectedDate(com.example.backend.data.repository.NutriPathRepository.getTodayDateString())
                            currentDestination = destination
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NutriTeal,
                            selectedTextColor = NutriTeal,
                            indicatorColor = NutriTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.title.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dismissKeyboardOnTap(focusManager, keyboardController)
        ) {
            when (currentDestination) {
                NutriDestination.HOME -> HomeScreen(
                    consumedNutrition = consumedNutrition,
                    targets = targets,
                    totalWaterMl = totalWater,
                    streakDays = streakDays,
                    mealLogs = mealLogs,
                    allFoods = allFoods,
                    onAddWater = { amount -> viewModel.logWater(amount) },
                    onMarkConsumed = { logId -> viewModel.markConsumed(logId) },
                    onNavigateToSubstitute = { logId ->
                        activeSubstituteMealType = null
                        activeSubstituteInitialMode = "pool"
                        activeSubstituteLogId = logId
                    },
                    onOpenSimulator = { showSimulatorDialog = true },
                    onSimulateWaterReminder = { viewModel.triggerWaterReminderSimulator() }
                )
                NutriDestination.LOG -> DailyLogScreen(
                    selectedDate = selectedDate,
                    mealLogs = mealLogs,
                    allFoods = allFoods,
                    dailyPlans = dailyPlans,
                    foodPool = foodPool,
                    onDateChanged = { newDate -> viewModel.setSelectedDate(newDate) },
                    onMarkConsumed = { logId -> viewModel.markConsumed(logId) },
                    onMarkWillEat = { logId -> viewModel.markWillEat(logId) },
                    onSubstitute = { logId ->
                        activeSubstituteMealType = null
                        activeSubstituteInitialMode = "pool"
                        activeSubstituteLogId = logId
                    },
                    onOpenSubstituteForMeal = { mealType, initialMode ->
                        activeSubstituteMealType = mealType
                        activeSubstituteInitialMode = initialMode
                        activeSubstituteLogId = 0L
                    },
                    onDeleteSubstituteLog = { logId ->
                        viewModel.deleteMealLog(logId)
                    },
                    onCreateCustomFood = { name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated ->
                        viewModel.createCustomFood(name, cat, refQty, refUnit, cals, p, c, f, fib, onCreated)
                    },
                    onLogExtraFood = { mealType, foodId, qty, unit ->
                        viewModel.logExtraFoodFromPool(mealType, foodId, qty, unit)
                    },
                    onRestoreOriginalFood = { logId ->
                        viewModel.restoreOriginalFood(logId)
                    }
                )
                NutriDestination.INSIGHT -> InsightScreen(
                    viewModel = viewModel
                )
                NutriDestination.PROFILE -> ProfileScreen(
                    user = user,
                    targets = targets,
                    is24HourFormat = is24HourFormat,
                    onOpenEditProfile = { isViewingEditProfile = true },
                    onOpenFoodPool = { isViewingFoodPool = true },
                    onOpenMealRoutine = { isViewingMealRoutine = true },
                    onOpenMealTimings = { isViewingMealTimings = true },
                    onOpenReminderSettings = { isViewingMealReminders = true },
                    onOpenWaterSettings = { isViewingWaterSettings = true },
                    onOpenExercise = { isViewingExercise = true },
                    onOpenSettings = { isViewingAppSettings = true },
                    onOpenVideoTour = { isViewingVideoTour = true },
                    onLogOut = {
                        viewModel.logout()
                        currentDestination = NutriDestination.HOME
                    },
                    onResetApp = {
                        viewModel.resetAppToBeginning()
                        currentDestination = NutriDestination.HOME
                    }
                )
            }
        }
    }

    if (showSimulatorDialog) {
        NotificationSimulatorDialog(
            onDismiss = { showSimulatorDialog = false },
            onTriggerStage1 = { viewModel.triggerSimulator(stage = 1) },
            onTriggerStage2 = { viewModel.triggerSimulator(stage = 2) },
            onTriggerStage3 = { viewModel.triggerSimulator(stage = 3) },
            onTriggerWaterReminder = { viewModel.triggerSimulator(stage = 4) },
            onTriggerFullDay = { viewModel.triggerFullDayNotificationSimulation() },
            getFullDayList = { viewModel.getFullDayNotificationSequence() },
            onTriggerSingleItem = { item -> viewModel.triggerSingleNotificationFromSequence(item) }
        )
    }
}
