package com.example.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String = "",
    val password: String = "",
    val biologicalSex: String = "Male",
    val age: Int = 25,
    val heightCm: Double = 170.0,
    val weightKg: Double = 65.0,
    val activityLevel: String = "Moderate", // "Low", "Moderate", "High"
    val bodyBuild: String = "Lean Muscle", // "Lean", "Athletic", "Average", "Stocky", "Lean Muscle"
    val isOnboarded: Boolean = true,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
