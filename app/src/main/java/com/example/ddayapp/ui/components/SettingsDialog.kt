package com.example.ddayapp.ui. components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui. unit.dp
import androidx.compose. ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ddayapp.data.Holiday
import com.example. ddayapp.data.Settings
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
    onAddCustomDay: (Holiday) -> Unit,
    onRemoveCustomDay: (String) -> Unit
) {
    var publicHolidays by remember { mutableStateOf(settings.publicHolidays) }
    var customDays by remember { mutableStateOf(settings.customDays) }
    var sabbathDay by remember { mutableStateOf(settings.sabbathDay) }
    var newCustomDate by remember { mutableStateOf("") }
    var newCustomName by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    LaunchedEffect(settings) {
        publicHolidays = settings.publicHolidays
        customDays = settings. customDays
        sabbathDay = settings.sabbathDay
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults. cardColors(
                containerColor = Color. White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier. fillMaxWidth(),
                    horizontalArrangement = Arrangement. SpaceBetween,
                    verticalAlignment = Alignment. CenterVertically
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
                            tint = Color. Gray
                        )
                    }
                }

                Spacer(modifier = Modifier. height(24.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        . verticalScroll(rememberScrollState())
                ) {
                    // 🔥 공휴일 섹션 (자동)
                    Text(
                        text = "공휴일 (자동)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF24a19c)
                    )

                    Text(
                        text = "시스템 연도에 맞춰 자동으로 불러옵니다",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier. padding(top = 4.dp, bottom = 12.dp)
                    )

                    // 연도 선택 및 가져오기
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        horizontalArrangement = Arrangement. spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    selectedYear = it
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("연도", fontSize = 14.sp) },
                            placeholder = { Text("2026", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (selectedYear.length == 4) {
                                    onFetchHolidays(selectedYear)
                                }
                            },
                            enabled = ! isLoadingHolidays && selectedYear.length == 4,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF24a19c)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isLoadingHolidays) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("가져오기", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(16.dp))

                    // 공휴일 목록
                    if (publicHolidays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "불러온 공휴일이 없습니다",
                                fontSize = 14.sp,
                                color = Color(0xFFBDBDBD)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            publicHolidays
                                .filter { it.date.isNotBlank() }
                                .take(5)  // 최대 5개만 표시
                                .forEach { holiday ->
                                    val displayDate = DateCalculator.formatDisplayDate(holiday.date)

                                    Card(
                                        modifier = Modifier. fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF0F9F8)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement. SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = holiday.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF24a19c)
                                                )
                                                Text(
                                                    text = displayDate,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF666666)
                                                )
                                            }
                                        }
                                    }
                                }

                            if (publicHolidays.size > 5) {
                                Text(
                                    text = "외 ${publicHolidays.size - 5}개",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(32.dp))

                    // 🔥 안식일 섹션 (수동)
                    Text(
                        text = "안식일 (수동 추가)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )

                    Text(
                        text = "특정 날짜를 수동으로 추가하여 제외할 수 있습니다",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier. padding(top = 4.dp, bottom = 12.dp)
                    )

                    // 날짜 추가
                    Column(
                        modifier = Modifier. fillMaxWidth(),
                        verticalArrangement = Arrangement. spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCustomDate,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            label = { Text("날짜", fontSize = 14.sp) },
                            placeholder = { Text("yyyy-MM-dd", fontSize = 14.sp) },
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color. Black,
                                disabledBorderColor = Color. Gray
                            )
                        )

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val cal = Calendar.getInstance().apply {
                                                timeInMillis = millis
                                            }
                                            newCustomDate = String. format(
                                                "%04d-%02d-%02d",
                                                cal.get(Calendar.YEAR),
                                                cal.get(Calendar. MONTH) + 1,
                                                cal.get(Calendar. DAY_OF_MONTH)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCustomName,
                                onValueChange = { newCustomName = it },
                                modifier = Modifier. weight(1f),
                                label = { Text("내용", fontSize = 14.sp) },
                                placeholder = { Text("내용 (선택)", fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (newCustomDate.isNotBlank()) {
                                        val newDay = Holiday(
                                            date = newCustomDate,
                                            name = newCustomName. ifBlank { "안식일" }
                                        )
                                        onAddCustomDay(newDay)
                                        customDays = customDays + newDay
                                        newCustomDate = ""
                                        newCustomName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B6B)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("추가", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(16.dp))

                    // 안식일 목록
                    if (customDays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 안식일이 없습니다",
                                fontSize = 14.sp,
                                color = Color(0xFFBDBDBD)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customDays
                                . filter { it.date.isNotBlank() }
                                . forEach { holiday ->
                                    val displayDate = DateCalculator.formatDisplayDate(holiday.date)

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFF5F5)
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
                                                Text(
                                                    text = holiday.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF6B6B)
                                                )
                                                Text(
                                                    text = displayDate,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF666666)
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    onRemoveCustomDay(holiday.date)
                                                    customDays = customDays. filter { it.date != holiday.date }
                                                }
                                            ) {
                                                Text(
                                                    "삭제",
                                                    color = Color. Red,
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
                            publicHolidays = publicHolidays,
                            customDays = customDays,
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
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}