package com.example.backend.logic.model

/**
 * Domain model representing calculated macronutrients and total energy.
 */
data class CalculatedNutrition(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double
)

/**
 * Domain model representing human body composition and metabolic metrics (BMI, BMR, TDEE).
 */
data class BodyMetricsSummary(
    val bmi: Double,
    val bmiCategory: String,
    val healthyWeightMinKg: Double,
    val healthyWeightMaxKg: Double,
    val bmrCalories: Int,
    val tdeeCalories: Int,
    val targetCalories: Int,
    val activityMultiplier: Double,
    val goalAdjustmentPercent: Int
)
