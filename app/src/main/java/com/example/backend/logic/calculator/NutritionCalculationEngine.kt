package com.example.backend.logic.calculator

import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.MealLogEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.logic.model.BodyMetricsSummary
import com.example.backend.logic.model.CalculatedNutrition
import kotlin.math.roundToInt

/**
 * Domain engine for calculating Body Mass Index (BMI), Basal Metabolic Rate (BMR),
 * Total Daily Energy Expenditure (TDEE), macronutrient targets, and food nutrient scaling.
 */
object NutritionCalculationEngine {

    /**
     * Calculate Body Mass Index (BMI) = weight (kg) / (height (m))^2
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val safeHeightM = (heightCm.coerceAtLeast(100.0)) / 100.0
        val safeWeightKg = weightKg.coerceAtLeast(25.0)
        val rawBmi = safeWeightKg / (safeHeightM * safeHeightM)
        return roundToOneDecimal(rawBmi)
    }

    /**
     * Standard WHO clinical classification for adult BMI.
     */
    fun bmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal Weight"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    /**
     * Healthy weight range (kg) for a given height based on normal BMI (18.5 - 24.9).
     */
    fun healthyWeightRange(heightCm: Double): Pair<Double, Double> {
        val heightM = (heightCm.coerceAtLeast(100.0)) / 100.0
        val minKg = roundToOneDecimal(18.5 * heightM * heightM)
        val maxKg = roundToOneDecimal(24.9 * heightM * heightM)
        return Pair(minKg, maxKg)
    }

    /**
     * Mifflin-St Jeor equation for Basal Metabolic Rate (BMR):
     * Male:   10 * weight(kg) + 6.25 * height(cm) - 5 * age(y) + 5
     * Female: 10 * weight(kg) + 6.25 * height(cm) - 5 * age(y) - 161
     */
    fun calculateBmr(
        age: Int,
        heightCm: Double,
        weightKg: Double,
        biologicalSex: String = "Male"
    ): Double {
        val safeAge = age.coerceIn(10, 110)
        val safeHeight = heightCm.coerceIn(100.0, 250.0)
        val safeWeight = weightKg.coerceIn(25.0, 300.0)
        val isFemale = biologicalSex.trim().equals("Female", ignoreCase = true)
        val sexOffset = if (isFemale) -161.0 else 5.0
        val rawBmr = (10.0 * safeWeight) + (6.25 * safeHeight) - (5.0 * safeAge) + sexOffset
        return rawBmr.coerceAtLeast(1000.0)
    }

    /**
     * Physical Activity Level (PAL) multiplier mapping all onboarding & profile options.
     */
    fun getActivityMultiplier(activityLevel: String): Double {
        return when (activityLevel.trim().lowercase()) {
            "sedentary", "low" -> 1.20
            "lightly active", "light" -> 1.375
            "moderate", "moderately active" -> 1.50
            "high", "very active", "active" -> 1.725
            "extra active", "athlete" -> 1.90
            else -> 1.375
        }
    }

    /**
     * Caloric adjustment multiplier for body build / fitness goal.
     */
    fun getGoalBuildMultiplier(bodyBuild: String): Double {
        return when (bodyBuild.trim().lowercase()) {
            "fat loss", "weight loss", "cutting" -> 0.82      // ~18% deficit for sustainable fat loss
            "stocky" -> 0.94                                  // ~6% recomposition reduction
            "lean" -> 1.00                                    // Efficient baseline maintenance
            "average", "maintenance", "balanced" -> 1.00      // Full TDEE maintenance
            "athletic" -> 1.05                                // +5% for higher lean muscle mass turnover
            "lean muscle", "muscle growth" -> 1.08            // +8% controlled lean surplus
            "bulking" -> 1.12                                 // +12% hypertrophy surplus
            else -> 1.00
        }
    }

    /**
     * Compute complete BMI, BMR, TDEE, and target calorie summary.
     */
    fun calculateBodyMetrics(
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String = "Male"
    ): BodyMetricsSummary {
        val bmi = calculateBmi(weightKg, heightCm)
        val category = bmiCategory(bmi)
        val (minW, maxW) = healthyWeightRange(heightCm)
        val bmr = calculateBmr(age, heightCm, weightKg, biologicalSex)
        val activityMult = getActivityMultiplier(activityLevel)
        val tdee = bmr * activityMult
        val goalMult = getGoalBuildMultiplier(bodyBuild)
        val isFemale = biologicalSex.trim().equals("Female", ignoreCase = true)
        val minSafeCalories = if (isFemale) 1200.0 else 1400.0
        val targetCals = (tdee * goalMult).coerceAtLeast(minSafeCalories)

        return BodyMetricsSummary(
            bmi = bmi,
            bmiCategory = category,
            healthyWeightMinKg = minW,
            healthyWeightMaxKg = maxW,
            bmrCalories = bmr.roundToInt(),
            tdeeCalories = tdee.roundToInt(),
            targetCalories = targetCals.roundToInt(),
            activityMultiplier = activityMult,
            goalAdjustmentPercent = ((goalMult - 1.0) * 100.0).roundToInt()
        )
    }

