package com.example.ddayapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.ddayapp.MainActivity

class DdayWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = DdayWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds:  IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // 위젯 삭제 시 설정 제거
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        appWidgetIds.forEach { widgetId ->
            prefs. edit().remove("widget_dday_id_$widgetId").apply()
        }
    }

    companion object {
        /**
         * 위젯에 DDay ID 저장
         */
        fun saveDdayIdPref(context: Context, widgetId: Int, ddayId:  Long) {
            val prefs = context.getSharedPreferences("widget_prefs", Context. MODE_PRIVATE)
            prefs.edit().putLong("widget_dday_id_$widgetId", ddayId).apply()
        }

        /**
         * 모든 위젯 업데이트
         */
        fun updateAllWidgets(context:  Context) {
            val intent = Intent(context, DdayWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}