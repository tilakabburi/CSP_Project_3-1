package com.example.backend.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.backend.receiver.MealActionReceiver

object NotificationHelper {

    const val CHANNEL_PRE_MEAL = "channel_pre_meal"
    const val CHANNEL_MEAL_TIME = "channel_meal_time"
    const val CHANNEL_DAILY_REVIEW = "channel_daily_review"
    const val CHANNEL_WATER_REMINDER = "channel_water_reminder"

    const val EXTRA_ACTION = "extra_action"
    const val EXTRA_LOG_ID = "extra_log_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_DEEP_LINK = "extra_deep_link"
    const val EXTRA_MEAL_TYPE = "extra_meal_type"
    const val EXTRA_WATER_AMOUNT = "extra_water_amount"

    const val ACTION_CONSUMED = "com.example.action.CONSUMED"
    const val ACTION_WILL_EAT = "com.example.action.WILL_EAT"
    const val ACTION_LOG_WATER = "com.example.action.LOG_WATER"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val preMealChannel = NotificationChannel(
                CHANNEL_PRE_MEAL,
                "Pre-Meal Reminders (1 Hour Before)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you 1 hour before scheduled meal times with planned foods."
                enableVibration(true)
            }

            val mealTimeChannel = NotificationChannel(
                CHANNEL_MEAL_TIME,
                "Meal Time Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers at actual meal times with Consumed, Will Eat, and Substitute actions."
                enableVibration(true)
            }

            val reviewChannel = NotificationChannel(
                CHANNEL_DAILY_REVIEW,
                "Daily End-of-Day Review",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle evening reminder to review incomplete daily food logs."
            }

            val waterChannel = NotificationChannel(
                CHANNEL_WATER_REMINDER,
                "Hydration & Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle hydration alerts throughout the day to meet your daily water goal."
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(preMealChannel, mealTimeChannel, reviewChannel, waterChannel))
        }
    }

    fun formatFoodBulletPoints(foodSummary: String): String {
        val rawItems = foodSummary.split(",", "\n", ";")
            .map { it.trim().trimStart('•', '-', '*').trim() }
            .filter { it.isNotEmpty() }
        if (rawItems.isEmpty()) return "• Planned nutritious meal"
        return rawItems.joinToString("\n") { "• $it" }
    }

    /**
     * Stage 1: Preparation Notification (Before Meal)
     * Displays the list of planned items in bullet points.
     */
    fun showPreMealNotification(
        context: Context,
        mealType: String,
        mealTime: String,
        foodSummary: String,
        notificationId: Int = 1001
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, "home")
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bulletItems = formatFoodBulletPoints(foodSummary)

        val notification = NotificationCompat.Builder(context, CHANNEL_PRE_MEAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🍳 $mealType at $mealTime")
            .setContentText("Items to prepare")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Upcoming $mealType at $mealTime:\n\n$bulletItems"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not yet granted
        }
    }

    /**
     * Stage 2: Actual Meal Time Notification
     * Displays items in bullet points with two direct action buttons: [Consumed] and [Will].
     */
    fun showMealTimeNotification(
        context: Context,
        mealType: String,
        foodSummary: String,
        logId: Long,
        notificationId: Int = 2001
    ) {
        val bulletItems = formatFoodBulletPoints(foodSummary)

        // Action 1: Consumed
        val consumedIntent = Intent(context, MealActionReceiver::class.java).apply {
            action = ACTION_CONSUMED
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEAL_TYPE, mealType)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val consumedPending = PendingIntent.getBroadcast(
            context,
            (notificationId * 10 + 1),
            consumedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Will
        val willIntent = Intent(context, MealActionReceiver::class.java).apply {
            action = ACTION_WILL_EAT
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEAL_TYPE, mealType)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val willPending = PendingIntent.getBroadcast(
            context,
            (notificationId * 10 + 2),
            willIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, "home")
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEAL_TYPE, mealType)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MEAL_TIME)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🍽️ It's $mealType time!")
            .setContentText("Items to consume")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Items to consume:\n$bulletItems"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.checkbox_on_background, "Consumed", consumedPending)
            .addAction(android.R.drawable.ic_menu_recent_history, "Will", willPending)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission check
        }
    }

    /**
     * Single End-of-Day Recovery Notification
     */
    fun showEndOfDayReviewNotification(context: Context, incompleteCount: Int, notificationId: Int = 3001) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, "log")
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REVIEW)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌙 Review your Daily Log")
            .setContentText("You have $incompleteCount incomplete food reminders today. Tap to review.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "You have $incompleteCount incomplete food reminders today.\nReview your Daily Log and record what actually happened to keep your nutrition totals accurate."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Ignored
        }
    }

    /**
     * Water / Hydration Reminder Notification
     * Short and direct: "You have to drink water" with buttons to add water in the notification itself.
     */
    fun showWaterReminderNotification(
        context: Context,
        currentWaterMl: Int = 0,
        targetWaterMl: Int = 0,
        notificationId: Int = 4001
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, "water")
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Add 250ml
        val log250Intent = Intent(context, MealActionReceiver::class.java).apply {
            action = ACTION_LOG_WATER
            putExtra(EXTRA_WATER_AMOUNT, 250)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val log250Pending = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            log250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Add 500ml
        val log500Intent = Intent(context, MealActionReceiver::class.java).apply {
            action = ACTION_LOG_WATER
            putExtra(EXTRA_WATER_AMOUNT, 500)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val log500Pending = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            log500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WATER_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 Hydration")
            .setContentText("You have to drink water")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("You have to drink water")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_add, "+250 ml", log250Pending)
            .addAction(android.R.drawable.ic_menu_add, "+500 ml", log500Pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission check
        }
    }
}
