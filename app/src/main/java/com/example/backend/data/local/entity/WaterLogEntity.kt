package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: String, // "YYYY-MM-DD"
    val quantityMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
