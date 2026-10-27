package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_food_plan")
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val mealType: String, // "Breakfast", "Lunch", "Snacks", "Dinner"
    val foodId: Long,
    val plannedQuantity: Double,
    val quantityUnit: String, // "piece", "g", "ml", "cup"
    val mealTime: String, // "08:00" in 24h format
    val dayType: String = "ALL", // "ALL", "WEEKDAY", "WEEKEND"
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
