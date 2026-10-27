package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Protein", "Carbohydrates", "Fiber", "Fats"
    val referenceQuantity: Double, // e.g. 100
    val referenceUnit: String, // "g", "ml", "piece"
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val iconKey: String = "food",
    val isCustom: Boolean = false,
    val active: Boolean = true
)
