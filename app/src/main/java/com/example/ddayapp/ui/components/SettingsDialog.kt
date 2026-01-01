package com.example.ddayapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ddayapp.data.Holiday
import com.example.ddayapp.data.Settings
import com.example.ddayapp.utils.DateCalculator
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: Settings,
    isLoadingHolidays: Boolean,
    onDismiss: () -> Unit,
    onSave: (Settings) -> Unit,
    onFetchHolidays: (String) -> Unit,
    onAddHoliday: (Holiday) -> Unit,
    onRemoveHoliday: (String) -> Unit
) {
    var holidays by remember { mutableStateOf(settings.holidays) }
    var sabbathDay by remember { mutableStateOf(settings.sabbathDay) }
    var newHolidayDate by remember { mutableStateOf("") }
    var newHolidayName by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    LaunchedEffect(settings) {
        holidays = settings.holidays
        sabbathDay = settings.sabbathDay
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "휴무일 설정",
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

                Text(
                    text = "추가한 휴무일은 D-day 계산에서 제외됩니다",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 공휴일 설정 섹션
                    Text(
                        text = "공휴일 등록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )


                    // API 자동 추가 섹션
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "한국 공휴일 자동 추가",
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 연도 선택
                                var expanded by remember { mutableStateOf(false) }
                                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                val years = (currentYear until currentYear + 5).map { it.toString() }

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = "${selectedYear}년",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        years.forEach { year ->
                                            DropdownMenuItem(
                                                text = { Text("${year}년") },
                                                onClick = {
                                                    selectedYear = year
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onFetchHolidays(selectedYear) },
                                    enabled = !isLoadingHolidays,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF24a19c)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isLoadingHolidays) "로딩중..." else "가져오기",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 안식일 설정
                    Text(
                        text = "안식일 설정",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 수동 추가 섹션 - 날짜와 내용 입력
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newHolidayDate,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            placeholder = { Text("날짜 선택", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newHolidayName,
                                onValueChange = { newHolidayName = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("내용 (선택)", fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (newHolidayDate.isNotBlank()) {
                                        val newHoliday = Holiday(
                                            date = newHolidayDate,
                                            name = newHolidayName.ifBlank { "" }
                                        )
                                        onAddHoliday(newHoliday)
                                        holidays = holidays + newHoliday
                                        newHolidayDate = ""
                                        newHolidayName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF24a19c)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("추가", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 휴무 날짜 리스트
                    Text(
                        text = "휴무일 목록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (holidays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 제외 날짜가 없습니다",
                                fontSize = 14.sp,
                                color = Color(0xFFBDBDBD)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            holidays
                                .filter { it.date.isNotBlank() }
                                .forEach { holiday ->
                                    val displayDate = DateCalculator.formatDisplayDate(holiday.date)

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFAFAFA)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (holiday.name.isNotBlank()) {
                                                    Text(
                                                        text = holiday.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF24a19c)
                                                    )
                                                }
                                                Text(
                                                    text = displayDate,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF666666)
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    onRemoveHoliday(holiday.date)
                                                    holidays =
                                                        holidays.filter { it.date != holiday.date }
                                                }
                                            ) {
                                                Text(
                                                    "삭제",
                                                    color = Color.Red,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 저장 버튼
                Button(
                    onClick = {
                        onSave(Settings(
                            holidays = holidays,
                            sabbathDay = sabbathDay
                        ))
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF24a19c)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "저장",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            newHolidayDate = String.format("%04d-%02d-%02d", year, month, day)
                        }
                        showDatePicker = false
                    }
                ) {
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
}