package com.example.backend.data.local.dao

import androidx.room.*
import com.example.backend.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDateFlow(userId: Long, date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date")
    suspend fun getWaterLogsForDate(userId: Long, date: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getWaterLogsForRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getWaterLogsForRange(userId: Long, startDate: String, endDate: String): List<WaterLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun clearAllWaterLogs()
}
