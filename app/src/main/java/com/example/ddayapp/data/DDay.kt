package com.example.ddayapp.data

data class DDay(
    val id: Long,
    val title:  String,
    val labelTitle:  String,
    val date: String,               // yyyy-MM-dd
    val color: String,

    // 🔥 D-day 계산 옵션만 저장
    val excludePublicHolidays: Boolean,
    val excludeCustomDays: Boolean,
    val excludeWeekends: Boolean
)