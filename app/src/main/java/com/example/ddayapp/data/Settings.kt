package com.example.ddayapp.data

data class Settings(
    val publicHolidays: List<Holiday> = emptyList(),  //  공휴일 (자동)
    val customDays: List<Holiday> = emptyList(),      //  안식일 (수동)
    val sabbathDay: String? = null
)