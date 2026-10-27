package com.example.frontend.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatUtil {

    /**
     * Parses any time representation ("08:00", "8:00", "08:30 AM", "1:30 PM", "13:00", "20:30")
     * into a standard 24-hour Pair of (hour, minute).
     */
    fun parseTimeHourMinute(rawTime: String, defaultMealType: String = ""): Pair<Int, Int> {
        val clean = rawTime.trim()
        if (clean.isBlank()) {
            val fallbackHour = if (defaultMealType.equals("Breakfast", ignoreCase = true)) 8
            else if (defaultMealType.equals("Lunch", ignoreCase = true)) 13
            else if (defaultMealType.equals("Snacks", ignoreCase = true)) 17
            else 20
            return Pair(fallbackHour, 0)
        }

        val isExplicitPm = clean.endsWith("PM", ignoreCase = true)
        val isExplicitAm = clean.endsWith("AM", ignoreCase = true)
        val stripped = clean.replace("(?i)am|pm".toRegex(), "").trim()
        val parts = stripped.split(":")
        val rawH = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 8
        val rawM = parts.getOrNull(1)?.trim()?.takeWhile { it.isDigit() }?.toIntOrNull()?.coerceIn(0, 59) ?: 0

        var hour24 = rawH
        if (isExplicitPm && hour24 < 12) {
            hour24 += 12
        } else if (isExplicitAm && hour24 == 12) {
            hour24 = 0
        }

        return Pair(hour24.coerceIn(0, 23), rawM)
    }

    /**
     * Formats a raw time string into either 12-hour AM/PM or 24-hour format.
     */
    fun formatDisplayTime(rawTime: String, is24Hour: Boolean, defaultMealType: String = ""): String {
        val (h24, m) = parseTimeHourMinute(rawTime, defaultMealType)
        return if (is24Hour) {
            String.format(Locale.US, "%02d:%02d", h24, m)
        } else {
            val period = if (h24 >= 12) "PM" else "AM"
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            String.format(Locale.US, "%02d:%02d %s", h12, m, period)
        }
    }

    /**
     * Formats current date/time in the user's preferred format.
     */
    fun formatCurrentTime(is24Hour: Boolean): String {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        return SimpleDateFormat(pattern, Locale.US).format(Date())
    }
}
