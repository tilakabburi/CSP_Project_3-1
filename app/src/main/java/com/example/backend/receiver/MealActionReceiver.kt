package com.example.backend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.NutriPathApplication
import com.example.backend.data.repository.NutriPathRepository
import com.example.backend.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val logId = intent.getLongExtra(NotificationHelper.EXTRA_LOG_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        val repository = NutriPathApplication.instance.repository
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    NotificationHelper.ACTION_CONSUMED -> {
                        val mealType = intent.getStringExtra(NotificationHelper.EXTRA_MEAL_TYPE)
                        val user = repository.getUser()
                        if (user != null && !mealType.isNullOrBlank()) {
                            val today = NutriPathRepository.getTodayDateString()
                            repository.ensureTodayMealLogs(user.id, today)
                            val todayLogs = repository.getLogsForDate(user.id, today)
                            val matchingLogs = todayLogs.filter {
                                it.mealType.equals(mealType, ignoreCase = true) && it.status != "consumed"
                            }
                            if (matchingLogs.isNotEmpty()) {
                                matchingLogs.forEach { repository.markLogConsumed(it.id) }
                            } else if (logId != -1L) {
                                repository.markLogConsumed(logId)
                            }
                        } else if (logId != -1L) {
                            repository.markLogConsumed(logId)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "🍽️ Marked as Consumed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    NotificationHelper.ACTION_WILL_EAT -> {
                        val mealType = intent.getStringExtra(NotificationHelper.EXTRA_MEAL_TYPE)
                        val user = repository.getUser()
                        if (user != null && !mealType.isNullOrBlank()) {
                            val today = NutriPathRepository.getTodayDateString()
                            repository.ensureTodayMealLogs(user.id, today)
                            val todayLogs = repository.getLogsForDate(user.id, today)
                            val matchingLogs = todayLogs.filter {
                                it.mealType.equals(mealType, ignoreCase = true) && it.status == "scheduled"
                            }
                            if (matchingLogs.isNotEmpty()) {
                                matchingLogs.forEach { repository.markLogWillEat(it.id) }
                            } else if (logId != -1L) {
                                repository.markLogWillEat(logId)
                            }
                        } else if (logId != -1L) {
                            repository.markLogWillEat(logId)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "🍽️ Marked: Will eat!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    NotificationHelper.ACTION_LOG_WATER -> {
                        val amount = intent.getIntExtra(NotificationHelper.EXTRA_WATER_AMOUNT, 250)
                        val user = repository.getUser()
                        if (user != null) {
                            repository.logWater(user.id, amount, NutriPathRepository.getTodayDateString())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "💧 Added $amount ml of water!", Toast.LENGTH_SHORT).show()
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
