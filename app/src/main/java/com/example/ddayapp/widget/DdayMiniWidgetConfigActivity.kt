package com.example.ddayapp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation. clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx. compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import com.example.ddayapp.data.PrefsHelper
import com. example.ddayapp.ui.theme.DdayAppTheme
import com.example.ddayapp.ui.theme.toComposeColor
import com.example. ddayapp.utils.DateCalculator

class DdayMiniWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    companion object {
        private const val TAG = "MiniWidgetConfig"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?. extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager. INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log. d(TAG, "Widget ID: $appWidgetId")

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID, finishing")
            finish()
            return
        }

        setContent {
            DdayAppTheme {
                MiniWidgetConfigScreen(
                    onDdaySelected = { ddayId ->
                        Log. d(TAG, "D-day selected: $ddayId")
                        saveWidgetConfig(ddayId)
                        finishWithSuccess()
                    },
                    onCancel = {
                        Log. d(TAG, "Configuration cancelled")
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfig(ddayId: Long) {
        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putLong("widget_mini_${appWidgetId}_dday_id", ddayId)
            .apply()
        Log.d(TAG, "Saved mini widget config:  widget_mini_${appWidgetId}_dday_id = $ddayId")
    }

    private fun finishWithSuccess() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        DdayMiniWidgetProvider. updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity. RESULT_OK, resultValue)
        Log.d(TAG, "Finishing with success")
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniWidgetConfigScreen(
    onDdaySelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsHelper = remember { PrefsHelper(context) }
    val ddays = remember { prefsHelper. loadDDays() }
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
                        text = "미니 위젯 D-day 선택",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "취소",
                            tint = Color. White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF24a19c)
                )
            )
        }
    ) { paddingValues ->
        if (ddays.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📅", fontSize = 64.sp)
                Spacer(modifier = Modifier. height(16.dp))
                Text(
                    text = "등록된 D-day가 없습니다",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "앱에서 D-day를 먼저 추가해주세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement. spacedBy(12.dp)
            ) {
                items(ddays) { dday ->
                    val ddayText = try {
                        DateCalculator. calculateDDay(
                            targetDate = dday.date,
                            excludePublicHolidays = dday.excludePublicHolidays,
                            excludeCustomDays = dday.excludeCustomDays,
                            excludedWeekdays = dday.excludedWeekdays,
                            publicHolidays = publicHolidays,
                            customDays = customDays
                        )
                    } catch (e:  Exception) {
                        "D-Day"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDdaySelected(dday.id) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    . background(
                                        dday.color.toComposeColor(),
                                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = dday.labelTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = dday.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1C1F)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = ddayText,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dday.color. toComposeColor()
                                    )
                                    Text(
                                        text = dday.date,
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}