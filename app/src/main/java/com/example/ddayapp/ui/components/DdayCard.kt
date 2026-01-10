package com.example.ddayapp. ui. components

import androidx.compose.foundation. background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled. MoreHoriz
import androidx.compose. material3.*
import androidx.compose.runtime.*
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
fun DdayCard(
    dday: DDay,
    publicHolidays: Set<String>,
    customDays: Set<String>,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,  // 🔥 복제 콜백 추가
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardColor = dday.color. toComposeColor()

    val ddayText = DateCalculator.calculateDDay(
        targetDate = dday. date,
        excludePublicHolidays = dday.excludePublicHolidays,
        excludeCustomDays = dday.excludeCustomDays,
        excludedWeekdays = dday.excludedWeekdays,  // 🔥 변경
        publicHolidays = publicHolidays,
        customDays = customDays
    )

    val formattedDate = DateCalculator.formatDate(dday.date)

    Card(
        modifier = modifier. fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults. cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            /* 상단 컬러 바 */
            Box(
                modifier = Modifier
                    . fillMaxWidth()
                    . height(36.dp)
                    .background(
                        cardColor,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dday.labelTitle,
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier. size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default. MoreHoriz,
                                contentDescription = "메뉴",
                                tint = Color. White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("편집") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            // 🔥 복제 메뉴 추가
                            DropdownMenuItem(
                                text = { Text("복제") },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("삭제", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            /* 카드 내용 */
            Column(
                modifier = Modifier. padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        fontWeight = FontWeight.Bold,
                        color = cardColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier. size(14.dp)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}