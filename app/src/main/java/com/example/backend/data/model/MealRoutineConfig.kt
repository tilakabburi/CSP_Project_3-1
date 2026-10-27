package com.example.backend.data.model

/**
 * Represents a single planned food item within a meal.
 */
data class MealFoodSelection(
    val foodId: Long,
    val plannedQuantity: Double,
    val quantityUnit: String
)

/**
 * Configuration for a specific meal (Breakfast, Lunch, Snacks, Dinner).
 */
data class MealConfigItem(
    val mealType: String, // "Breakfast", "Lunch", "Snacks", "Dinner"
    val mealTime: String, // "08:00" in 24h format
    val foods: List<MealFoodSelection> = emptyList(),
    val reminderEnabled: Boolean = true
)

/**
 * Full schedule routine configuration holding weekday and weekend settings.
 */
data class RoutineScheduleConfig(
    val weekdayPlans: List<MealConfigItem>,
    val weekendPlans: List<MealConfigItem> = emptyList(),
    val separateWeekendSchedule: Boolean = false,
    val reminderLeadMinutes: Int = 30
)
