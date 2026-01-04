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
import java.util.Calendar
import androidx.compose.foundation.horizontalScroll

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

    /* 📅 DatePicker 상태 */
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val colors = listOf(
        "#24a19c", "#218efd", "#ff6b6b",
        "#a855f7", "#f59e0b", "#10b981"
    )

    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ===== 헤더 ===== */
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
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(24.dp))

                /* ===== 라벨 ===== */
                Text("라벨", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = labelTitle,
                    onValueChange = { labelTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("라벨을 입력하세요") },
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                /* ===== 제목 ===== */
                Text("제목", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("제목을 입력하세요") },
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                /* ===== 날짜 (달력 표시) ===== */
                Text("날짜", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true
                )

                /* 📅 달력 */
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = millis
                                    }
                                    date = String.format(
                                        "%04d-%02d-%02d",
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH) + 1,
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    )
                                }
                                showDatePicker = false
                            }) {
                                Text("확인")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("취소")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ---------- 공휴일 제외 ---------- */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = excludeHolidays,
                        onCheckedChange = { excludeHolidays = it }
                    )
                    Text(
                        text = "공휴일 제외 (주말 포함)",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(Modifier.height(16.dp))

                /* ===== 요일 선택 (가로 스크롤) ===== */
                Text("요일 선택", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEach { day ->
                        val isSelected = day in selectedDays

                        Button(
                            onClick = {
                                selectedDays = if (isSelected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isSelected -> "#24a19c".toComposeColor()
                                    else -> Color(0xFFF5F5F5)
                                },
                                contentColor = when {
                                    isSelected -> Color.White
                                    else -> Color(0xFF666666)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(
                                horizontal = 6.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Text(day, fontSize = 10.sp)
                        }
                    }
                }

                Text(
                    text = when {
                        selectedDays.isNotEmpty() && excludeHolidays ->
                            "선택한 요일만 카운트하며 공휴일은 제외됩니다"
                        selectedDays.isNotEmpty() ->
                            "선택된 요일만 카운트됩니다 (${selectedDays.joinToString(", ")})"
                        excludeHolidays ->
                            "공휴일을 제외하고 카운트합니다"
                        else ->
                            "모든 날짜를 포함합니다"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(16.dp))

                /* ===== 색상 ===== */
                Text("색상", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.toComposeColor())
                                .border(
                                    width = if (color == selectedColor) 2.dp else 0.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                /* ===== 저장 ===== */
                Button(
                    onClick = {
                        onSave(
                            DDay(
                                id = dday?.id ?: "",
                                labelTitle = labelTitle,
                                title = title,
                                date = date,
                                color = selectedColor,
                                excludeHolidays = excludeHolidays,
                                selectedDays = selectedDays
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedColor.toComposeColor()
                    )
                ) {
                    Text(
                        text = if (dday == null) "추가" else "수정",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

