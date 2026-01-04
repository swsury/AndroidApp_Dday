package com.example.ddayapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateCalculator {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * D-day 계산
     * @param targetDate 목표 날짜 (yyyy-MM-dd)
     * @param excludeHolidays 공휴일(주말) 제외 여부
     * @param selectedDays 선택된 요일 리스트 (예: ["월", "화"])
     * @return D-day 문자열 (예: "D-15", "D-Day", "D+5")
     */
    fun calculateDDay(
        targetDate: String,
        excludeHolidays: Boolean = false,
        selectedDays: List<String> = emptyList(),
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

        val diffInMillis = target.timeInMillis - today.timeInMillis
        val totalDiff = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

        // 옵션 없으면 기본 계산
        if (!excludeHolidays && selectedDays.isEmpty()) {
            return when {
                totalDiff == 0 -> "D-Day"
                totalDiff > 0 -> "D-$totalDiff"
                else -> "D+${kotlin.math.abs(totalDiff)}"
            }
        }

        var count = 0
        val current = today.clone() as Calendar
        val direction = if (totalDiff >= 0) 1 else -1

        while (
            (direction > 0 && current.before(target)) ||
            (direction < 0 && current.after(target))
        ) {
            current.add(Calendar.DAY_OF_MONTH, direction)
            val dayOfWeek = current.get(Calendar.DAY_OF_WEEK)

            // 1️⃣ 요일 선택 (최우선)
            if (selectedDays.isNotEmpty()) {
                val dayName = getDayName(dayOfWeek)
                if (dayName in selectedDays) {
                    count++
                }
            }
            // 2️⃣ 공휴일 제외 (주말 포함)
            else if (excludeHolidays) {
                if (!isPublicHoliday(current, holidays)) {
                    count++
                }
            }
            // 3️⃣ 기본
            else {
                count++
            }
        }

        return when {
            count == 0 && totalDiff == 0 -> "D-Day"
            totalDiff >= 0 -> "D-$count"
            else -> "D+$count"
        }
    }


    /**
     * 날짜 포맷팅 (yyyy. MM. dd.)
     */
    fun formatDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            val calendar = Calendar.getInstance().apply { time = date }
            val year = calendar.get(Calendar.YEAR)
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
            "$year. $month. $day."
        } catch (e: Exception) {
            dateStr
        }
    }
    
    /**
     * 날짜 표시 포맷팅 (yyyy년 MM월 dd일)
     */
    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            val calendar = Calendar.getInstance().apply { time = date }
            val year = calendar.get(Calendar.YEAR)
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
            "${year}년 ${month}월 ${day}일"
        } catch (e: Exception) {
            dateStr
        }
    }
    
    /**
     * 요일 이름 가져오기
     */
    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }

    /**
     * 공휴일 가져오기
     */
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


    /**
     * 오늘 날짜를 yyyy-MM-dd 형식으로 반환
     */
    fun getTodayString(): String {
        return dateFormat.format(Date())
    }
}
