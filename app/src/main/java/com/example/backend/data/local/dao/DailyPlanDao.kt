package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.DailyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_food_plan WHERE userId = :userId AND active = 1 ORDER BY mealTime ASC")
    fun getDailyPlanFlow(userId: Long): Flow<List<DailyPlanEntity>>

    @Query("SELECT * FROM daily_food_plan WHERE userId = :userId AND active = 1 ORDER BY mealTime ASC")
    suspend fun getDailyPlans(userId: Long): List<DailyPlanEntity>

    @Query("SELECT * FROM daily_food_plan WHERE userId = :userId AND (dayType = :dayType OR dayType = 'ALL') AND active = 1 ORDER BY mealTime ASC")
    suspend fun getDailyPlansForDayType(userId: Long, dayType: String): List<DailyPlanEntity>

    @Query("SELECT * FROM daily_food_plan WHERE id = :id LIMIT 1")
    suspend fun getDailyPlanById(id: Long): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<DailyPlanEntity>): List<Long>

    @Update
    suspend fun updatePlan(plan: DailyPlanEntity)

    @Delete
    suspend fun deletePlan(plan: DailyPlanEntity)

    @Query("DELETE FROM daily_food_plan WHERE id = :id")
    suspend fun deletePlanById(id: Long)

    @Query("DELETE FROM daily_food_plan WHERE userId = :userId AND dayType = :dayType")
    suspend fun deletePlansByUserAndDayType(userId: Long, dayType: String)

    @Query("DELETE FROM daily_food_plan WHERE userId = :userId")
    suspend fun deletePlansByUserId(userId: Long)

    @Query("DELETE FROM daily_food_plan")
    suspend fun clearAllPlans()
}
