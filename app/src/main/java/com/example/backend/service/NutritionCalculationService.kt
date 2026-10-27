package com.example.backend.service

import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.MealLogEntity
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.logic.calculator.NutritionCalculationEngine
import com.example.backend.logic.model.BodyMetricsSummary
import com.example.backend.logic.model.CalculatedNutrition

typealias CalculatedNutrition = com.example.backend.logic.model.CalculatedNutrition
typealias BodyMetricsSummary = com.example.backend.logic.model.BodyMetricsSummary

object NutritionCalculationService {

    fun calculateBmi(weightKg: Double, heightCm: Double): Double =
        NutritionCalculationEngine.calculateBmi(weightKg, heightCm)

    fun bmiCategory(bmi: Double): String =
        NutritionCalculationEngine.bmiCategory(bmi)

    fun calculateBmr(
        age: Int,
        heightCm: Double,
        weightKg: Double,
        biologicalSex: String = "Male"
    ): Double = NutritionCalculationEngine.calculateBmr(age, heightCm, weightKg, biologicalSex)

    fun calculateBodyMetrics(
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String = "Male"
    ): BodyMetricsSummary = NutritionCalculationEngine.calculateBodyMetrics(
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        bodyBuild = bodyBuild,
        biologicalSex = biologicalSex
    )

    fun calculateTargets(
        userId: Long,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        bodyBuild: String,
        biologicalSex: String = "Male"
    ): NutritionTargetEntity = NutritionCalculationEngine.calculateTargets(
        userId = userId,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        bodyBuild = bodyBuild,
        biologicalSex = biologicalSex
    )

    fun scaleNutrition(food: FoodEntity, quantity: Double): CalculatedNutrition =
        NutritionCalculationEngine.scaleNutrition(food, quantity)

    fun scaleNutrition(food: FoodEntity, quantity: Double, unit: String): CalculatedNutrition =
        NutritionCalculationEngine.scaleNutrition(food, quantity)

    fun aggregateDailyNutrition(logs: List<MealLogEntity>): CalculatedNutrition =
        NutritionCalculationEngine.aggregateDailyNutrition(logs)
}
