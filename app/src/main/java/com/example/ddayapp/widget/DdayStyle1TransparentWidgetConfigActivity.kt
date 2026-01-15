package com.example.ddayapp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ddayapp.ui.theme.DdayAppTheme

class DdayStyle1TransparentWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isReconfiguring = false // 🔥 재설정 모드 플래그
    
    companion object {
        private const val TAG = "Style1TransparentConfig"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 🔥 재설정 모드 확인 (이미 위젯이 홈화면에 있는 경우)
        isReconfiguring = intent?.flags?.and(Intent.FLAG_ACTIVITY_NEW_TASK) != 0

        setContent {
            DdayAppTheme {
                WidgetConfigScreen(
                    title = "1x1 미니 위젯 투명",
                    onDdaySelected = { ddayId ->
                        saveWidgetConfig(ddayId)
                        finishWithSuccess()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun saveWidgetConfig(ddayId: Long) {
        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putLong("widget_style1_transparent_${appWidgetId}_dday_id", ddayId)
            .apply()
        Log.d(TAG, "Saved: widget_style1_transparent_${appWidgetId}_dday_id = $ddayId")
    }

    private fun finishWithSuccess() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        DdayStyle1TransparentWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
