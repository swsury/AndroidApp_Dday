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
import com.example.ddayapp.utils.DateCalculator
import android.util.Log
import android.graphics.Color

class DdayStyle1TransparentWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "Style1TransparentWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId:  Int
        ) {
            try {
                val prefsHelper = PrefsHelper(context)
                val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

                val ddayId = widgetPrefs.getLong("widget_style1_transparent_${appWidgetId}_dday_id", -1L)
                val views = RemoteViews(context.packageName, R.layout.widget_dday_style1_transparent)

                if (ddayId != -1L) {
                    val ddays = prefsHelper.loadDDays()
                    val dday = ddays.find { it.id == ddayId }
                    val settings = prefsHelper.loadSettings()

                    if (dday != null) {
                        val publicHolidays = settings.publicHolidays.map { it.date }.toSet()
                        val customDays = settings.customDays.map { it.date }.toSet()

                        val ddayText = DateCalculator.calculateDDay(
                            targetDate = dday.date,
                            excludePublicHolidays = dday.excludePublicHolidays,
                            excludeCustomDays = dday.excludeCustomDays,
                            excludedWeekdays = dday.excludedWeekdays,
                            publicHolidays = publicHolidays,
                            customDays = customDays
                        )

                        views.setTextViewText(R.id.widget_label, dday.labelTitle)
                        views.setTextViewText(R.id.widget_title, dday.title)
                        views.setTextViewText(R.id.widget_dday, ddayText)
                        views.setTextViewText(R.id.widget_date, dday.date)

                        // 🔥 텍스트 색상을 D-day 색상으로 변경
                        try {
                            val color = Color.parseColor(dday.color)
                            views.setTextColor(R.id.widget_label, color)  // 라벨 색상
                            views.setTextColor(R.id.widget_dday, color)   // D-day 숫자 색상
                            views.setTextColor(R.id.widget_title, color)  // 제목 색상 (선택사항)
                            views.setTextColor(R.id.widget_date, color) //날짜 색상
                        } catch (e: Exception) {
                            // 기본 색상 유지
                            views.setTextColor(R.id.widget_label, Color.parseColor("#24a19c"))
                            views.setTextColor(R.id.widget_dday, Color.parseColor("#24a19c"))
                        }

                        // 위젯 클릭 시 해당 D-day 편집 화면 열기
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("dday_id", dday.id)
                            putExtra("open_edit", true)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    } else {
                        setDefaultContent(views)
                        setDefaultClickIntent(context, views, appWidgetId)
                    }
                } else {
                    // 위젯 설정이 안 된 경우
                    setDefaultContent(views)
                    setDefaultClickIntent(context, views, appWidgetId)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget $appWidgetId", e)
            }
        }

        private fun setDefaultContent(views: RemoteViews) {
            views.setTextViewText(R.id.widget_label, "D-day")
            views.setTextViewText(R.id.widget_title, "위젯 설정")
            views.setTextViewText(R.id.widget_dday, "")
            views.setTextViewText(R.id.widget_date, "터치하여 설정")

            // 기본 텍스트 색상
            views.setTextColor(R.id.widget_label, Color.parseColor("#24a19c"))
            views.setTextColor(R.id.widget_dday, Color.parseColor("#24a19c"))
            views.setTextColor(R.id.widget_title, Color.parseColor("#666666"))
            views.setTextColor(R.id.widget_date, Color.parseColor("#24a19c"))
        }

        // 🔥 기본 클릭 시 메인 화면만 열기
        private fun setDefaultClickIntent(context: Context, views: RemoteViews, appWidgetId: Int) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId, // 고유한 requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, DdayStyle1TransparentWidgetProvider::class.java)
                )
                appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update all widgets", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds:  IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context:  Context, appWidgetIds: IntArray) {
        val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = widgetPrefs.edit()
        appWidgetIds.forEach { editor.remove("widget_style1_transparent_${it}_dday_id") }
        editor.apply()
    }

    override fun onEnabled(context:  Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget disabled")
    }
}