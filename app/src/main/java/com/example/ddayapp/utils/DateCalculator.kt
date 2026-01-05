package com.example.ddayapp. utils

import java.text.SimpleDateFormat
import java. util.*
import java.util.concurrent.TimeUnit

object DateCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * D-Day 계산
     * @param targetDate 목표 날짜 (yyyy-MM-dd)
     * @param excludePublicHolidays 공휴일 제외 여부
     * @param excludeWeekends 주말 제외 여부
     * @param holidays 공휴일 Set (yyyy-MM-dd 형식)
     * @return D-Day 문자열 (예: "D-30", "D-Day", "D+15")
     */
    fun calculateDDay(
        targetDate:  String,
        excludePublicHolidays: Boolean,
        excludeWeekends:  Boolean,
        holidays: Set<String> = emptySet()
    ): String {

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar. SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val target = Calendar.getInstance().apply {
            time = dateFormat.parse(targetDate) ?: Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar. SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 오늘과 목표일이 같으면 D-Day
        if (isSameDay(today, target)) return "D-Day"

        val direction = if (target.after(today)) 1 else -1
        val current = today.clone() as Calendar
        var count = 0

        // 날짜를 하나씩 이동하면서 카운트
        while (
            (direction > 0 && ! current.after(target)) ||
            (direction < 0 && !current. before(target))
        ) {
            // 오늘은 제외하고 시작
            if (! isSameDay(current, today)) {
                val dayOfWeek = current.get(Calendar.DAY_OF_WEEK)

                // 주말 제외 옵션이 켜져있고, 토요일 또는 일요일이면 건너뛰기
                val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar. SUNDAY)
                if (excludeWeekends && isWeekend) {
                    current.add(Calendar.DAY_OF_MONTH, direction)
                    continue
                }

                // 공휴일 제외 옵션이 켜져있고, 공휴일이면 건너뛰기
                if (excludePublicHolidays && isPublicHoliday(current, holidays)) {
                    current. add(Calendar.DAY_OF_MONTH, direction)
                    continue
                }

                count++
            }

            // 목표일에 도달했으면 종료
            if (isSameDay(current, target)) break

            current.add(Calendar.DAY_OF_MONTH, direction)
        }

        return if (direction > 0) "D-$count" else "D+${Math.abs(count)}"
    }

    /**
     * 두 Calendar가 같은 날짜인지 확인
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar. YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 날짜를 포맷팅 (yyyy.  MM. dd.)
     */
    fun formatDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val year = cal.get(Calendar. YEAR)
            val month = String.format("%02d", cal. get(Calendar.MONTH) + 1)
            val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
            "$year. $month. $day."
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * 오늘 날짜를 문자열로 반환 (yyyy-MM-dd)
     */
    fun getTodayString(): String {
        val cal = Calendar.getInstance()
        return String.format(
            "%04d-%02d-%02d",
            cal.get(Calendar. YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * 날짜 표시 형식 변환 (yyyy-MM-dd → yyyy. MM.dd)
     */
    fun formatDisplayDate(date: String): String {
        return try {
            val parts = date.split("-")
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } catch (e:  Exception) {
            date
        }
    }

    /**
     * 공휴일인지 확인
     * @param calendar 확인할 날짜
     * @param holidays 공휴일 Set (yyyy-MM-dd 형식)
     * @return 공휴일이면 true
     */
    private fun isPublicHoliday(
        calendar: Calendar,
        holidays: Set<String>
    ): Boolean {
        val year = calendar.get(Calendar. YEAR)
        val month = String.format("%02d", calendar. get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        val date = "$year-$month-$day"
        return date in holidays
    }
}