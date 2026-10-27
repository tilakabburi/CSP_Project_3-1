package com.example.backend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NutriPathApplication
import com.example.backend.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val repository = NutriPathApplication.instance.repository
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = repository.getUser()
                    if (user != null) {
                        val plans = repository.getDailyPlans(user.id)
                        val allFoods = repository.getAllFoods().associateBy { it.id }
                        ReminderScheduler.scheduleDailyReminders(context, plans, allFoods)
                        ReminderScheduler.scheduleWaterReminders(context, enabled = true, intervalHours = 2)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
