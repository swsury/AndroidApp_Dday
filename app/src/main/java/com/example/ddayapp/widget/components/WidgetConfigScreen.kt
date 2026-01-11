package com.example.ddayapp.widget. components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx. compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui. Alignment
import androidx.compose. ui.Modifier
import androidx. compose.ui.graphics.Color
import androidx.compose. ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import com.example.ddayapp.data.PrefsHelper
import com. example.ddayapp.ui. theme.BackgroundGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    onDdaySelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val prefsHelper = remember { PrefsHelper(context) }
    val ddays = remember { prefsHelper.loadDDays() }
    val settings = remember { prefsHelper.loadSettings() }

    val publicHolidays = remember {
        settings.publicHolidays.map { it.date }. toSet()
    }
    val customDays = remember {
        settings.customDays.map { it.date }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "위젯에 표시할 D-day 선택",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF24a19c),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        if (ddays.isEmpty()) {
            // 빈 상태
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier. height(16.dp))
                    Text(
                        text = "등록된 D-day가 없습니다",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onCancel) {
                        Text("닫기")
                    }
                }
            }
        } else {
            // DDay 리스트
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ddays) { dday ->
                    DdayWidgetConfigCard(
                        dday = dday,
                        publicHolidays = publicHolidays,
                        customDays = customDays,
                        onClick = { onDdaySelected(dday.id) }
                    )
                }
            }
        }
    }
}