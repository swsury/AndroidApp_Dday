package com.example.ddayapp.data

data class Settings(
    val holidays: List<Holiday> = emptyList(),
    val sabbathDay: String? = null // "월", "화", ... or null
)
