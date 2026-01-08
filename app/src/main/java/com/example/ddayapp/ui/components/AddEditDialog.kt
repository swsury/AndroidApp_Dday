package com.example.ddayapp. ui.components

import androidx.compose.foundation.background
import androidx. compose.foundation. border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose. foundation.verticalScroll
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ddayapp.data.DDay
import com.example.ddayapp. ui.theme.toComposeColor
import com.example.ddayapp.utils.DateCalculator
import java. util. Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDialog(
    dday: DDay?,
    publicHolidays:  Set<String>,  // 🔥 공휴일 데이터
    customDays: Set<String>,      // 🔥 안식일 데이터
    onDismiss: () -> Unit,
    onSave: (DDay) -> Unit
) {
    var labelTitle by remember { mutableStateOf(dday?.labelTitle ?: "") }
    var title by remember { mutableStateOf(dday?. title ?: "") }
    var date by remember { mutableStateOf(dday?.date ?: DateCalculator.getTodayString()) }
    var selectedColor by remember { mutableStateOf(dday?.color ?: "#24a19c") }
    var excludePublicHolidays by remember { mutableStateOf(dday?.excludePublicHolidays ?: false) }
    var excludeCustomDays by remember { mutableStateOf(dday?.excludeCustomDays ?: false) }  // 🔥 추가
    var excludeWeekends by remember { mutableStateOf(dday?.excludeWeekends ?:  false) }

    /* 📅 DatePicker 상태 */
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val colors = listOf(
        "#24a19c", "#218efd", "#ff6b6b",
        "#a855f7", "#f59e0b", "#10b981"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    . padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ===== 헤더 ===== */
                Row(
                    modifier = Modifier. fillMaxWidth(),
                    horizontalArrangement = Arrangement. SpaceBetween,
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Text(
                        text = if (dday == null) "새 D-day" else "D-day 편집",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default. Close, contentDescription = "닫기")
                    }
                }

                Spacer(Modifier.height(24.dp))

                /* ===== 라벨 ===== */
                Text("라벨", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier. height(8.dp))
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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color. Gray
                    )
                )

                /* 📅 달력 */
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?. let { millis ->
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = millis
                                    }
                                    date = String.format(
                                        "%04d-%02d-%02d",
                                        cal. get(Calendar.YEAR),
                                        cal.get(Calendar. MONTH) + 1,
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
                Row(
                    verticalAlignment = Alignment. CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = excludePublicHolidays,
                        onCheckedChange = { excludePublicHolidays = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "공휴일 제외 (${publicHolidays.size}개)",
                        fontSize = 14.sp,
                        color = Color(0xFF24a19c),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                /* ---------- 안식일 제외 ---------- */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = excludeCustomDays,
                        onCheckedChange = { excludeCustomDays = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "안식일 제외 (${customDays.size}개)",
                        fontSize = 14.sp,
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                /* ---------- 주말 제외 ---------- */
                Row(
                    verticalAlignment = Alignment. CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = excludeWeekends,
                        onCheckedChange = { excludeWeekends = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "주말 제외 (토, 일)",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }

                Text(
                    text = when {
                        excludePublicHolidays && excludeCustomDays && excludeWeekends ->
                            "공휴일, 안식일, 주말을 제외하고 카운트합니다"
                        excludePublicHolidays && excludeCustomDays ->
                            "공휴일과 안식일을 제외하고 카운트합니다"
                        excludePublicHolidays && excludeWeekends ->
                            "공휴일과 주말을 제외하고 카운트합니다"
                        excludeCustomDays && excludeWeekends ->
                            "안식일과 주말을 제외하고 카운트합니다"
                        excludePublicHolidays ->
                            "공휴일을 제외하고 카운트합니다"
                        excludeCustomDays ->
                            "안식일을 제외하고 카운트합니다"
                        excludeWeekends ->
                            "주말을 제외하고 카운트합니다"
                        else ->
                            "모든 날짜를 포함합니다"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                )

                Spacer(Modifier.height(16.dp))

                /* ===== 색상 ===== */
                Text("색상", fontSize = 14.sp, color = Color(0xFF666666))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color. toComposeColor())
                                .border(
                                    width = if (color == selectedColor) 3.dp else 0.dp,
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
                        if (title.isBlank()) {
                            return@Button
                        }

                        onSave(
                            DDay(
                                id = dday?.id ?: System.currentTimeMillis(),
                                labelTitle = labelTitle,
                                title = title,
                                date = date,
                                color = selectedColor,
                                excludePublicHolidays = excludePublicHolidays,
                                excludeCustomDays = excludeCustomDays,  // 🔥 추가
                                excludeWeekends = excludeWeekends,
                                publicHolidays = publicHolidays,  // 🔥 수정
                                customDays = customDays           // 🔥 수정
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
                        color = Color. White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}