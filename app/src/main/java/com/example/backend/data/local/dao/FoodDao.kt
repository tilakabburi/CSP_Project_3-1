package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE active = 1 ORDER BY name ASC")
    fun getAllFoodsFlow(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE active = 1 ORDER BY name ASC")
    suspend fun getAllFoods(): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE id IN (:ids)")
    suspend fun getFoodsByIds(ids: List<Long>): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>): List<Long>

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFoods(foods: List<FoodEntity>)

    @Query("DELETE FROM foods WHERE isCustom = 0")
    suspend fun deleteNonCustomFoods()
}