    /**
     * Calculate Daily BMR, TDEE, and Macronutrient Targets based on personal biometrics,
     * activity level, and body build/goal.
     */
    fun calculateTargets(
        userId: Long,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String = "Male"
    ): NutritionTargetEntity {
        val metrics = calculateBodyMetrics(
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            activityLevel = activityLevel,
            bodyBuild = bodyBuild,
            biologicalSex = biologicalSex
        )

        val dailyCalories = metrics.targetCalories.toDouble()
        val actNorm = activityLevel.trim().lowercase()
        val buildNorm = bodyBuild.trim().lowercase()

        // Effective reference weight for protein calculation:
        // For individuals with BMI > 27 (non-athletic), use Adjusted Body Weight (ABW)
        // so adipose tissue does not artificially inflate protein targets.
        val heightM = (heightCm.coerceAtLeast(100.0)) / 100.0
        val weightAtBmi25 = 25.0 * heightM * heightM
        val isAthleticBuild = buildNorm == "athletic" || buildNorm == "lean muscle" || buildNorm == "bulking"
        val effectiveWeightForProtein = if (!isAthleticBuild && metrics.bmi > 27.0 && weightKg > weightAtBmi25) {
            weightAtBmi25 + 0.4 * (weightKg - weightAtBmi25)
        } else {
            weightKg.coerceAtLeast(30.0)
        }

        // Evidence-based protein requirement (g per kg of effective weight):
        // Higher protein is prescribed for high activity, muscle gain, and fat-loss deficits (to preserve lean mass).
        val baseProteinPerKg = when (actNorm) {
            "high", "very active", "active", "extra active", "athlete" -> 1.8
            "moderate", "moderately active" -> 1.6
            "lightly active", "light" -> 1.4
            else -> 1.2
        }
        val goalProteinPerKg = when (buildNorm) {
            "fat loss", "weight loss", "cutting" -> 1.8
            "athletic", "lean muscle", "muscle growth", "bulking" -> 1.8
            "stocky" -> 1.6
            else -> baseProteinPerKg
        }
        val proteinPerKg = maxOf(baseProteinPerKg, goalProteinPerKg)

        // Clamp protein between 15% and 35% of total daily calories (4 kcal/g)
        val rawProteinGrams = effectiveWeightForProtein * proteinPerKg
        val minProteinGrams = (dailyCalories * 0.15) / 4.0
        val maxProteinGrams = (dailyCalories * 0.35) / 4.0
        val proteinGrams = rawProteinGrams.coerceIn(minProteinGrams, maxProteinGrams).roundToInt().toDouble()

        // Fat: 25% of daily calories (22% for fat loss to spare carbs/protein), 9 kcal/g, min 0.6g/kg
        val fatShare = if (buildNorm == "fat loss" || buildNorm == "stocky") 0.23 else 0.26
        val minFatGrams = effectiveWeightForProtein * 0.6
        val fatGrams = ((dailyCalories * fatShare) / 9.0).coerceAtLeast(minFatGrams).roundToInt().toDouble()

        // Fiber: 14g per 1000 kcal (IOM standard), clamped to 22g - 42g daily
        val fiberGrams = ((dailyCalories / 1000.0) * 14.0).coerceIn(20.0, 42.0).roundToInt().toDouble()

        // Carbs: Remaining energy budget (4 kcal/g), minimum 75g for brain/CNS glucose needs
        val remainingCaloriesForCarbs = (dailyCalories - (proteinGrams * 4.0 + fatGrams * 9.0)).coerceAtLeast(300.0)
        val carbsGrams = (remainingCaloriesForCarbs / 4.0).roundToInt().toDouble()

        // Water: 35 ml per kg of body weight + activity sweat compensation, rounded to nearest 100 ml
        val activityWaterBonusMl = when (actNorm) {
            "high", "very active", "active", "extra active", "athlete" -> 500.0
            "moderate", "moderately active" -> 250.0
            "lightly active", "light" -> 100.0
            else -> 0.0
        }
        val rawWaterMl = (weightKg.coerceAtLeast(30.0) * 35.0) + activityWaterBonusMl
        val waterMl = ((rawWaterMl / 100.0).roundToInt() * 100).coerceIn(1800, 4500)

        return NutritionTargetEntity(
            userId = userId,
            caloriesTarget = dailyCalories,
            proteinTarget = proteinGrams,
            carbsTarget = carbsGrams,
            fatTarget = fatGrams,
            fiberTarget = fiberGrams,
            waterTargetMl = waterMl
        )
    }

    /**
     * Complete Nutrition Scaling for a food item and chosen quantity.
     */
    fun scaleNutrition(food: FoodEntity, quantity: Double): CalculatedNutrition {
        val ref = if (food.referenceQuantity > 0) food.referenceQuantity else 100.0
        val factor = quantity / ref
        return CalculatedNutrition(
            calories = roundToOneDecimal(food.calories * factor),
            protein = roundToOneDecimal(food.protein * factor),
            carbs = roundToOneDecimal(food.carbs * factor),
            fat = roundToOneDecimal(food.fat * factor),
            fiber = roundToOneDecimal(food.fiber * factor)
        )
    }

    /**
     * Aggregates valid actual consumed items for the day.
     * Strictly sums items where status == "consumed" to prevent double-counting.
     */
    fun aggregateDailyNutrition(logs: List<MealLogEntity>): CalculatedNutrition {
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFiber = 0.0

        for (log in logs) {
            if (log.status.equals("consumed", ignoreCase = true)) {
                totalCalories += log.calories
                totalProtein += log.protein
                totalCarbs += log.carbs
                totalFat += log.fat
                totalFiber += log.fiber
            }
        }

        return CalculatedNutrition(
            calories = roundToOneDecimal(totalCalories),
            protein = roundToOneDecimal(totalProtein),
            carbs = roundToOneDecimal(totalCarbs),
            fat = roundToOneDecimal(totalFat),
            fiber = roundToOneDecimal(totalFiber)
        )
    }

    private fun roundToOneDecimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }
}
