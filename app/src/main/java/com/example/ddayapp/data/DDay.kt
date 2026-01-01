package com.example.ddayapp.data

import java.util.UUID

data class DDay(
    val id: String = UUID.randomUUID().toString(),
    val labelTitle: String,
    val title: String,
    val date: String, // Format: "yyyy-MM-dd"
    val color: String, // Hex color code
    val excludeHolidays: Boolean = false,
    val selectedDays: List<String> = emptyList() // ["월", "화", ...]
)
