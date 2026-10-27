package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.FoodPoolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodPoolDao {
    @Query("SELECT * FROM user_food_pool WHERE userId = :userId")
    fun getPoolFlow(userId: Long): Flow<List<FoodPoolEntity>>

    @Query("SELECT * FROM user_food_pool WHERE userId = :userId")
    suspend fun getPool(userId: Long): List<FoodPoolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToPool(poolEntity: FoodPoolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllToPool(poolEntities: List<FoodPoolEntity>): List<Long>

    @Query("DELETE FROM user_food_pool WHERE userId = :userId AND foodId = :foodId")
    suspend fun removeFromPool(userId: Long, foodId: Long)

    @Query("DELETE FROM user_food_pool WHERE userId = :userId")
    suspend fun clearPoolForUser(userId: Long)

    @Query("UPDATE user_food_pool SET isDailyRegular = :isRegular WHERE userId = :userId AND foodId = :foodId")
    suspend fun setDailyRegular(userId: Long, foodId: Long, isRegular: Boolean)

    @Query("DELETE FROM user_food_pool")
    suspend fun clearAllFoodPool()
}
