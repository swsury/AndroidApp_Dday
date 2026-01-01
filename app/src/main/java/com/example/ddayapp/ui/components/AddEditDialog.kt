package com.example.ddayapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ddayapp.data.DDay
import com.example.ddayapp.ui.theme.toComposeColor
import com.example.ddayapp.utils.DateCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDialog(
    dday: DDay?,
    onDismiss: () -> Unit,
    onSave: (DDay) -> Unit
) {
    var labelTitle by remember { mutableStateOf(dday?.labelTitle ?: "") }
    var title by remember { mutableStateOf(dday?.title ?: "") }
    var date by remember { mutableStateOf(dday?.date ?: DateCalculator.getTodayString()) }
    var selectedColor by remember { mutableStateOf(dday?.color ?: "#24a19c") }
    var excludeHolidays by remember { mutableStateOf(dday?.excludeHolidays ?: false) }
    var selectedDays by remember { mutableStateOf(dday?.selectedDays ?: emptyList()) }
    
    val colors = listOf(
        "#24a19c",
        "#218efd",
        "#ff6b6b",
        "#a855f7",
        "#f59e0b",
        "#10b981"
    )
    
    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (dday == null) "새 D-day" else "D-day 편집",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 라벨
                Text(
                    text = "라벨",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = labelTitle,
                    onValueChange = { labelTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("라벨을 입력하세요") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 제목
                Text(
                    text = "제목",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("제목을 입력하세요") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 날짜
                Text(
                    text = "날짜",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 공휴일 제외
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        excludeHolidays = !excludeHolidays
                        if (excludeHolidays) {
                            selectedDays = emptyList()
                        }
                    }
                ) {
                    Checkbox(
                        checked = excludeHolidays,
                        onCheckedChange = { 
                            excludeHolidays = it
                            if (it) {
                                selectedDays = emptyList()
                            }
                        }
                    )
                    Text(
                        text = "공휴일 제외 (주말 제외)",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 요일 선택
                Text(
                    text = "요일 선택",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEach { day ->
                        val isSelected = day in selectedDays
                        val isEnabled = !excludeHolidays
                        
                        Button(
                            onClick = {
                                if (isEnabled) {
                                    selectedDays = if (isSelected) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    !isEnabled -> Color(0xFFE0E0E0)
                                    isSelected -> "#24a19c".toComposeColor()
                                    else -> Color(0xFFF5F5F5)
                                },
                                contentColor = when {
                                    !isEnabled -> Color(0xFFBDBDBD)
                                    isSelected -> Color.White
                                    else -> Color(0xFF666666)
                                }
                            ),
                            enabled = isEnabled,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = day,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Text(
                    text = when {
                        excludeHolidays -> "공휴일 제외 옵션이 선택되어 있습니다"
                        selectedDays.isNotEmpty() -> "선택된 요일만 카운트됩니다 (${selectedDays.joinToString(", ")})"
                        else -> "선택 안 함 시 모든 날짜를 포함합니다"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 색상 선택
                Text(
                    text = "색상",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.toComposeColor())
                                .border(
                                    width = if (color == selectedColor) 2.dp else 0.dp,
                                    color = Color(0xFF1B1C1F),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 저장 버튼
                Button(
                    onClick = {
                        if (labelTitle.isNotBlank() && title.isNotBlank() && date.isNotBlank()) {
                            val newDDay = DDay(
                                id = dday?.id ?: "",
                                labelTitle = labelTitle,
                                title = title,
                                date = date,
                                color = selectedColor,
                                excludeHolidays = excludeHolidays,
                                selectedDays = if (selectedDays.isNotEmpty()) selectedDays else emptyList()
                            )
                            onSave(newDDay)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedColor.toComposeColor()
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (dday == null) "추가" else "수정",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
