package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_targets")
data class NutritionTargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val caloriesTarget: Double,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val fiberTarget: Double,
    val waterTargetMl: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
