package com.example.ddayapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.ddayapp.MainActivity
import com.example.ddayapp.R
import com.example.ddayapp.data.PrefsHelper
import com.example. ddayapp.utils.DateCalculator
import android.graphics.Color
import android.util.Log


class DdayMiniWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "DdayMiniWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId:  Int
        ) {
            try {
                val prefsHelper = PrefsHelper(context)
                val widgetPrefs = context. getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

                // 위젯에 설정된 D-day ID 가져오기
                val ddayId = widgetPrefs. getLong("widget_mini_${appWidgetId}_dday_id", -1L)

                val views = RemoteViews(context.packageName, R.layout. widget_dday_mini)

                if (ddayId != -1L) {
                    val ddays = prefsHelper.loadDDays()
                    val dday = ddays.find { it.id == ddayId }
                    val settings = prefsHelper.loadSettings()

                    if (dday != null) {
                        // D-day 계산
                        val publicHolidays = settings.publicHolidays.map { it.date }. toSet()
                        val customDays = settings.customDays.map { it.date }.toSet()

                        val ddayText = DateCalculator.calculateDDay(
                            targetDate = dday.date,
                            excludePublicHolidays = dday.excludePublicHolidays,
                            excludeCustomDays = dday.excludeCustomDays,
                            excludedWeekdays = dday.excludedWeekdays,
                            publicHolidays = publicHolidays,
                            customDays = customDays
                        )

                        views.setTextViewText(R. id.widget_dday, ddayText)
                        views. setTextViewText(R.id.widget_title, dday.title)

                        // 배경 색상 설정
                        try {
                            val color = Color.parseColor(dday.color)
                            views. setInt(R.id.widget_background, "setBackgroundColor", color)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse color: ${dday.color}", e)
                            views.setInt(R.id.widget_background, "setBackgroundColor", Color.parseColor("#24a19c"))
                        }
                    } else {
                        views.setTextViewText(R. id.widget_dday, "")
                        views.setTextViewText(R.id.widget_title, "삭제됨")
                    }
                } else {
                    views.setTextViewText(R.id.widget_dday, "")
                    views. setTextViewText(R.id.widget_title, "설정")
                }

                // 클릭 시 앱 열기
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)

            } catch (e:  Exception) {
                Log.e(TAG, "Failed to update widget $appWidgetId", e)
            }
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, DdayMiniWidgetProvider::class. java)
                )
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e:  Exception) {
                Log.e(TAG, "Failed to update all widgets", e)
            }
        }
    }

    override fun onUpdate(
        context:  Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetPrefs = context. getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = widgetPrefs.edit()
        appWidgetIds.forEach { appWidgetId ->
            editor. remove("widget_mini_${appWidgetId}_dday_id")
        }
        editor.apply()
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Mini Widget enabled")
    }

    override fun onDisabled(context:  Context) {
        super.onDisabled(context)
        Log.d(TAG, "Mini Widget disabled")
    }
}