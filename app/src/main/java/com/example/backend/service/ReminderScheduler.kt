package com.example.backend.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.backend.data.local.entity.DailyPlanEntity
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.receiver.MealAlarmReceiver
import java.util.*

object ReminderScheduler {

    /**
     * Schedules daily pre-meal (Stage 1 preparation) and meal-time (Stage 2) alarms for each meal plan,
     * taking into account whether today is a weekday or weekend, and the user's selected advance prep lead time.
     */
    fun scheduleDailyReminders(
        context: Context,
        plans: List<DailyPlanEntity>,
        foodsMap: Map<Long, FoodEntity>,
        leadMinutes: Int = 30
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Check if today is weekend
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
        val targetDayType = if (isWeekend) "WEEKEND" else "WEEKDAY"

        val relevantPlans = if (plans.any { it.dayType == targetDayType }) {
            plans.filter { it.dayType == targetDayType || it.dayType == "ALL" }
        } else {
            plans.filter { it.dayType == "ALL" || it.dayType == "WEEKDAY" }
        }

        // Group plans by meal type (e.g. Breakfast)
        val grouped = relevantPlans.groupBy { it.mealType }

        for ((mealType, mealPlans) in grouped) {
            val primaryPlan = mealPlans.firstOrNull() ?: continue
            val foodSummary = mealPlans.joinToString(", ") { plan ->
                val food = foodsMap[plan.foodId]
                val name = food?.name ?: "Food"
                "${plan.plannedQuantity.toInt()} ${plan.quantityUnit} $name"
            }

            // Parse mealTime (supports both "HH:mm" 24-hour and "hh:mm AM/PM" 12-hour formats)
            val (hour, minute) = parseTime24Hour(primaryPlan.mealTime, mealType)

            // 1. Schedule Pre-Meal Preparation Reminder (e.g. 15, 30, 45, or 60 min before mealTime)
            val totalMinutes = hour * 60 + minute
            val preTotalMinutes = (totalMinutes - leadMinutes + 24 * 60) % (24 * 60)
            val preHour = preTotalMinutes / 60
            val preMinute = preTotalMinutes % 60

            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = (mealType.hashCode() * 10 + 1),
                hour = preHour,
                minute = preMinute,
                alarmType = "pre_meal",
                mealType = mealType,
                mealTime = primaryPlan.mealTime,
                foodSummary = foodSummary
            )

            // 2. Schedule Meal-Time Reminder (at mealTime)
            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = (mealType.hashCode() * 10 + 2),
                hour = hour,
                minute = minute,
                alarmType = "meal_time",
                mealType = mealType,
                mealTime = primaryPlan.mealTime,
                foodSummary = foodSummary
            )
        }

        // Schedule End-of-Day Review at 21:00 (9:00 PM)
        scheduleAlarm(
            context = context,
            alarmManager = alarmManager,
            requestCode = 99999,
            hour = 21,
            minute = 0,
            alarmType = "end_of_day",
            mealType = "Daily Review",
            mealTime = "21:00",
            foodSummary = ""
        )
    }

    const val WATER_ALARM_BASE_REQUEST_CODE = 80000

    /**
     * Schedules periodic water reminders throughout the daytime hours (e.g., 08:00 to 21:00)
     * at specified intervals (every 1, 2, or 3 hours).
     */
    fun scheduleWaterReminders(
        context: Context,
        enabled: Boolean,
        intervalHours: Int = 2
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Always cancel previous scheduled water alarms first to prevent duplicates
        cancelWaterReminders(context)

        if (!enabled) return

        val step = intervalHours.coerceIn(1, 4)
        val startHour = 8  // 8:00 AM
        val endHour = 21   // 9:00 PM

        var reqCodeOffset = 0
        for (h in startHour..endHour step step) {
            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = WATER_ALARM_BASE_REQUEST_CODE + reqCodeOffset,
                hour = h,
                minute = 0,
                alarmType = "water_reminder",
                mealType = "Hydration",
                mealTime = String.format(Locale.getDefault(), "%02d:00", h),
                foodSummary = "Drink water"
            )
            reqCodeOffset++
        }
    }

    fun cancelWaterReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (i in 0..24) {
            val intent = Intent(context, MealAlarmReceiver::class.java).apply {
                putExtra("alarm_type", "water_reminder")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                WATER_ALARM_BASE_REQUEST_CODE + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        hour: Int,
        minute: Int,
        alarmType: String,
        mealType: String,
        mealTime: String,
        foodSummary: String
    ) {
        val intent = Intent(context, MealAlarmReceiver::class.java).apply {
            putExtra("alarm_type", alarmType)
            putExtra("meal_type", mealType)
            putExtra("meal_time", mealTime)
            putExtra("food_summary", foodSummary)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // In case exact alarm permission is restricted, fallback to standard inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Interactive Simulator for immediate testing in AI Studio Emulator.
     * Lets user test Stage 1 (Pre-meal), Stage 2 (Meal time), Stage 3 (End-of-day), or Stage 4 (Water reminder) on demand!
     */
    fun triggerSimulatedNotification(
        context: Context,
        stage: Int, // 1 = Pre-Meal, 2 = Meal-Time, 3 = End-of-Day, 4 = Water Reminder
        mealType: String = "Breakfast",
        mealTime: String = "08:00 AM",
        foodSummary: String = "2 pieces Eggs, 1 piece Banana, 250 ml Milk",
        logId: Long = 1L,
        currentWaterMl: Int = 750,
        targetWaterMl: Int = 2200
    ) {
        when (stage) {
            1 -> NotificationHelper.showPreMealNotification(
                context = context,
                mealType = mealType,
                mealTime = mealTime,
                foodSummary = foodSummary
            )
            2 -> NotificationHelper.showMealTimeNotification(
                context = context,
                mealType = mealType,
                foodSummary = foodSummary,
                logId = logId
            )
            3 -> NotificationHelper.showEndOfDayReviewNotification(
                context = context,
                incompleteCount = 2
            )
            4 -> NotificationHelper.showWaterReminderNotification(
                context = context,
                currentWaterMl = currentWaterMl,
                targetWaterMl = targetWaterMl
            )
        }
    }

    /**
     * Builds the complete 12-notification sequence covering an entire day:
     * Pre-Meal preparation alerts, Meal-time alerts with Consumed/Will Eat actions,
     * Hydration alerts (+250ml/+500ml), and End-of-day recovery review.
     */
    fun getFullDayNotificationSequence(
        plans: List<DailyPlanEntity> = emptyList(),
        foodsMap: Map<Long, FoodEntity> = emptyMap(),
        leadMinutes: Int = 30,
        currentWaterMl: Int = 750,
        targetWaterMl: Int = 2200
    ): List<SimulatedNotificationInfo> {
        val defaultFoods = mapOf(
            "Breakfast" to "100 g Oatmeal, cooked, 100 g Cows' milk, 100 g Eggs, raw",
            "Lunch" to "100 g Rice, cooked, 100 g Rajma, cooked, 100 g Steamed cabbage",
            "Snacks" to "100 g Apples, raw, 100 g Almonds, 100 g Buttermilk",
            "Dinner" to "100 g Tuna, 100 g Sweet potatoes, 100 g Spinach"
        )
        val defaultTimes = mapOf(
            "Breakfast" to "08:30",
            "Lunch" to "13:00",
            "Snacks" to "17:00",
            "Dinner" to "20:30"
        )

        fun getMealSummary(mealType: String): Pair<String, String> {
            val matchingPlans = plans.filter { it.mealType.equals(mealType, ignoreCase = true) }
            val time = matchingPlans.firstOrNull()?.mealTime ?: defaultTimes[mealType] ?: "08:00"
            val summary = if (matchingPlans.isNotEmpty()) {
                matchingPlans.joinToString(", ") { p ->
                    val f = foodsMap[p.foodId]
                    val name = f?.name ?: "Nutritious Food"
                    "${p.plannedQuantity.toInt()} ${p.quantityUnit} $name"
                }
            } else {
                defaultFoods[mealType] ?: "Balanced wholesome food"
            }
            return Pair(time, summary)
        }

        fun toAmPm(hour: Int, minute: Int): String {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour < 12) "AM" else "PM"
            return String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm)
        }

        fun parseHAndM(timeStr: String, mealType: String = ""): Pair<Int, Int> {
            return parseTime24Hour(timeStr, mealType)
        }

        fun getPreMealTime(hour: Int, minute: Int, lead: Int): String {
            val totalMin = (hour * 60 + minute - lead + 24 * 60) % (24 * 60)
            return toAmPm(totalMin / 60, totalMin % 60)
        }

        val (bfTimeRaw, bfSummary) = getMealSummary("Breakfast")
        val (bfHour, bfMin) = parseHAndM(bfTimeRaw, "Breakfast")
        val bfTimeAmPm = toAmPm(bfHour, bfMin)
        val bfPreTimeAmPm = getPreMealTime(bfHour, bfMin, leadMinutes)

        val (luTimeRaw, luSummary) = getMealSummary("Lunch")
        val (luHour, luMin) = parseHAndM(luTimeRaw, "Lunch")
        val luTimeAmPm = toAmPm(luHour, luMin)
        val luPreTimeAmPm = getPreMealTime(luHour, luMin, leadMinutes)

        val (snTimeRaw, snSummary) = getMealSummary("Snacks")
        val (snHour, snMin) = parseHAndM(snTimeRaw, "Snacks")
        val snTimeAmPm = toAmPm(snHour, snMin)
        val snPreTimeAmPm = getPreMealTime(snHour, snMin, leadMinutes)

        val (diTimeRaw, diSummary) = getMealSummary("Dinner")
        val (diHour, diMin) = parseHAndM(diTimeRaw, "Dinner")
        val diTimeAmPm = toAmPm(diHour, diMin)
        val diPreTimeAmPm = getPreMealTime(diHour, diMin, leadMinutes)

        return listOf(
            // 1. Pre-Breakfast
            SimulatedNotificationInfo(
                id = 5001,
                timeLabel = bfPreTimeAmPm,
                stageCategory = "Pre-Meal Preparation",
                title = "🍳 Breakfast at $bfTimeAmPm",
                subtitle = "Items to prepare ($leadMinutes min advance)",
                detailText = "Upcoming Breakfast at $bfTimeAmPm:\n${NotificationHelper.formatFoodBulletPoints(bfSummary)}",
                actions = listOf("Open App"),
                channelName = "Pre-Meal Reminders",
                iconEmoji = "🍳",
                stageType = 1,
                mealType = "Breakfast"
            ),
            // 2. Breakfast Meal Time
            SimulatedNotificationInfo(
                id = 5010,
                timeLabel = bfTimeAmPm,
                stageCategory = "Meal Time Alert",
                title = "🍽️ It's Breakfast time!",
                subtitle = "Items to consume",
                detailText = "Items to consume:\n${NotificationHelper.formatFoodBulletPoints(bfSummary)}",
                actions = listOf("Consumed", "Will"),
                channelName = "Meal Time Reminders",
                iconEmoji = "🥣",
                stageType = 2,
                mealType = "Breakfast"
            ),
            // 3. Morning Hydration
            SimulatedNotificationInfo(
                id = 5020,
                timeLabel = "10:30 AM",
                stageCategory = "Hydration Reminder",
                title = "💧 Hydration",
                subtitle = "Mid-morning water check",
                detailText = "You have to drink water (Goal: $targetWaterMl ml)",
                actions = listOf("+250 ml", "+500 ml"),
                channelName = "Hydration & Water Reminders",
                iconEmoji = "💧",
                stageType = 4
            ),
            // 4. Pre-Lunch
            SimulatedNotificationInfo(
                id = 5030,
                timeLabel = luPreTimeAmPm,
                stageCategory = "Pre-Meal Preparation",
                title = "🥗 Lunch at $luTimeAmPm",
                subtitle = "Items to prepare ($leadMinutes min advance)",
                detailText = "Upcoming Lunch at $luTimeAmPm:\n${NotificationHelper.formatFoodBulletPoints(luSummary)}",
                actions = listOf("Open App"),
                channelName = "Pre-Meal Reminders",
                iconEmoji = "🥗",
                stageType = 1,
                mealType = "Lunch"
            ),
            // 5. Lunch Meal Time
            SimulatedNotificationInfo(
                id = 5040,
                timeLabel = luTimeAmPm,
                stageCategory = "Meal Time Alert",
                title = "🍽️ It's Lunch time!",
                subtitle = "Items to consume",
                detailText = "Items to consume:\n${NotificationHelper.formatFoodBulletPoints(luSummary)}",
                actions = listOf("Consumed", "Will"),
                channelName = "Meal Time Reminders",
                iconEmoji = "🍗",
                stageType = 2,
                mealType = "Lunch"
            ),
            // 6. Afternoon Hydration
            SimulatedNotificationInfo(
                id = 5050,
                timeLabel = "03:30 PM",
                stageCategory = "Hydration Reminder",
                title = "💧 Hydration",
                subtitle = "Afternoon hydration boost",
                detailText = "You have to drink water (Stay hydrated & energized)",
                actions = listOf("+250 ml", "+500 ml"),
                channelName = "Hydration & Water Reminders",
                iconEmoji = "💧",
                stageType = 4
            ),
            // 7. Pre-Snacks
            SimulatedNotificationInfo(
                id = 5060,
                timeLabel = snPreTimeAmPm,
                stageCategory = "Pre-Meal Preparation",
                title = "🥪 Snacks at $snTimeAmPm",
                subtitle = "Items to prepare ($leadMinutes min advance)",
                detailText = "Upcoming Snacks at $snTimeAmPm:\n${NotificationHelper.formatFoodBulletPoints(snSummary)}",
                actions = listOf("Open App"),
                channelName = "Pre-Meal Reminders",
                iconEmoji = "🥪",
                stageType = 1,
                mealType = "Snacks"
            ),
            // 8. Snacks Meal Time
            SimulatedNotificationInfo(
                id = 5070,
                timeLabel = snTimeAmPm,
                stageCategory = "Meal Time Alert",
                title = "🍽️ It's Snacks time!",
                subtitle = "Items to consume",
                detailText = "Items to consume:\n${NotificationHelper.formatFoodBulletPoints(snSummary)}",
                actions = listOf("Consumed", "Will"),
                channelName = "Meal Time Reminders",
                iconEmoji = "🍎",
                stageType = 2,
                mealType = "Snacks"
            ),
            // 9. Evening Hydration
            SimulatedNotificationInfo(
                id = 5080,
                timeLabel = "06:30 PM",
                stageCategory = "Hydration Reminder",
                title = "💧 Hydration",
                subtitle = "Evening hydration check",
                detailText = "You have to drink water (Keep up your daily target)",
                actions = listOf("+250 ml", "+500 ml"),
                channelName = "Hydration & Water Reminders",
                iconEmoji = "💧",
                stageType = 4
            ),
            // 10. Pre-Dinner
            SimulatedNotificationInfo(
                id = 5090,
                timeLabel = diPreTimeAmPm,
                stageCategory = "Pre-Meal Preparation",
                title = "🍲 Dinner at $diTimeAmPm",
                subtitle = "Items to prepare ($leadMinutes min advance)",
                detailText = "Upcoming Dinner at $diTimeAmPm:\n${NotificationHelper.formatFoodBulletPoints(diSummary)}",
                actions = listOf("Open App"),
                channelName = "Pre-Meal Reminders",
                iconEmoji = "🍲",
                stageType = 1,
                mealType = "Dinner"
            ),
            // 11. Dinner Meal Time
            SimulatedNotificationInfo(
                id = 5100,
                timeLabel = diTimeAmPm,
                stageCategory = "Meal Time Alert",
                title = "🍽️ It's Dinner time!",
                subtitle = "Items to consume",
                detailText = "Items to consume:\n${NotificationHelper.formatFoodBulletPoints(diSummary)}",
                actions = listOf("Consumed", "Will"),
                channelName = "Meal Time Reminders",
                iconEmoji = "🍲",
                stageType = 2,
                mealType = "Dinner"
            ),
            // 12. End-of-Day Review
            SimulatedNotificationInfo(
                id = 5110,
                timeLabel = "09:30 PM",
                stageCategory = "Daily Review",
                title = "🌙 Review your Daily Log",
                subtitle = "Gentle daily recovery check",
                detailText = "You have 2 incomplete food reminders today. Tap to review your daily nutrition log.",
                actions = listOf("Review Log"),
                channelName = "Daily End-of-Day Review",
                iconEmoji = "🌙",
                stageType = 3
            )
        )
    }

    /**
     * Triggers the full 1-day notification sequence into Android's status bar / notification manager.
     * All 12 notifications are sent with distinct IDs so they appear simultaneously for verification.
     */
    fun triggerFullDaySimulation(
        context: Context,
        plans: List<DailyPlanEntity> = emptyList(),
        foodsMap: Map<Long, FoodEntity> = emptyMap(),
        leadMinutes: Int = 30,
        currentWaterMl: Int = 750,
        targetWaterMl: Int = 2200
    ): List<SimulatedNotificationInfo> {
        val sequence = getFullDayNotificationSequence(
            plans = plans,
            foodsMap = foodsMap,
            leadMinutes = leadMinutes,
            currentWaterMl = currentWaterMl,
            targetWaterMl = targetWaterMl
        )

        for (item in sequence) {
            triggerSingleItemFromSequence(
                context = context,
                item = item,
                currentWaterMl = currentWaterMl,
                targetWaterMl = targetWaterMl
            )
        }

        return sequence
    }

    /**
     * Triggers an individual simulated item from the 1-day sequence.
     */
    fun triggerSingleItemFromSequence(
        context: Context,
        item: SimulatedNotificationInfo,
        currentWaterMl: Int = 750,
        targetWaterMl: Int = 2200
    ) {
        when (item.stageType) {
            1 -> NotificationHelper.showPreMealNotification(
                context = context,
                mealType = item.mealType ?: "Meal",
                mealTime = item.timeLabel,
                foodSummary = item.detailText,
                notificationId = item.id
            )
            2 -> NotificationHelper.showMealTimeNotification(
                context = context,
                mealType = item.mealType ?: "Meal",
                foodSummary = item.detailText,
                logId = 1L,
                notificationId = item.id
            )
            3 -> NotificationHelper.showEndOfDayReviewNotification(
                context = context,
                incompleteCount = 2,
                notificationId = item.id
            )
            4 -> NotificationHelper.showWaterReminderNotification(
                context = context,
                currentWaterMl = currentWaterMl,
                targetWaterMl = targetWaterMl,
                notificationId = item.id
            )
        }
    }

    /**
     * Parses both 24-hour ("13:30") and 12-hour ("01:30 PM", "08:00 AM") time strings into (hour24, minute).
     */
    fun parseTime24Hour(timeStr: String, mealType: String = ""): Pair<Int, Int> {
        val cleaned = timeStr.trim().uppercase(Locale.getDefault())
        val isPm = cleaned.contains("PM")
        val isAm = cleaned.contains("AM")
        val digitsOnly = cleaned.replace("AM", "").replace("PM", "").trim()
        val parts = digitsOnly.split(":")
        val defaultHour = when (mealType.lowercase(Locale.getDefault())) {
            "lunch" -> 13
            "snacks", "snack" -> 17
            "dinner" -> 20
            else -> 8
        }
        val rawHour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return Pair(defaultHour, 0)
        val rawMin = parts.getOrNull(1)?.trim()?.takeWhile { it.isDigit() }?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val hour24 = when {
            isPm && rawHour < 12 -> rawHour + 12
            isAm && rawHour == 12 -> 0
            else -> rawHour.coerceIn(0, 23)
        }
        return Pair(hour24, rawMin)
    }
}

/**
 * Model representing a simulated notification for UI inspection and verification.
 */
data class SimulatedNotificationInfo(
    val id: Int,
    val timeLabel: String,
    val stageCategory: String, // "Pre-Meal Preparation", "Meal Time Alert", "Hydration Reminder", "Daily Review"
    val title: String,
    val subtitle: String,
    val detailText: String,
    val actions: List<String>,
    val channelName: String,
    val iconEmoji: String,
    val stageType: Int, // 1 = Pre-Meal, 2 = Meal-Time, 3 = End-of-Day, 4 = Water Reminder
    val mealType: String? = null
)
