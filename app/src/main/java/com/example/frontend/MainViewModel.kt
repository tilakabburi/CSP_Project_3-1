package com.example.frontend

import android.content.Context
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NutriPathApplication
import com.example.backend.data.local.entity.*
import com.example.backend.data.model.*
import com.example.backend.data.repository.NutriPathRepository
import com.example.backend.service.CalculatedNutrition
import com.example.backend.service.NutritionCalculationService
import com.example.backend.service.ReminderScheduler
import com.example.backend.service.SimulatedNotificationInfo
import com.example.frontend.util.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WeeklyDayStat(
    val dayLabel: String, // e.g. "Mon"
    val dateString: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val waterMl: Int,
    val isTargetHit: Boolean
)

data class InsightSummary(
    val averageCalories: Double,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFat: Double,
    val averageFiber: Double,
    val averageWaterMl: Int,
    val waterGoalDaysHit: Int,
    val completionRatePercent: Int,
    val weeklyDays: List<WeeklyDayStat>,
    val targetCalories: Double = 2100.0,
    val targetProtein: Double = 120.0,
    val targetCarbs: Double = 220.0,
    val targetFat: Double = 50.0,
    val targetFiber: Double = 28.0,
    val targetWaterMl: Int = 2200,
    val totalDays: Int = 7,
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalWaterMl: Int = 0
) {
    // Period-scaled goals: daily goal multiplied by days in period (e.g. 7 for weekly, 30 for monthly)
    val periodTargetCalories: Double get() = (targetCalories * totalDays).coerceAtLeast(1.0)
    val periodTargetProtein: Double get() = (targetProtein * totalDays).coerceAtLeast(1.0)
    val periodTargetCarbs: Double get() = (targetCarbs * totalDays).coerceAtLeast(1.0)
    val periodTargetFat: Double get() = (targetFat * totalDays).coerceAtLeast(1.0)
    val periodTargetFiber: Double get() = (targetFiber * totalDays).coerceAtLeast(1.0)
    val periodTargetWaterMl: Int get() = (targetWaterMl * totalDays).coerceAtLeast(1)

    // Period percentages
    val caloriePercent: Int
        get() = ((totalCalories / periodTargetCalories) * 100).toInt().coerceAtLeast(0)

    val proteinPercent: Int
        get() = ((totalProtein / periodTargetProtein) * 100).toInt().coerceAtLeast(0)

    val waterPercent: Int
        get() = ((totalWaterMl.toDouble() / periodTargetWaterMl) * 100).toInt().coerceAtLeast(0)

    val fiberPercent: Int
        get() = ((totalFiber / periodTargetFiber) * 100).toInt().coerceAtLeast(0)

    val carbsPercent: Int
        get() = ((totalCarbs / periodTargetCarbs) * 100).toInt().coerceAtLeast(0)

    val fatPercent: Int
        get() = ((totalFat / periodTargetFat) * 100).toInt().coerceAtLeast(0)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NutriPathApplication).repository
    private val prefs = application.getSharedPreferences("nutripath_language_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("pref_selected_language", "en") ?: "en")
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("pref_selected_language", language.code).apply()
    }

    private val _is24HourFormat = MutableStateFlow(
        prefs.getBoolean("pref_is_24_hour_format", false)
    )
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    fun setTimeFormat(is24Hour: Boolean) {
        _is24HourFormat.value = is24Hour
        prefs.edit().putBoolean("pref_is_24_hour_format", is24Hour).apply()
    }

    private val _weightUnit = MutableStateFlow(
        prefs.getString("pref_weight_unit", "kg (Metric)") ?: "kg (Metric)"
    )
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    fun setWeightUnit(unit: String) {
        _weightUnit.value = unit
        prefs.edit().putString("pref_weight_unit", unit).apply()
    }

    private val _heightUnit = MutableStateFlow(
        prefs.getString("pref_height_unit", "cm (Metric)") ?: "cm (Metric)"
    )
    val heightUnit: StateFlow<String> = _heightUnit.asStateFlow()

    fun setHeightUnit(unit: String) {
        _heightUnit.value = unit
        prefs.edit().putString("pref_height_unit", unit).apply()
    }

    val authLoading = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)

    fun clearAuthError() {
        authError.value = null
    }

    val selectedDate = MutableStateFlow(NutriPathRepository.getTodayDateString())
    val dataRefreshTrigger = MutableStateFlow(0)

    val user: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val targets: StateFlow<NutritionTargetEntity?> = user.flatMapLatest { u ->
        if (u != null) repository.getTargetsFlow(u.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allFoods: StateFlow<List<FoodEntity>> = repository.allFoodsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodPool: StateFlow<List<FoodPoolEntity>> = user.flatMapLatest { u ->
        if (u != null) repository.getFoodPoolFlow(u.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyPlans: StateFlow<List<DailyPlanEntity>> = user.flatMapLatest { u ->
        if (u != null) repository.getDailyPlanFlow(u.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val mealLogsForDate: StateFlow<List<MealLogEntity>> = combine(user, selectedDate, dataRefreshTrigger) { u, date, _ ->
        Pair(u, date)
    }.flatMapLatest { (u, date) ->
        if (u != null) {
            flow {
                repository.ensureTodayMealLogs(u.id, date)
                emitAll(repository.getLogsForDateFlow(u.id, date))
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val waterLogsForDate: StateFlow<List<WaterLogEntity>> = combine(user, selectedDate, dataRefreshTrigger) { u, date, _ ->
        Pair(u, date)
    }.flatMapLatest { (u, date) ->
        if (u != null) repository.getWaterLogsForDateFlow(u.id, date) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val streak: StateFlow<Int> = combine(user, dataRefreshTrigger) { u, _ -> u }.flatMapLatest { u ->
        if (u != null) flow { emit(repository.calculateCurrentStreak(u.id)) } else flowOf(1)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            val result = repository.login(email, password)
            authLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                authError.value = err.message ?: "Login failed. Please check your credentials."
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            val result = repository.register(name, email, password)
            authLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                authError.value = err.message ?: "Registration failed. Please try again."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun completeProfileSetup(
        name: String,
        age: Int,
        biologicalSex: String,
        height: Double,
        weight: Double,
        activity: String,
        build: String,
        selectedFoodIds: List<Long>,
        routineConfig: RoutineScheduleConfig? = null
    ) {
        val currentUser = user.value
        val userId = currentUser?.id ?: 1L
        viewModelScope.launch {
            repository.completeProfileSetup(
                userId = userId,
                name = name,
                age = age,
                biologicalSex = biologicalSex,
                heightCm = height,
                weightKg = weight,
                activityLevel = activity,
                bodyBuild = build,
                selectedFoodIds = selectedFoodIds,
                routineConfig = routineConfig
            )
            if (routineConfig != null) {
                reminderLeadMinutes.value = routineConfig.reminderLeadMinutes
                prefs.edit().putInt("pref_reminder_lead_minutes", routineConfig.reminderLeadMinutes).apply()
            }
            rescheduleAlarms()
            dataRefreshTrigger.value++
        }
    }

    fun saveCustomMealRoutine(routineConfig: RoutineScheduleConfig, onSaved: () -> Unit = {}) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.saveCustomMealRoutine(currentUser.id, routineConfig)
            reminderLeadMinutes.value = routineConfig.reminderLeadMinutes
            prefs.edit().putInt("pref_reminder_lead_minutes", routineConfig.reminderLeadMinutes).apply()
            rescheduleAlarms()
            dataRefreshTrigger.value++
            onSaved()
        }
    }

    // Central Single Source of Truth for Daily Nutrition Aggregation
    val consumedNutrition: StateFlow<CalculatedNutrition> = mealLogsForDate.map { logs ->
        NutritionCalculationService.aggregateDailyNutrition(logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculatedNutrition(0.0, 0.0, 0.0, 0.0, 0.0))

    // Total Water Consumed for Date
    val totalWaterMl: StateFlow<Int> = waterLogsForDate.map { logs ->
        logs.sumOf { it.quantityMl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Deep Link target for Notification actions
    val deepLinkEvent = MutableStateFlow<String?>(null)
    val deepLinkLogId = MutableStateFlow<Long?>(null)

    fun handleDeepLink(link: String?, logId: Long?) {
        deepLinkEvent.value = link
        deepLinkLogId.value = logId
    }

    fun clearDeepLink() {
        deepLinkEvent.value = null
        deepLinkLogId.value = null
    }

    fun setSelectedDate(dateStr: String) {
        selectedDate.value = dateStr
    }

    fun markConsumed(logId: Long) {
        viewModelScope.launch {
            repository.markLogConsumed(logId)
        }
    }

    fun markWillEat(logId: Long) {
        viewModelScope.launch {
            repository.markLogWillEat(logId)
        }
    }

    fun substituteMeal(
        logId: Long,
        replacementFoodId: Long,
        quantity: Double,
        unit: String,
        mealTypeOverride: String? = null
    ) {
        val currentUser = user.value
        viewModelScope.launch {
            if (currentUser != null) {
                repository.addFoodToPool(currentUser.id, replacementFoodId)
            }
            if (logId > 0L) {
                repository.substituteFood(logId, replacementFoodId, quantity, unit)
            } else if (!mealTypeOverride.isNullOrBlank() && currentUser != null) {
                repository.logSubstituteFood(
                    userId = currentUser.id,
                    date = selectedDate.value,
                    mealType = mealTypeOverride,
                    replacementFoodId = replacementFoodId,
                    replacementQuantity = quantity,
                    replacementUnit = unit,
                    status = "consumed"
                )
            } else if (currentUser != null) {
                repository.logSubstituteFood(
                    userId = currentUser.id,
                    date = selectedDate.value,
                    mealType = "Breakfast",
                    replacementFoodId = replacementFoodId,
                    replacementQuantity = quantity,
                    replacementUnit = unit,
                    status = "consumed"
                )
            }
        }
    }

    fun restoreOriginalFood(logId: Long) {
        viewModelScope.launch {
            repository.restoreOriginalFood(logId)
        }
    }

    fun logSubstituteForMeal(
        mealType: String,
        replacementFoodId: Long,
        quantity: Double,
        unit: String,
        date: String = selectedDate.value
    ) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.addFoodToPool(currentUser.id, replacementFoodId)
            repository.logSubstituteFood(
                userId = currentUser.id,
                date = date,
                mealType = mealType,
                replacementFoodId = replacementFoodId,
                replacementQuantity = quantity,
                replacementUnit = unit
            )
        }
    }

    fun deleteMealLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteMealLog(logId)
        }
    }

    fun addFoodToPool(foodId: Long) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.addFoodToPool(currentUser.id, foodId)
        }
    }

    fun logExtraFoodFromPool(mealType: String, foodId: Long, quantity: Double, unit: String) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.addFoodToPool(currentUser.id, foodId)
            repository.logExtraFood(
                userId = currentUser.id,
                date = selectedDate.value,
                mealType = mealType,
                foodId = foodId,
                quantity = quantity,
                unit = unit
            )
        }
    }

    fun logWater(amountMl: Int) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.logWater(currentUser.id, amountMl, selectedDate.value)
        }
    }

    fun toggleFoodPoolItem(foodId: Long, currentlyInPool: Boolean) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            if (currentlyInPool) {
                repository.removeFoodFromPool(currentUser.id, foodId)
            } else {
                repository.addFoodToPool(currentUser.id, foodId)
            }
        }
    }

    fun clearAllFoodPool() {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.clearFoodPoolForUser(currentUser.id)
        }
    }

    fun createCustomFood(
        name: String,
        category: String,
        referenceQuantity: Double,
        referenceUnit: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        onCreated: (Long) -> Unit = {}
    ) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            val id = repository.createCustomFood(
                userId = currentUser.id,
                name = name,
                category = category,
                referenceQuantity = referenceQuantity,
                referenceUnit = referenceUnit,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = fiber
            )
            onCreated(id)
        }
    }

    fun addDailyPlan(mealType: String, foodId: Long, quantity: Double, unit: String, time: String) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.saveDailyPlan(currentUser.id, mealType, foodId, quantity, unit, time)
            rescheduleAlarms()
        }
    }

    fun deleteDailyPlan(planId: Long) {
        viewModelScope.launch {
            repository.deleteDailyPlan(planId)
            rescheduleAlarms()
        }
    }

    private fun rescheduleAlarms() {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            val allPlans = repository.getDailyPlans(currentUser.id)
            val enabledPlans = allPlans.filter { plan ->
                when (plan.mealType.lowercase()) {
                    "breakfast" -> breakfastNotification.value
                    "lunch" -> lunchNotification.value
                    "dinner" -> dinnerNotification.value
                    else -> true
                }
            }
            val foodsMap = repository.getAllFoods().associateBy { it.id }
            ReminderScheduler.scheduleDailyReminders(getApplication(), enabledPlans, foodsMap, reminderLeadMinutes.value)
        }
    }

    fun triggerSimulator(stage: Int, mealType: String = "Breakfast", logId: Long = 1L) {
        if (stage == 4) {
            triggerWaterReminderSimulator()
            return
        }
        val logs = mealLogsForDate.value
        val targetLog = logs.firstOrNull { it.mealType.equals(mealType, ignoreCase = true) } ?: logs.firstOrNull()

        val plans = dailyPlans.value.filter { it.mealType.equals(mealType, ignoreCase = true) }
        val summary = if (plans.isNotEmpty()) {
            plans.joinToString(", ") { p ->
                val fName = allFoods.value.firstOrNull { it.id == p.foodId }?.name ?: "Food"
                "${p.plannedQuantity.toInt()} ${p.quantityUnit} $fName"
            }
        } else {
            val targetLogs = logs.filter { it.mealType.equals(mealType, ignoreCase = true) }
            if (targetLogs.isNotEmpty()) {
                targetLogs.joinToString(", ") { l ->
                    val fName = allFoods.value.firstOrNull { it.id == l.plannedFoodId }?.name ?: "Food"
                    "${l.plannedQuantity.toInt()} ${l.quantityUnit} $fName"
                }
            } else {
                "100 g Oatmeal, cooked, 100 g Cows' milk, 100 g Eggs, raw"
            }
        }

        ReminderScheduler.triggerSimulatedNotification(
            context = getApplication(),
            stage = stage,
            mealType = mealType,
            mealTime = "08:00 AM",
            foodSummary = summary,
            logId = targetLog?.id ?: logId,
            currentWaterMl = totalWaterMl.value,
            targetWaterMl = targets.value?.waterTargetMl ?: 2200
        )
    }

    fun triggerWaterReminderSimulator() {
        val currentWater = totalWaterMl.value
        val targetWater = targets.value?.waterTargetMl ?: 2200
        ReminderScheduler.triggerSimulatedNotification(
            context = getApplication(),
            stage = 4,
            currentWaterMl = currentWater,
            targetWaterMl = targetWater
        )
    }

    /**
     * Triggers all 12 notifications across an entire day into Android's status bar / notification drawer,
     * and returns the simulated sequence for UI inspection and verification.
     */
    fun triggerFullDayNotificationSimulation(): List<SimulatedNotificationInfo> {
        val plans = dailyPlans.value
        val foodsMap = allFoods.value.associateBy { it.id }
        val leadMinutes = reminderLeadMinutes.value
        val currentWater = totalWaterMl.value
        val targetWater = targets.value?.waterTargetMl ?: 2200

        return ReminderScheduler.triggerFullDaySimulation(
            context = getApplication(),
            plans = plans,
            foodsMap = foodsMap,
            leadMinutes = leadMinutes,
            currentWaterMl = currentWater,
            targetWaterMl = targetWater
        )
    }

    /**
     * Retrieves the sequence of 12 notifications for the complete day without firing system notifications.
     */
    fun getFullDayNotificationSequence(): List<SimulatedNotificationInfo> {
        val plans = dailyPlans.value
        val foodsMap = allFoods.value.associateBy { it.id }
        val leadMinutes = reminderLeadMinutes.value
        val currentWater = totalWaterMl.value
        val targetWater = targets.value?.waterTargetMl ?: 2200

        return ReminderScheduler.getFullDayNotificationSequence(
            plans = plans,
            foodsMap = foodsMap,
            leadMinutes = leadMinutes,
            currentWaterMl = currentWater,
            targetWaterMl = targetWater
        )
    }

    /**
     * Re-triggers an individual item from the 1-day notification sequence.
     */
    fun triggerSingleNotificationFromSequence(item: SimulatedNotificationInfo) {
        val currentWater = totalWaterMl.value
        val targetWater = targets.value?.waterTargetMl ?: 2200

        ReminderScheduler.triggerSingleItemFromSequence(
            context = getApplication(),
            item = item,
            currentWaterMl = currentWater,
            targetWaterMl = targetWater
        )
    }

    fun saveUserProfile(
        name: String,
        age: Int,
        height: Double,
        weight: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String? = null,
        selectedFoodIds: List<Long>? = null
    ) {
        viewModelScope.launch {
            val userId = repository.saveUserAndCalculateTargets(
                name = name,
                age = age,
                heightCm = height,
                weightKg = weight,
                activityLevel = activityLevel,
                bodyBuild = bodyBuild,
                biologicalSex = biologicalSex
            )
            if (!selectedFoodIds.isNullOrEmpty()) {
                selectedFoodIds.forEach { foodId ->
                    repository.addFoodToPool(userId, foodId)
                }
            }
            dataRefreshTrigger.value++
        }
    }

    fun resetAppToBeginning() {
        viewModelScope.launch {
            repository.resetAppToBeginning()
            dataRefreshTrigger.value++
        }
    }

    fun clearAllInputs() {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.clearAllMealLogsAndSelections(currentUser.id)
            dataRefreshTrigger.value++
        }
    }

    fun resetInsights() {
        val currentUser = user.value
        viewModelScope.launch {
            if (currentUser != null) {
                repository.clearMealAndWaterHistoryOnly(currentUser.id)
            }
            dataRefreshTrigger.value++
        }
    }

    val reminderLeadMinutes = MutableStateFlow(prefs.getInt("pref_reminder_lead_minutes", 30))
    val breakfastNotification = MutableStateFlow(prefs.getBoolean("pref_breakfast_notification", true))
    val lunchNotification = MutableStateFlow(prefs.getBoolean("pref_lunch_notification", true))
    val dinnerNotification = MutableStateFlow(prefs.getBoolean("pref_dinner_notification", true))
    val waterNotification = MutableStateFlow(prefs.getBoolean("pref_water_notification", true))
    val waterReminderIntervalHours = MutableStateFlow(prefs.getInt("pref_water_interval_hours", 2))
    val activeDietaryGoal = MutableStateFlow(prefs.getString("pref_active_dietary_goal", "Balanced Maintenance") ?: "Balanced Maintenance")

    init {
        rescheduleWaterAlarms()
    }

    fun setReminderLeadMinutes(minutes: Int) {
        reminderLeadMinutes.value = minutes
        prefs.edit().putInt("pref_reminder_lead_minutes", minutes).apply()
        rescheduleAlarms()
    }

    fun setWaterReminderInterval(hours: Int) {
        waterReminderIntervalHours.value = hours
        prefs.edit().putInt("pref_water_interval_hours", hours).apply()
        rescheduleWaterAlarms()
    }

    fun rescheduleWaterAlarms() {
        ReminderScheduler.scheduleWaterReminders(
            context = getApplication(),
            enabled = waterNotification.value,
            intervalHours = waterReminderIntervalHours.value
        )
    }

    fun toggleMealNotification(type: String, enabled: Boolean) {
        when (type.lowercase()) {
            "breakfast" -> {
                breakfastNotification.value = enabled
                prefs.edit().putBoolean("pref_breakfast_notification", enabled).apply()
                rescheduleAlarms()
            }
            "lunch" -> {
                lunchNotification.value = enabled
                prefs.edit().putBoolean("pref_lunch_notification", enabled).apply()
                rescheduleAlarms()
            }
            "dinner" -> {
                dinnerNotification.value = enabled
                prefs.edit().putBoolean("pref_dinner_notification", enabled).apply()
                rescheduleAlarms()
            }
            "water" -> {
                waterNotification.value = enabled
                prefs.edit().putBoolean("pref_water_notification", enabled).apply()
                rescheduleWaterAlarms()
            }
        }
    }

    fun updateGoalPreset(goalName: String) {
        activeDietaryGoal.value = goalName
        prefs.edit().putString("pref_active_dietary_goal", goalName).apply()
        val currentUser = user.value ?: return
        val currentTarget = targets.value ?: return
        viewModelScope.launch {
            val base = NutritionCalculationService.calculateTargets(
                userId = currentUser.id,
                age = currentUser.age,
                heightCm = currentUser.heightCm,
                weightKg = currentUser.weightKg,
                activityLevel = currentUser.activityLevel,
                bodyBuild = currentUser.bodyBuild,
                biologicalSex = currentUser.biologicalSex
            )
            val isFemale = currentUser.biologicalSex.equals("Female", ignoreCase = true)
            val minSafeCals = if (isFemale) 1200.0 else 1400.0
            val updated = when (goalName) {
                "Fat Loss" -> {
                    val cals = (base.caloriesTarget - 350.0).coerceAtLeast(minSafeCals)
                    val p = kotlin.math.round(base.proteinTarget * 1.15)
                    val f = kotlin.math.round(cals * 0.25 / 9.0)
                    val c = kotlin.math.round(((cals - (p * 4.0 + f * 9.0)) / 4.0).coerceAtLeast(60.0))
                    val fib = kotlin.math.round(((cals / 1000.0) * 14.0).coerceIn(20.0, 42.0))
                    currentTarget.copy(
                        caloriesTarget = cals,
                        proteinTarget = p,
                        carbsTarget = c,
                        fatTarget = f,
                        fiberTarget = fib,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                "Muscle Growth" -> {
                    val cals = base.caloriesTarget + 300.0
                    val p = kotlin.math.round(base.proteinTarget * 1.20)
                    val f = kotlin.math.round(cals * 0.25 / 9.0)
                    val c = kotlin.math.round(((cals - (p * 4.0 + f * 9.0)) / 4.0).coerceAtLeast(100.0))
                    val fib = kotlin.math.round(((cals / 1000.0) * 14.0).coerceIn(20.0, 42.0))
                    currentTarget.copy(
                        caloriesTarget = cals,
                        proteinTarget = p,
                        carbsTarget = c,
                        fatTarget = f,
                        fiberTarget = fib,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                "High Protein" -> {
                    val cals = base.caloriesTarget
                    val p = kotlin.math.round(cals * 0.35 / 4.0)
                    val f = kotlin.math.round(cals * 0.25 / 9.0)
                    val c = kotlin.math.round(cals * 0.40 / 4.0)
                    currentTarget.copy(
                        caloriesTarget = cals,
                        proteinTarget = p,
                        carbsTarget = c,
                        fatTarget = f,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                "Low Carb" -> {
                    val cals = base.caloriesTarget
                    val p = kotlin.math.round(cals * 0.30 / 4.0)
                    val f = kotlin.math.round(cals * 0.45 / 9.0)
                    val c = kotlin.math.round(cals * 0.25 / 4.0)
                    currentTarget.copy(
                        caloriesTarget = cals,
                        proteinTarget = p,
                        carbsTarget = c,
                        fatTarget = f,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                else -> base.copy(id = currentTarget.id)
            }
            repository.updateNutritionTargets(updated)
            dataRefreshTrigger.value++
        }
    }

    fun updateWaterTarget(newWaterMl: Int) {
        val currentTarget = targets.value ?: return
        viewModelScope.launch {
            val updated = currentTarget.copy(waterTargetMl = newWaterMl, updatedAt = System.currentTimeMillis())
            repository.updateNutritionTargets(updated)
            dataRefreshTrigger.value++
        }
    }

    /**
     * Compute weekly & monthly insights based on Room logs
     */
    suspend fun computeWeeklyInsights(daysCount: Int = 7): InsightSummary {
        val currentUser = user.value ?: return emptyInsightSummary(targets.value, daysCount)
        val target = targets.value ?: return emptyInsightSummary(null, daysCount)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val dayStats = mutableListOf<WeeklyDayStat>()
        var sumCals = 0.0
        var sumProtein = 0.0
        var sumCarbs = 0.0
        var sumFat = 0.0
        var sumFiber = 0.0
        var sumWater = 0
        var waterGoalDays = 0
        var completedDays = 0

        val count = if (daysCount > 0) daysCount else 7
        val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(count - 1)) }
        val startDateStr = sdf.format(startCal.time)
        val endDateStr = sdf.format(Date())

        val allLogsGrouped = repository.getLogsForRange(currentUser.id, startDateStr, endDateStr).groupBy { it.date }
        val allWaterGrouped = repository.getWaterLogsForRange(currentUser.id, startDateStr, endDateStr).groupBy { it.date }

        // Lookback for the chosen period
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(c.time)
            val dayLabel = dayFormat.format(c.time)

            val logs = allLogsGrouped[dateStr] ?: emptyList()
            val waterLogs = allWaterGrouped[dateStr] ?: emptyList()
            val dayNutri = NutritionCalculationService.aggregateDailyNutrition(logs)
            val dayWater = waterLogs.sumOf { it.quantityMl }

            sumCals += dayNutri.calories
            sumProtein += dayNutri.protein
            sumCarbs += dayNutri.carbs
            sumFat += dayNutri.fat
            sumFiber += dayNutri.fiber
            sumWater += dayWater

            val isWaterHit = target.waterTargetMl > 0 && dayWater.toDouble() >= (target.waterTargetMl * 0.8)
            val isNutriHit = target.caloriesTarget > 0 && dayNutri.calories >= (target.caloriesTarget * 0.6)
            if (isWaterHit) waterGoalDays++
            if (isNutriHit || isWaterHit) completedDays++

            dayStats.add(
                WeeklyDayStat(
                    dayLabel = dayLabel,
                    dateString = dateStr,
                    calories = dayNutri.calories,
                    protein = dayNutri.protein,
                    carbs = dayNutri.carbs,
                    fat = dayNutri.fat,
                    fiber = dayNutri.fiber,
                    waterMl = dayWater,
                    isTargetHit = isNutriHit || isWaterHit
                )
            )
        }

        val rawWaterTarget = target.waterTargetMl.takeIf { it > 0 } ?: 2200
        val targetCals = target.caloriesTarget.takeIf { it > 0.0 } ?: 2100.0
        val targetProt = target.proteinTarget.takeIf { it > 0.0 } ?: 120.0
        val targetCarb = target.carbsTarget.takeIf { it > 0.0 } ?: 220.0
        val targetF = target.fatTarget.takeIf { it > 0.0 } ?: 50.0
        val targetFib = target.fiberTarget.takeIf { it > 0.0 } ?: 28.0

        val calScore = ((sumCals / (targetCals * count)) * 100).coerceIn(0.0, 100.0)
        val protScore = ((sumProtein / (targetProt * count)) * 100).coerceIn(0.0, 100.0)
        val waterScore = ((sumWater.toDouble() / (rawWaterTarget * count)) * 100).coerceIn(0.0, 100.0)
        val fibScore = ((sumFiber / (targetFib * count)) * 100).coerceIn(0.0, 100.0)
        val compositeScore = ((calScore + protScore + waterScore + fibScore) / 4.0).toInt()

        return InsightSummary(
            averageCalories = (sumCals / count).coerceAtLeast(0.0),
            averageProtein = (sumProtein / count).coerceAtLeast(0.0),
            averageCarbs = (sumCarbs / count).coerceAtLeast(0.0),
            averageFat = (sumFat / count).coerceAtLeast(0.0),
            averageFiber = (sumFiber / count).coerceAtLeast(0.0),
            averageWaterMl = sumWater / count,
            waterGoalDaysHit = waterGoalDays,
            completionRatePercent = compositeScore,
            weeklyDays = dayStats,
            targetCalories = targetCals,
            targetProtein = targetProt,
            targetCarbs = targetCarb,
            targetFat = targetF,
            targetFiber = targetFib,
            targetWaterMl = rawWaterTarget,
            totalDays = count,
            totalCalories = sumCals,
            totalProtein = sumProtein,
            totalCarbs = sumCarbs,
            totalFat = sumFat,
            totalFiber = sumFiber,
            totalWaterMl = sumWater
        )
    }

    private fun emptyInsightSummary(target: NutritionTargetEntity?, daysCount: Int = 7): InsightSummary {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayStats = mutableListOf<WeeklyDayStat>()
        for (i in (daysCount - 1) downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(c.time)
            val dayLabel = dayFormat.format(c.time)
            dayStats.add(
                WeeklyDayStat(
                    dayLabel = dayLabel,
                    dateString = dateStr,
                    calories = 0.0,
                    protein = 0.0,
                    carbs = 0.0,
                    fat = 0.0,
                    fiber = 0.0,
                    waterMl = 0,
                    isTargetHit = false
                )
            )
        }
        val count = if (daysCount > 0) daysCount else 7
        return InsightSummary(
            averageCalories = 0.0,
            averageProtein = 0.0,
            averageCarbs = 0.0,
            averageFat = 0.0,
            averageFiber = 0.0,
            averageWaterMl = 0,
            waterGoalDaysHit = 0,
            completionRatePercent = 0,
            weeklyDays = dayStats,
            targetCalories = target?.caloriesTarget?.takeIf { it > 0.0 } ?: 2100.0,
            targetProtein = target?.proteinTarget?.takeIf { it > 0.0 } ?: 120.0,
            targetCarbs = target?.carbsTarget?.takeIf { it > 0.0 } ?: 220.0,
            targetFat = target?.fatTarget?.takeIf { it > 0.0 } ?: 50.0,
            targetFiber = target?.fiberTarget?.takeIf { it > 0.0 } ?: 28.0,
            targetWaterMl = target?.waterTargetMl?.takeIf { it > 0 } ?: 2200,
            totalDays = count,
            totalCalories = 0.0,
            totalProtein = 0.0,
            totalCarbs = 0.0,
            totalFat = 0.0,
            totalFiber = 0.0,
            totalWaterMl = 0
        )
    }
}
