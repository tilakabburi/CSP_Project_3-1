package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.NutritionTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionTargetDao {
    @Query("SELECT * FROM nutrition_targets WHERE userId = :userId LIMIT 1")
    fun getTargetFlow(userId: Long): Flow<NutritionTargetEntity?>

    @Query("SELECT * FROM nutrition_targets WHERE userId = :userId LIMIT 1")
    suspend fun getTarget(userId: Long): NutritionTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(target: NutritionTargetEntity): Long

    @Query("DELETE FROM nutrition_targets")
    suspend fun clearAllTargets()
}
