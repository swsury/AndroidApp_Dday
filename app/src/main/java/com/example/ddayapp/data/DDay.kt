package com.example.ddayapp.data

data class DDay(
    val id: Long,
    val title: String,
    val labelTitle: String,
    val date: String,               // yyyy-MM-dd
    val color: String,

    // 🔥 D-day 계산 옵션
    val excludePublicHolidays: Boolean,
    val excludeCustomDays: Boolean,      // 🔥 안식일 제외 옵션 추가
    val excludeWeekends: Boolean,

    // 공휴일 목록 (yyyy-MM-dd)
    val publicHolidays: Set<String> = emptySet(),   // 🔥 공휴일
    val customDays: Set<String> = emptySet()        // 🔥 안식일
)