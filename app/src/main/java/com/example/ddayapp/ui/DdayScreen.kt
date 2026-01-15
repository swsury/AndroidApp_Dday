package com.example.ddayapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddayapp.data.DDay
import com.example.ddayapp.ui.components.*
import com.example.ddayapp.ui.theme.BackgroundGray
import com.example.ddayapp.viewmodel.DdayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DdayScreen(
    viewModel: DdayViewModel = viewModel(),
    initialDdayIdToEdit: Long?  = null
) {
    val ddays by viewModel.ddays.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoadingHolidays by viewModel.isLoadingHolidays.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var editingDday by remember { mutableStateOf<DDay?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // 🔥 드래그 상태
    var draggingGroupIndex by remember { mutableStateOf<Int?>(null) }
    var draggingCardId by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // 🔥 각 그룹의 펼침/접힘 상태 관리
    var expandedGroups by remember { mutableStateOf<Set<String>>(emptySet()) }

    val snackbarHostState = remember { SnackbarHostState() }

    // 실시간 공휴일/안식일 데이터
    val publicHolidays = remember(settings.publicHolidays) {
        settings.publicHolidays.map { it.date }.toSet()
    }
    val customDays = remember(settings.customDays) {
        settings.customDays.map { it.date }.toSet()
    }

    // 🔥 labelTitle로 그룹화 및 정렬 가능한 그룹 순서
    val groupedDdays = remember(ddays) {
        ddays.groupBy { it.labelTitle }
    }

    var groupOrder by remember { mutableStateOf(groupedDdays.keys.toList()) }

    // ddays가 변경되면 groupOrder 업데이트 (새 그룹 추가 시)
    LaunchedEffect(ddays) {
        val currentGroups = ddays.map { it.labelTitle }.distinct()
        val newGroups = currentGroups.filterNot { it in groupOrder }
        groupOrder = groupOrder.filter { it in currentGroups } + newGroups
    }

    // 🔥 위젯에서 전달된 D-day 편집 열기
    LaunchedEffect(initialDdayIdToEdit, ddays) {
        if (initialDdayIdToEdit != null && initialDdayIdToEdit != -1L) {
            val ddayToEdit = ddays.find { it.id == initialDdayIdToEdit }
            if (ddayToEdit != null) {
                editingDday = ddayToEdit
                showAddDialog = true
            }
        }
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
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.topAppBarColors(
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
                    contentDescription = "��가"
                )
            }
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ddays.isEmpty()) {
                // 빈 상태
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📅", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "D-day가 없습니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ 버튼을 눌러 새로운 D-day를 추가하세요",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            } else {
                // 🔥 그룹화된 D-day 리스트
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = groupOrder,
                        key = { _, label -> "group_$label" }
                    ) { groupIndex, labelTitle ->
                        val ddaysInGroup = groupedDdays[labelTitle] ?: emptyList()
                        val isExpanded = expandedGroups.contains(labelTitle)
                        val isDraggingGroup = draggingGroupIndex == groupIndex

                        // 🔥 드래그 가능한 그룹 헤더
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    if (isDraggingGroup) {
                                        translationY = dragOffset.y
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                        shadowElevation = 12.dp.toPx()
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingGroupIndex = groupIndex
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += Offset(0f, dragAmount.y)

                                            val itemHeight = 80.dp.toPx()
                                            val currentOffset = dragOffset.y
                                            val movement = (currentOffset / itemHeight).toInt()

                                            if (movement != 0) {
                                                val newIndex = (groupIndex + movement)
                                                    .coerceIn(0, groupOrder.size - 1)
                                                if (newIndex != groupIndex) {
                                                    val newOrder = groupOrder.toMutableList()
                                                    val item = newOrder.removeAt(groupIndex)
                                                    newOrder.add(newIndex, item)
                                                    groupOrder = newOrder
                                                    draggingGroupIndex = newIndex
                                                    dragOffset = Offset.Zero
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingGroupIndex = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingGroupIndex = null
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedGroups = if (isExpanded) {
                                            expandedGroups - labelTitle
                                        } else {
                                            expandedGroups + labelTitle
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF24a19c).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "드래그",
                                            tint = Color(0xFF24a19c),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = labelTitle,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF24a19c)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${ddaysInGroup.size}개",
                                            fontSize = 14.sp,
                                            color = Color(0xFF999999)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded)
                                                Icons.Default.KeyboardArrowUp
                                            else
                                                Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "접기" else "펼치기",
                                            tint = Color(0xFF24a19c)
                                        )
                                    }
                                }
                            }
                        }

                        // 펼쳐진 경우에만 아이템 표시
                        if (isExpanded) {
                            ddaysInGroup.forEachIndexed { _, dday ->
                                val isDraggingCard = draggingCardId == dday.id

                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .graphicsLayer {
                                            if (isDraggingCard) {
                                                translationY = dragOffset.y
                                                scaleX = 1.05f
                                                scaleY = 1.05f
                                                shadowElevation = 8.dp.toPx()
                                                alpha = 0.8f
                                            }
                                        }
                                        .pointerInput(Unit) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingCardId = dday.id
                                                    dragOffset = Offset.Zero
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset += Offset(0f, dragAmount.y)

                                                    // 🔥 다른 그룹으로 이동 감지
                                                    val cardHeight = 140.dp.toPx()
                                                    val currentOffset = dragOffset.y

                                                    // 위쪽으로 드래그 (이전 그룹으로)
                                                    if (currentOffset < -cardHeight) {
                                                        val currentGroupIndex = groupOrder.indexOf(labelTitle)
                                                        if (currentGroupIndex > 0) {
                                                            val targetGroup = groupOrder[currentGroupIndex - 1]
                                                            viewModel.moveDdayToGroup(dday.id, targetGroup)
                                                            dragOffset = Offset.Zero
                                                            draggingCardId = null
                                                            snackbarMessage = "'$targetGroup' 그룹으로 이동했습니다"
                                                            showSnackbar = true
                                                        }
                                                    }
                                                    // 아래쪽으로 드래그 (다음 그룹으로)
                                                    else if (currentOffset > cardHeight) {
                                                        val currentGroupIndex = groupOrder.indexOf(labelTitle)
                                                        if (currentGroupIndex < groupOrder.size - 1) {
                                                            val targetGroup = groupOrder[currentGroupIndex + 1]
                                                            viewModel.moveDdayToGroup(dday.id, targetGroup)
                                                            dragOffset = Offset.Zero
                                                            draggingCardId = null
                                                            snackbarMessage = "'$targetGroup' 그룹으로 이동했습니다"
                                                            showSnackbar = true
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggingCardId = null
                                                    dragOffset = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    draggingCardId = null
                                                    dragOffset = Offset.Zero
                                                }
                                            )
                                        }
                                ) {
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
                                            viewModel.addDDay(duplicatedDday)
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
                            }
                        }
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
                    viewModel.updateDDay(dday)
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