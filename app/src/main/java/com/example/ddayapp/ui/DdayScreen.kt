package com.example.ddayapp. ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation. lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui. unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddayapp.data.DDay
import com.example.ddayapp.ui.components.*
import com.example.ddayapp.ui.theme.BackgroundGray
import com.example.ddayapp.viewmodel.DdayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DdayScreen(
    viewModel:  DdayViewModel = viewModel()
) {
    val ddays by viewModel.ddays.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoadingHolidays by viewModel.isLoadingHolidays.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var editingDday by remember { mutableStateOf<DDay? >(null) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // 실시간 공휴일/안식일 데이터
    val publicHolidays = remember(settings.publicHolidays) {
        settings.publicHolidays.map { it. date }. toSet()
    }
    val customDays = remember(settings.customDays) {
        settings.customDays.map { it.date }.toSet()
    }

    // Snackbar 표시
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(snackbarMessage)
            showSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "D-day",
                        fontSize = 24.sp,
                        fontWeight = FontWeight. Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults. topAppBarColors(
                    containerColor = Color(0xFF24a19c)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingDday = null
                    showAddDialog = true
                },
                containerColor = Color(0xFF24a19c),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "추가"
                )
            }
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                . fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ddays. isEmpty()) {
                // 빈 상태
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📅",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier. height(16.dp))

                    Text(
                        text = "D-day가 없습니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1F)
                    )

                    Spacer(modifier = Modifier. height(8.dp))

                    Text(
                        text = "+ 버튼을 눌러 새로운 D-day를 추가하세요",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            } else {
                // D-day 리스트
                LazyColumn(
                    modifier = Modifier. fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = ddays.size,  // 🔥 items 대신 count 사용
                        key = { index -> ddays[index].id }
                    ) { index ->
                        val dday = ddays[index]
                        DdayCard(
                            dday = dday,
                            publicHolidays = publicHolidays,
                            customDays = customDays,
                            onEdit = {
                                editingDday = dday
                                showAddDialog = true
                            },
                            onDuplicate = {
                                val duplicatedDday = dday.copy(
                                    id = System.currentTimeMillis(),
                                    title = "${dday.title} (복사)"
                                )
                                viewModel. addDDay(duplicatedDday)
                                snackbarMessage = "복제되었습니다"
                                showSnackbar = true
                            },
                            onDelete = {
                                viewModel.deleteDDay(dday.id)
                                snackbarMessage = "삭제되었습니다"
                                showSnackbar = true
                            }
                        )
                    }

                    // 하단 여백
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // AddEditDialog
    if (showAddDialog) {
        AddEditDialog(
            dday = editingDday,
            publicHolidays = publicHolidays,
            customDays = customDays,
            onDismiss = {
                showAddDialog = false
                editingDday = null
            },
            onSave = { dday ->
                if (editingDday == null) {
                    viewModel.addDDay(dday)
                    snackbarMessage = "추가되었습니다"
                } else {
                    viewModel. updateDDay(dday)
                    snackbarMessage = "수정되었습니다"
                }
                showSnackbar = true
                showAddDialog = false
                editingDday = null
            }
        )
    }

    // SettingsDialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            isLoadingHolidays = isLoadingHolidays,
            onDismiss = {
                showSettingsDialog = false
            },
            onSave = { newSettings ->
                viewModel.updateSettings(newSettings)
                snackbarMessage = "설정이 저장되었습니다"
                showSnackbar = true
            },
            onAddCustomDay = { holiday ->
                viewModel.addCustomDay(holiday)
            },
            onRemoveCustomDay = { date ->
                viewModel.removeCustomDay(date)
            }
        )
    }
}