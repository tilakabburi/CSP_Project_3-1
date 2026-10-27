package com.example.backend.data.repository

import androidx.room.withTransaction
import com.example.backend.data.DatasetFoods
import com.example.backend.data.local.AppDatabase
import com.example.backend.data.local.entity.*
import com.example.backend.data.model.*
import com.example.backend.service.NutritionCalculationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NutriPathRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val targetDao = db.nutritionTargetDao()
    private val foodDao = db.foodDao()
    private val foodPoolDao = db.foodPoolDao()
    private val planDao = db.dailyPlanDao()
    private val logDao = db.mealLogDao()
    private val waterDao = db.waterLogDao()

    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val allFoodsFlow: Flow<List<FoodEntity>> = foodDao.getAllFoodsFlow()

    fun getTargetsFlow(userId: Long): Flow<NutritionTargetEntity?> = targetDao.getTargetFlow(userId)
    fun getDailyPlanFlow(userId: Long): Flow<List<DailyPlanEntity>> = planDao.getDailyPlanFlow(userId)
    fun getFoodPoolFlow(userId: Long): Flow<List<FoodPoolEntity>> = foodPoolDao.getPoolFlow(userId)
    fun getLogsForDateFlow(userId: Long, date: String): Flow<List<MealLogEntity>> = logDao.getLogsForDateFlow(userId, date)
    fun getWaterLogsForDateFlow(userId: Long, date: String): Flow<List<WaterLogEntity>> = waterDao.getWaterLogsForDateFlow(userId, date)
    fun getLogsForRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<MealLogEntity>> =
        logDao.getLogsForRangeFlow(userId, startDate, endDate)
    fun getWaterForRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<WaterLogEntity>> =
        waterDao.getWaterLogsForRangeFlow(userId, startDate, endDate)

    suspend fun getUser(): UserEntity? = withContext(Dispatchers.IO) { userDao.getUser() }
    suspend fun getFoodById(id: Long): FoodEntity? = withContext(Dispatchers.IO) { foodDao.getFoodById(id) }
    suspend fun getAllFoods(): List<FoodEntity> = withContext(Dispatchers.IO) { foodDao.getAllFoods() }
    suspend fun getDailyPlans(userId: Long): List<DailyPlanEntity> = withContext(Dispatchers.IO) { planDao.getDailyPlans(userId) }
    suspend fun getLogsForDate(userId: Long, date: String): List<MealLogEntity> = withContext(Dispatchers.IO) { logDao.getLogsForDate(userId, date) }
    suspend fun getLogsForRange(userId: Long, startDate: String, endDate: String): List<MealLogEntity> = withContext(Dispatchers.IO) { logDao.getLogsForRange(userId, startDate, endDate) }
    suspend fun getTarget(userId: Long): NutritionTargetEntity? = withContext(Dispatchers.IO) { targetDao.getTarget(userId) }
    suspend fun getWaterLogsForDate(userId: Long, date: String): List<WaterLogEntity> = withContext(Dispatchers.IO) { waterDao.getWaterLogsForDate(userId, date) }
    suspend fun getWaterLogsForRange(userId: Long, startDate: String, endDate: String): List<WaterLogEntity> = withContext(Dispatchers.IO) { waterDao.getWaterLogsForRange(userId, startDate, endDate) }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(Exception("No account found with this email"))

        if (user.password != password) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }

        userDao.logoutAll()
        val activeUser = user.copy(isLoggedIn = true)
        userDao.updateUser(activeUser)
        Result.success(activeUser)
    }

    suspend fun register(name: String, email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }

        userDao.logoutAll()
        val newUser = UserEntity(
            name = name.trim().ifBlank { "User" },
            email = trimmedEmail,
            password = password,
            isOnboarded = false,
            isLoggedIn = true
        )
        val newId = userDao.insertUser(newUser)
        val insertedUser = newUser.copy(id = newId)
        Result.success(insertedUser)
    }

    suspend fun logout(): Unit = withContext(Dispatchers.IO) {
        userDao.logoutAll()
    }

    suspend fun completeProfileSetup(
        userId: Long,
        name: String,
        age: Int,
        biologicalSex: String,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        selectedFoodIds: List<Long>,
        routineConfig: RoutineScheduleConfig? = null
    ): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val existing = userDao.getUser() ?: userDao.getAllUsers().firstOrNull { it.id == userId }
            val user = if (existing != null) {
                existing.copy(
                    name = name.trim().ifBlank { existing.name },
                    age = age,
                    biologicalSex = biologicalSex,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    activityLevel = activityLevel,
                    bodyBuild = bodyBuild,
                    isOnboarded = true,
                    isLoggedIn = true
                ).also { userDao.updateUser(it) }
            } else {
                val newUser = UserEntity(
                    id = userId,
                    name = name,
                    age = age,
                    biologicalSex = biologicalSex,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    activityLevel = activityLevel,
                    bodyBuild = bodyBuild,
                    isOnboarded = true,
                    isLoggedIn = true
                )
                val newId = userDao.insertUser(newUser)
                newUser.copy(id = newId)
            }

            val target = NutritionCalculationService.calculateTargets(
                userId = user.id,
                age = user.age,
                heightCm = user.heightCm,
                weightKg = user.weightKg,
                activityLevel = user.activityLevel,
                bodyBuild = user.bodyBuild,
                biologicalSex = user.biologicalSex
            )
            targetDao.insertOrUpdate(target)

            foodPoolDao.clearPoolForUser(user.id)
            val poolEntries = selectedFoodIds.distinct().map { foodId ->
                FoodPoolEntity(userId = user.id, foodId = foodId, isDailyRegular = true)
            }
            if (poolEntries.isNotEmpty()) {
                foodPoolDao.insertAllToPool(poolEntries)
            }

            if (routineConfig != null) {
                saveCustomMealRoutine(user.id, routineConfig)
            } else {
                val existingPlans = planDao.getDailyPlans(user.id)
                if (existingPlans.isEmpty() && selectedFoodIds.isNotEmpty()) {
                    val foods = foodDao.getFoodsByIds(selectedFoodIds)
                    val breakfastFood = foods.firstOrNull { it.category == "Protein" } ?: foods.firstOrNull()
                    val lunchFood = foods.firstOrNull { it.category == "Carbohydrates" } ?: foods.getOrNull(1) ?: breakfastFood
                    val dinnerFood = foods.firstOrNull { it.category == "Fiber" || it.category == "Protein" } ?: foods.lastOrNull()

                    breakfastFood?.let {
                        planDao.insertPlan(DailyPlanEntity(userId = user.id, mealType = "Breakfast", foodId = it.id, plannedQuantity = it.referenceQuantity, quantityUnit = it.referenceUnit, mealTime = "08:00"))
                    }
                    lunchFood?.let {
                        planDao.insertPlan(DailyPlanEntity(userId = user.id, mealType = "Lunch", foodId = it.id, plannedQuantity = it.referenceQuantity, quantityUnit = it.referenceUnit, mealTime = "13:00"))
                    }
                    dinnerFood?.let {
                        planDao.insertPlan(DailyPlanEntity(userId = user.id, mealType = "Dinner", foodId = it.id, plannedQuantity = it.referenceQuantity, quantityUnit = it.referenceUnit, mealTime = "20:00"))
                    }
                }
                ensureTodayMealLogs(user.id)
            }

            user.id
        }
    }

    suspend fun saveUserAndCalculateTargets(
        name: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val existing = userDao.getUser()
        val effectiveSex = biologicalSex ?: existing?.biologicalSex ?: "Male"
        val user = if (existing != null) {
            existing.copy(
                name = name,
                age = age,
                biologicalSex = effectiveSex,
                heightCm = heightCm,
                weightKg = weightKg,
                activityLevel = activityLevel,
                bodyBuild = bodyBuild,
                isOnboarded = true,
                isLoggedIn = true
            ).also { userDao.updateUser(it) }
        } else {
            val newUser = UserEntity(
                name = name,
                age = age,
                biologicalSex = effectiveSex,
                heightCm = heightCm,
                weightKg = weightKg,
                activityLevel = activityLevel,
                bodyBuild = bodyBuild,
                isOnboarded = true,
                isLoggedIn = true
            )
            val newId = userDao.insertUser(newUser)
            newUser.copy(id = newId)
        }

        val target = NutritionCalculationService.calculateTargets(
            userId = user.id,
            age = user.age,
            heightCm = user.heightCm,
            weightKg = user.weightKg,
            activityLevel = user.activityLevel,
            bodyBuild = user.bodyBuild,
            biologicalSex = user.biologicalSex
        )
        targetDao.insertOrUpdate(target)
        user.id
    }

    suspend fun updateNutritionTargets(target: NutritionTargetEntity) = withContext(Dispatchers.IO) {
        targetDao.insertOrUpdate(target)
    }

    suspend fun addFoodToPool(userId: Long, foodId: Long) = withContext(Dispatchers.IO) {
        val existing = foodPoolDao.getPool(userId)
        if (existing.none { it.foodId == foodId }) {
            foodPoolDao.insertToPool(FoodPoolEntity(userId = userId, foodId = foodId))
        }
    }

    suspend fun removeFoodFromPool(userId: Long, foodId: Long) = withContext(Dispatchers.IO) {
        foodPoolDao.removeFromPool(userId, foodId)
    }

    suspend fun clearFoodPoolForUser(userId: Long) = withContext(Dispatchers.IO) {
        foodPoolDao.clearPoolForUser(userId)
    }

    suspend fun createCustomFood(
        userId: Long,
        name: String,
        category: String,
        referenceQuantity: Double,
        referenceUnit: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        iconKey: String = "custom"
    ): Long = withContext(Dispatchers.IO) {
        val food = FoodEntity(
            name = name,
            category = category,
            referenceQuantity = referenceQuantity,
            referenceUnit = referenceUnit,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            iconKey = iconKey,
            isCustom = true
        )
        val foodId = foodDao.insertFood(food)
        foodPoolDao.insertToPool(FoodPoolEntity(userId = userId, foodId = foodId, isDailyRegular = false))
        foodId
    }

    suspend fun resetAppToBeginning(): Unit = withContext(Dispatchers.IO) {
        foodPoolDao.clearAllFoodPool()
        planDao.clearAllPlans()
        logDao.clearAllLogs()
        waterDao.clearAllWaterLogs()
        targetDao.clearAllTargets()
        userDao.deleteAllUsers()
    }

    suspend fun clearAllMealLogsAndSelections(userId: Long): Unit = withContext(Dispatchers.IO) {
        foodPoolDao.clearAllFoodPool()
        planDao.clearAllPlans()
        logDao.clearAllLogs()
        waterDao.clearAllWaterLogs()
    }

    suspend fun clearMealAndWaterHistoryOnly(userId: Long): Unit = withContext(Dispatchers.IO) {
        logDao.clearAllLogs()
        waterDao.clearAllWaterLogs()
        ensureTodayMealLogs(userId)
    }

    suspend fun saveDailyPlan(
        userId: Long,
        mealType: String,
        foodId: Long,
        quantity: Double,
        unit: String,
        mealTime: String
    ) = withContext(Dispatchers.IO) {
        val plan = DailyPlanEntity(
            userId = userId,
            mealType = mealType,
            foodId = foodId,
            plannedQuantity = quantity,
            quantityUnit = unit,
            mealTime = mealTime,
            active = true
        )
        planDao.insertPlan(plan)
    }

    suspend fun deleteDailyPlan(planId: Long) = withContext(Dispatchers.IO) {
        planDao.deletePlanById(planId)
    }

    fun isWeekendDate(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = sdf.parse(date) ?: return false
            val cal = Calendar.getInstance().apply { time = parsed }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveCustomMealRoutine(
        userId: Long,
        routineConfig: RoutineScheduleConfig
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            planDao.deletePlansByUserId(userId)
            logDao.deleteAllScheduledLogsForUser(userId)

            val dayTypeForWeekday = if (routineConfig.separateWeekendSchedule) "WEEKDAY" else "ALL"
            for (mealConfig in routineConfig.weekdayPlans) {
                for (foodItem in mealConfig.foods) {
                    planDao.insertPlan(
                        DailyPlanEntity(
                            userId = userId,
                            mealType = mealConfig.mealType,
                            foodId = foodItem.foodId,
                            plannedQuantity = foodItem.plannedQuantity,
                            quantityUnit = foodItem.quantityUnit,
                            mealTime = mealConfig.mealTime,
                            dayType = dayTypeForWeekday,
                            active = true
                        )
                    )
                }
            }

            if (routineConfig.separateWeekendSchedule) {
                for (mealConfig in routineConfig.weekendPlans) {
                    for (foodItem in mealConfig.foods) {
                        planDao.insertPlan(
                            DailyPlanEntity(
                                userId = userId,
                                mealType = mealConfig.mealType,
                                foodId = foodItem.foodId,
                                plannedQuantity = foodItem.plannedQuantity,
                                quantityUnit = foodItem.quantityUnit,
                                mealTime = mealConfig.mealTime,
                                dayType = "WEEKEND",
                                active = true
                            )
                        )
                    }
                }
            }
            ensureTodayMealLogs(userId)
        }
    }

    suspend fun ensureTodayMealLogs(userId: Long, date: String = getTodayDateString()) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val isWeekend = isWeekendDate(date)
            val plans = planDao.getDailyPlans(userId)
            val activePlans = plans.filter { plan ->
                plan.active && (
                    plan.dayType == "ALL" ||
                    (isWeekend && plan.dayType == "WEEKEND") ||
                    (!isWeekend && plan.dayType == "WEEKDAY")
                )
            }
            val activePlanKeys = activePlans.map { "${it.mealType.lowercase()}_${it.foodId}" }.toSet()

            val existingLogs = logDao.getLogsForDate(userId, date)

            // 1. Clean up any unacted 'scheduled' logs that do not belong to active plans for this day type
            val staleScheduledLogs = existingLogs.filter {
                it.status == "scheduled" && it.plannedFoodId > 0L &&
                "${it.mealType.lowercase()}_${it.plannedFoodId}" !in activePlanKeys
            }
            for (stale in staleScheduledLogs) {
                logDao.deleteLogById(stale.id)
            }

            // 2. Clean up any duplicate 'scheduled' logs for the same mealType and plannedFoodId
            val scheduledGrouped = existingLogs.filter { it.status == "scheduled" && it.plannedFoodId > 0L }
                .groupBy { "${it.mealType.lowercase()}_${it.plannedFoodId}" }
            for ((_, group) in scheduledGrouped) {
                if (group.size > 1) {
                    val duplicates = group.drop(1)
                    for (dup in duplicates) {
                        logDao.deleteLogById(dup.id)
                    }
                }
            }

            // 3. Re-fetch clean logs for exact verification
            val cleanLogs = logDao.getLogsForDate(userId, date)

            for (plan in activePlans) {
                val exists = cleanLogs.any {
                    it.mealType.equals(plan.mealType, ignoreCase = true) && it.plannedFoodId == plan.foodId
                }
                if (!exists) {
                    val food = foodDao.getFoodById(plan.foodId)
                    if (food != null) {
                        val nutrition = NutritionCalculationService.scaleNutrition(food, plan.plannedQuantity, plan.quantityUnit)
                        logDao.insertLog(
                            MealLogEntity(
                                userId = userId,
                                date = date,
                                mealType = plan.mealType,
                                plannedFoodId = plan.foodId,
                                actualFoodId = plan.foodId,
                                plannedQuantity = plan.plannedQuantity,
                                actualQuantity = plan.plannedQuantity,
                                quantityUnit = plan.quantityUnit,
                                calories = nutrition.calories,
                                protein = nutrition.protein,
                                carbs = nutrition.carbs,
                                fat = nutrition.fat,
                                fiber = nutrition.fiber,
                                status = "scheduled",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun markLogConsumed(logId: Long) = withContext(Dispatchers.IO) {
        val log = logDao.getLogById(logId) ?: return@withContext
        val food = foodDao.getFoodById(log.actualFoodId) ?: foodDao.getFoodById(log.plannedFoodId) ?: return@withContext
        val nutrition = NutritionCalculationService.scaleNutrition(food, log.actualQuantity, log.quantityUnit)
        val updated = log.copy(
            status = "consumed",
            calories = nutrition.calories,
            protein = nutrition.protein,
            carbs = nutrition.carbs,
            fat = nutrition.fat,
            fiber = nutrition.fiber,
            timestamp = System.currentTimeMillis()
        )
        logDao.updateLog(updated)
    }

    suspend fun markLogWillEat(logId: Long) = withContext(Dispatchers.IO) {
        val log = logDao.getLogById(logId) ?: return@withContext
        val updated = log.copy(
            status = "will_eat",
            timestamp = System.currentTimeMillis()
        )
        logDao.updateLog(updated)
    }

    suspend fun substituteFood(
        logId: Long,
        replacementFoodId: Long,
        replacementQuantity: Double,
        replacementUnit: String
    ): Long = withContext(Dispatchers.IO) {
        val originalLog = logDao.getLogById(logId) ?: return@withContext -1L
        val replacementFood = foodDao.getFoodById(replacementFoodId) ?: return@withContext -1L

        addFoodToPool(originalLog.userId, replacementFoodId)

        val nutrition = NutritionCalculationService.scaleNutrition(
            food = replacementFood,
            quantity = replacementQuantity,
            unit = replacementUnit
        )

        val effectivePlannedFoodId = if (originalLog.plannedFoodId > 0L) originalLog.plannedFoodId else originalLog.actualFoodId
        val effectivePlannedQty = if (originalLog.plannedQuantity > 0.0) originalLog.plannedQuantity else originalLog.actualQuantity

        // Step A (Original Item): Update the existing log entry for the planned food by setting its status to "substituted". Do NOT delete this record.
        val updatedOriginal = originalLog.copy(
            plannedFoodId = effectivePlannedFoodId,
            plannedQuantity = effectivePlannedQty,
            status = "substituted",
            timestamp = System.currentTimeMillis()
        )
        logDao.updateLog(updatedOriginal)

        // Step B (Replacement Item): Insert a new log entry for the replacement food under the same meal category and date with status set to "consumed", referencing the actual food ID and its scaled macros.
        val replacementLog = MealLogEntity(
            userId = originalLog.userId,
            date = originalLog.date,
            mealType = originalLog.mealType,
            plannedFoodId = effectivePlannedFoodId,
            actualFoodId = replacementFoodId,
            plannedQuantity = effectivePlannedQty,
            actualQuantity = replacementQuantity,
            quantityUnit = replacementUnit,
            status = "consumed",
            calories = nutrition.calories,
            protein = nutrition.protein,
            carbs = nutrition.carbs,
            fat = nutrition.fat,
            fiber = nutrition.fiber,
            timestamp = System.currentTimeMillis() + 1
        )
        val newLogId = logDao.insertLog(replacementLog)
        newLogId
    }

    suspend fun restoreOriginalFood(logId: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val log = logDao.getLogById(logId) ?: return@withTransaction
            val originalFood = foodDao.getFoodById(log.plannedFoodId) ?: return@withTransaction
            val nutrition = NutritionCalculationService.scaleNutrition(
                food = originalFood,
                quantity = log.plannedQuantity,
                unit = log.quantityUnit
            )

            // Remove any replacement logs associated with this substituted plan item
            val logsOnDate = logDao.getLogsForDate(log.userId, log.date)
            val replacementLogs = logsOnDate.filter {
                it.id != log.id &&
                it.plannedFoodId == log.plannedFoodId &&
                it.mealType.equals(log.mealType, ignoreCase = true) &&
                it.status == "consumed"
            }
            for (repl in replacementLogs) {
                logDao.deleteLogById(repl.id)
            }

            val updated = log.copy(
                actualFoodId = log.plannedFoodId,
                actualQuantity = log.plannedQuantity,
                calories = nutrition.calories,
                protein = nutrition.protein,
                carbs = nutrition.carbs,
                fat = nutrition.fat,
                fiber = nutrition.fiber,
                status = "scheduled",
                timestamp = System.currentTimeMillis()
            )
            logDao.updateLog(updated)
        }
    }

    suspend fun logSubstituteFood(
        userId: Long,
        date: String = getTodayDateString(),
        mealType: String = "Breakfast",
        foodId: Long = 0L,
        quantity: Double = 1.0,
        unit: String = "serving",
        mealCategory: String = mealType,
        replacementFoodId: Long = foodId,
        replacementQuantity: Double = quantity,
        replacementUnit: String = unit,
        status: String = "consumed"
    ): Long = withContext(Dispatchers.IO) {
        val effectiveFoodId = if (replacementFoodId != 0L) replacementFoodId else foodId
        val effectiveQty = if (replacementQuantity > 0) replacementQuantity else quantity
        val effectiveUnit = replacementUnit.ifBlank { unit }
        val effectiveCategory = mealCategory.ifBlank { mealType }

        val food = foodDao.getFoodById(effectiveFoodId) ?: return@withContext -1L
        addFoodToPool(userId, effectiveFoodId)

        val nutrition = NutritionCalculationService.scaleNutrition(food, effectiveQty, effectiveUnit)
        val log = MealLogEntity(
            userId = userId,
            date = date,
            mealType = effectiveCategory,
            plannedFoodId = 0L,
            actualFoodId = effectiveFoodId,
            plannedQuantity = effectiveQty,
            actualQuantity = effectiveQty,
            quantityUnit = effectiveUnit,
            calories = nutrition.calories,
            protein = nutrition.protein,
            carbs = nutrition.carbs,
            fat = nutrition.fat,
            fiber = nutrition.fiber,
            status = status,
            timestamp = System.currentTimeMillis()
        )
        logDao.insertLog(log)
    }

    suspend fun logExtraFood(
        userId: Long,
        date: String = getTodayDateString(),
        mealType: String = "Breakfast",
        foodId: Long = 0L,
        quantity: Double = 1.0,
        unit: String = "serving"
    ): Long = withContext(Dispatchers.IO) {
        logSubstituteFood(userId = userId, date = date, mealType = mealType, foodId = foodId, quantity = quantity, unit = unit)
    }

    suspend fun deleteMealLog(logId: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val logToDelete = logDao.getLogById(logId)
            if (logToDelete != null) {
                logDao.deleteLogById(logId)
                if (logToDelete.plannedFoodId > 0L) {
                    val logsOnDate = logDao.getLogsForDate(logToDelete.userId, logToDelete.date)
                    val originalSubstituted = logsOnDate.firstOrNull {
                        it.plannedFoodId == logToDelete.plannedFoodId &&
                        it.mealType.equals(logToDelete.mealType, ignoreCase = true) &&
                        it.status == "substituted"
                    }
                    if (originalSubstituted != null) {
                        restoreOriginalFood(originalSubstituted.id)
                    }
                }
            }
        }
    }

    suspend fun logWater(userId: Long, amountMl: Int, date: String = getTodayDateString()) = withContext(Dispatchers.IO) {
        val waterLog = WaterLogEntity(
            userId = userId,
            date = date,
            quantityMl = amountMl,
            timestamp = System.currentTimeMillis()
        )
        waterDao.insertWaterLog(waterLog)
    }

    suspend fun calculateCurrentStreak(userId: Long): Int = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        var streak = 0

        for (i in 0..30) {
            val dateStr = sdf.format(cal.time)
            val logs = logDao.getLogsForDate(userId, dateStr)
            val waterLogs = waterDao.getWaterLogsForDate(userId, dateStr)
            val consumedMeals = logs.count { it.status == "consumed" }
            val totalWater = waterLogs.sumOf { it.quantityMl }

            if (consumedMeals > 0 || totalWater >= 500) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else if (i == 0) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        streak.coerceAtLeast(1)
    }

    suspend fun syncAllFoodsToUserPool(userId: Long) = withContext(Dispatchers.IO) {
        // No-op
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val oldPresetNames = setOf(
            "apple betty", "bean soup", "blueberries", "bran flakes", "bread pudding", "bread, cracked wheat",
            "brown sugar, firm-packed, dark", "cakes", "candied sweet potatoes", "cane syrup", "cherries", "cherry pie",
            "chocolate fudge", "clam chowder", "cocoa", "corn bread, ground meal", "corn meal", "cornflakes",
            "cornstarch", "crackers", "cranberry sauce, sweetened", "cupcake", "farina, cooked", "figs, canned with syrup",
            "figs, fresh", "fruit cake", "fruit cocktail, canned", "fudge", "gelatin, made with water", "gingerbread",
            "grapes, european (muscat/tokay)", "ice milk", "ices (water ice)", "jellies", "kale", "lemon meringue pie",
            "lemonade concentrate, frozen", "lentils, dry", "lima beans, dry, cooked", "macaroni, cooked", "molasses",
            "muffins", "navy beans, baked with pork", "noodles, cooked", "pancakes, 4\" diam.", "parsnips", "pears",
            "persimmons", "pizza, 14\" diam.", "plain cake, no icing", "popcorn, salted", "potatoes, baked",
            "potatoes, mashed with milk and butter", "preserves (jam)", "prune juice", "prunes", "puddings, sugar",
            "puffed rice", "puffed wheat presweetened", "raisins", "red kidney beans, cooked", "rhubarb, sweetened",
            "rolls", "rye flour", "shredded wheat biscuit", "skim milk, instant", "spaghetti with meat sauce",
            "spanish rice", "split cooked peas", "split-pea soup", "sponge cake", "steamed potatoes before peeling",
            "syrup", "table blends sugar", "tapioca cream pudding", "tomato catsup", "wheat germ",
            "wheat meal cereal, unrefined", "wheat pancakes, 4\" diam.", "wheat, whole grain", "wheat-germ cereal, toasted",
            "whole-wheat bread", "whole-wheat flour", "bacon", "brazil nuts", "cheddar cheese", "cheese, generic",
            "corn oil", "corned beef", "corned beef hash, canned", "cream cheese", "cream or half-and-half",
            "duck, domestic", "eggs, scrambled or fried", "evaporated milk, undiluted", "fortified milk",
            "french dressing", "fried chicken (breast/leg/thigh)", "ham, canned, spiced", "ham, pan-broiled",
            "hamburger", "hydrogenated cooking fat", "lamb chop, broiled", "lard", "margarine", "mayonnaise",
            "mince (raw meat)", "olive oil", "olives, large (green)", "olives, ripe", "oyster stew",
            "peanut butter, natural", "pecans", "pork roast", "pork sausage", "pork shoulder, braised",
            "potato chips", "powdered milk, whole", "processed cheese", "rice polish/bran", "roquefort cheese",
            "safflower seed oil", "salmon", "salt pork", "sesame seeds", "shad", "steak, generic", "swiss cheese",
            "thousand island sauce", "whipping cream", "beet greens", "collards, cooked", "dandelion greens",
            "eggplant", "endive", "kohlrabi", "mustard greens, cooked", "okra", "parsley", "sauerkraut",
            "turnip greens, cooked", "chicken, roasted", "cod", "fish sticks, fried", "flounder",
            "ground beef, lean", "haddock", "halibut", "herring", "lobster", "oysters", "sardines", "scallops",
            "shrimp", "steak, lean, as round", "swordfish", "turkey", "veal", "waffles"
        )

        val existingFoods = foodDao.getAllFoods()

        // Purge only legacy preset foods from the previous dataset
        val stalePresetFoods = existingFoods.filter { !it.isCustom && it.name.lowercase().trim() in oldPresetNames }
        if (stalePresetFoods.isNotEmpty()) {
            foodDao.deleteFoods(stalePresetFoods)
        }

        val remainingFoods = foodDao.getAllFoods()
        val remainingNames = remainingFoods.map { it.name.lowercase().trim() }.toSet()
        val missingFoods = DatasetFoods.allDatasetFoods.filter {
            it.name.lowercase().trim() !in remainingNames
        }
        if (missingFoods.isNotEmpty()) {
            foodDao.insertFoods(missingFoods)
        }

        val existingUsers = userDao.getAllUsers()
        if (existingUsers.isEmpty()) {
            val demoUser = UserEntity(
                name = "Demo User",
                email = "demo@nutripath.com",
                password = "demo",
                biologicalSex = "Male",
                age = 25,
                heightCm = 175.0,
                weightKg = 70.0,
                activityLevel = "Moderate",
                bodyBuild = "Lean Muscle",
                isOnboarded = true,
                isLoggedIn = false
            )
            val demoId = userDao.insertUser(demoUser)
            val target = NutritionCalculationService.calculateTargets(
                userId = demoId,
                age = 25,
                heightCm = 175.0,
                weightKg = 70.0,
                activityLevel = "Moderate",
                bodyBuild = "Lean Muscle",
                biologicalSex = "Male"
            )
            targetDao.insertOrUpdate(target)
        }
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
