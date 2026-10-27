package com.example.backend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NutriPathApplication
import com.example.backend.data.repository.NutriPathRepository
import com.example.backend.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MealAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("alarm_type") ?: "meal_time"
        val mealType = intent.getStringExtra("meal_type") ?: "Meal"
        val mealTime = intent.getStringExtra("meal_time") ?: "08:00"
        val foodSummary = intent.getStringExtra("food_summary") ?: "Your planned nutritious foods"
        val logId = intent.getLongExtra("log_id", -1L)

        val repository = NutriPathApplication.instance.repository
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    "pre_meal" -> {
                        NotificationHelper.showPreMealNotification(
                            context = context,
                            mealType = mealType,
                            mealTime = mealTime,
                            foodSummary = foodSummary
                        )
                    }
                    "meal_time" -> {
                        NotificationHelper.showMealTimeNotification(
                            context = context,
                            mealType = mealType,
                            foodSummary = foodSummary,
                            logId = logId
                        )
                    }
                    "water_reminder" -> {
                        val user = repository.getUser()
                        if (user != null) {
                            val targets = repository.getTarget(user.id)
                            val waterTarget = targets?.waterTargetMl ?: 2200
                            val todayLogs = repository.getWaterLogsForDate(user.id, NutriPathRepository.getTodayDateString())
                            val currentWater = todayLogs.sumOf { it.quantityMl }
                            NotificationHelper.showWaterReminderNotification(
                                context = context,
                                currentWaterMl = currentWater,
                                targetWaterMl = waterTarget
                            )
                        } else {
                            NotificationHelper.showWaterReminderNotification(
                                context = context,
                                currentWaterMl = 500,
                                targetWaterMl = 2200
                            )
                        }
                    }
                    "end_of_day" -> {
                        val user = repository.getUser()
                        if (user != null) {
                            val todayLogs = repository.getLogsForDate(user.id, NutriPathRepository.getTodayDateString())
                            val incomplete = todayLogs.count { it.status == "scheduled" || it.status == "will_eat" }
                            if (incomplete > 0) {
                                NotificationHelper.showEndOfDayReviewNotification(context, incomplete)
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
