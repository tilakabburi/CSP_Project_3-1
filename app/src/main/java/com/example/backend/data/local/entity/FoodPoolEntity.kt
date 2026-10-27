package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_food_pool")
data class FoodPoolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val foodId: Long,
    val isDailyRegular: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
