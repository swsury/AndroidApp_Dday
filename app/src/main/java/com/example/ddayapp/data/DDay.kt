package com.example.ddayapp.data

data class DDay(
    val id: Long,
    val title: String,
    val labelTitle: String,
    val date: String,
    val color: String,

    val excludePublicHolidays:  Boolean,
    val excludeCustomDays: Boolean,
    val excludedWeekdays: Set<Int> = emptySet(),

    val order: Int = 0
)