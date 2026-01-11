package com.example.ddayapp. widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.glance.appwidget.updateAll
import com.example.ddayapp.ui.theme.DdayAppTheme
import com.example.ddayapp.widget.components.WidgetConfigScreen
import kotlinx.coroutines. CoroutineScope
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.launch

class DdayWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 위젯 ID 가져오기
        appWidgetId = intent?. extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager. INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // 유효하지 않은 ID면 종료
        if (appWidgetId == AppWidgetManager. INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 기본 결과를 CANCELED로 설정
        setResult(RESULT_CANCELED)

        setContent {
            DdayAppTheme {
                WidgetConfigScreen(
                    onDdaySelected = { ddayId ->
                        configureWidget(ddayId)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun configureWidget(ddayId: Long) {
        // 위젯 설정 저장
        DdayWidgetReceiver.saveDdayIdPref(this, appWidgetId, ddayId)

        // Glance 위젯 업데이트
        CoroutineScope(Dispatchers.IO).launch {  // 🔥 수정: 타입 명시
            DdayWidget().updateAll(this@DdayWidgetConfigActivity)
        }

        // 결과 반환
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}