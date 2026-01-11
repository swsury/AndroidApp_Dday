package com.example.ddayapp.widget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime. Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui. unit.dp
import androidx.compose. ui.unit.sp
import com.example.ddayapp.data.DDay
import com.example.ddayapp.ui.theme.toComposeColor
import com.example.ddayapp.utils.DateCalculator

@Composable
fun DdayWidgetConfigCard(
    dday:  DDay,
    publicHolidays: Set<String>,
    customDays: Set<String>,
    onClick: () -> Unit
) {
    val cardColor = dday.color.toComposeColor()

    val ddayText = DateCalculator.calculateDDay(
        targetDate = dday.date,
        excludePublicHolidays = dday.excludePublicHolidays,
        excludeCustomDays = dday.excludeCustomDays,
        excludedWeekdays = dday. excludedWeekdays,
        publicHolidays = publicHolidays,
        customDays = customDays
    )

    val formattedDate = DateCalculator.formatDate(dday.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults. cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        cardColor,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment. CenterStart
            ) {
                Text(
                    text = dday.labelTitle,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            // 내용
            Column(
                modifier = Modifier. padding(16.dp),
                verticalArrangement = Arrangement. spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dday.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Text(
                        text = ddayText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight. Bold,
                        color = cardColor
                    )
                }

                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}