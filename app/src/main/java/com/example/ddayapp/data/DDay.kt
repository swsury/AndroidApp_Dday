package com.example.ddayapp.data

data class DDay(
    val id: Long,
    val title: String,
    val labelTitle: String,
    val date: String,               // yyyy-MM-dd
    val color: String,

    // 🔥 D-day 계산 옵션
    val excludePublicHolidays: Boolean,
    val excludeWeekends: Boolean,

    // 공휴일 목록 (yyyy-MM-dd)
    val holidays: Set<String> = emptySet()
)
