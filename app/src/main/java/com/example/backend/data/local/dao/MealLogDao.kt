package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY timestamp ASC")
    fun getLogsForDateFlow(userId: Long, date: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY timestamp ASC")
    suspend fun getLogsForDate(userId: Long, date: String): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getLogsForRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    suspend fun getLogsForRange(userId: Long, startDate: String, endDate: String): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): MealLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MealLogEntity): Long

    @Update
    suspend fun updateLog(log: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM meal_logs WHERE userId = :userId AND date = :date AND status = 'scheduled'")
    suspend fun deleteScheduledLogsForDate(userId: Long, date: String)

    @Query("DELETE FROM meal_logs WHERE userId = :userId AND status = 'scheduled'")
    suspend fun deleteAllScheduledLogsForUser(userId: Long)

    @Query("DELETE FROM meal_logs")
    suspend fun clearAllLogs()
}
