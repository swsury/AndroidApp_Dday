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
import android.graphics.Color
import android.util.Log

class DdayStyle1WidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "Style1Widget"
        
        fun updateAppWidget(
            context:  Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val prefsHelper = PrefsHelper(context)
                val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                
                val ddayId = widgetPrefs.getLong("widget_style1_${appWidgetId}_dday_id", -1L)
                val views = RemoteViews(context. packageName, R.layout.widget_dday_style1)
                
                if (ddayId != -1L) {
                    val ddays = prefsHelper.loadDDays()
                    val dday = ddays.find { it.id == ddayId }
                    val settings = prefsHelper.loadSettings()
                    
                    if (dday != null) {
                        val publicHolidays = settings.publicHolidays.map { it.date }.toSet()
                        val customDays = settings. customDays.map { it. date }.toSet()
                        
                        val ddayText = DateCalculator.calculateDDay(
                            targetDate = dday.date,
                            excludePublicHolidays = dday.excludePublicHolidays,
                            excludeCustomDays = dday.excludeCustomDays,
                            excludedWeekdays = dday.excludedWeekdays,
                            publicHolidays = publicHolidays,
                            customDays = customDays
                        )
                        
                        views.setTextViewText(R.id.widget_label, dday.labelTitle)
                        views.setTextViewText(R.id. widget_title, dday.title)
                        views.setTextViewText(R. id.widget_dday, ddayText)
                        views. setTextViewText(R.id.widget_date, dday.date)
                        
                        try {
                            val color = Color. parseColor(dday.color)
                            views.setInt(R.id.widget_background, "setBackgroundColor", color)
                        } catch (e:  Exception) {
                            views.setInt(R.id. widget_background, "setBackgroundColor", Color.parseColor("#24a19c"))
                        }
                    } else {
                        setDefaultContent(views)
                    }
                } else {
                    setDefaultContent(views)
                }
                
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent. FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views. setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                Log. e(TAG, "Failed to update widget $appWidgetId", e)
            }
        }
        
        private fun setDefaultContent(views: RemoteViews) {
            views.setTextViewText(R. id.widget_label, "D-day")
            views.setTextViewText(R.id.widget_title, "위젯 설정")
            views.setTextViewText(R.id.widget_dday, "")
            views.setTextViewText(R.id.widget_date, "터치하여 설정")
        }
        
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager. getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, DdayStyle1WidgetProvider::class.java)
                )
                appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update all widgets", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager:  AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = widgetPrefs.edit()
        appWidgetIds.forEach { editor.remove("widget_style1_${it}_dday_id") }
        editor.apply()
    }
}