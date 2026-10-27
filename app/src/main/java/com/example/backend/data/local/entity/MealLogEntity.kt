package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: String, // "YYYY-MM-DD"
    val mealType: String, // "Breakfast", "Lunch", "Snacks", "Dinner"
    val plannedFoodId: Long,
    val actualFoodId: Long,
    val plannedQuantity: Double,
    val actualQuantity: Double,
    val quantityUnit: String,
    val status: String, // "scheduled", "consumed", "will_eat", "substituted", "missed"
    // Nutrition snapshot at logging time
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val timestamp: Long = System.currentTimeMillis()
)
