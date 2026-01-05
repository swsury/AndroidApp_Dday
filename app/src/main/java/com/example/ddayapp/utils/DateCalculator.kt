package com.example.ddayapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateDDay(
        targetDate: String,
        excludePublicHolidays: Boolean,
        excludeWeekends: Boolean,
        holidays: Set<String> = emptySet()
    ): String {

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val target = Calendar.getInstance().apply {
            time = dateFormat.parse(targetDate) ?: Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = ((target.timeInMillis - today.timeInMillis) /
                TimeUnit.DAYS.toMillis(1)).toInt()

        if (diff == 0) return "D-Day"

        val direction = if (diff > 0) 1 else -1
        val current = today.clone() as Calendar
        var count = 0

        while (
            (direction > 0 && current.before(target)) ||
            (direction < 0 && current.after(target))
        ) {
            current.add(Calendar.DAY_OF_MONTH, direction)

            val dayOfWeek = current.get(Calendar.DAY_OF_WEEK)

            // 주말 제외
            if (
                excludeWeekends &&
                (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            ) continue

            // 공휴일 제외
            if (excludePublicHolidays && isPublicHoliday(current, holidays)) continue

            count++
        }

        return if (diff > 0) "D-$count" else "D+$count"
    }

    fun formatDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val year = cal.get(Calendar.YEAR)
            val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
            "$year. $month. $day."
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getTodayString(): String {
        val cal = Calendar.getInstance()
        return String.format(
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun formatDisplayDate(date: String): String {
        // yyyy-MM-dd → yyyy.MM.dd
        return try {
            val parts = date.split("-")
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } catch (e: Exception) {
            date
        }
    }

    private fun isPublicHoliday(
        calendar: Calendar,
        holidays: Set<String>
    ): Boolean {
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        val date = "$year-$month-$day"
        return date in holidays
    }
}
