package com.example.ddayapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.ddayapp.data.DDay
import com.example.ddayapp.data.Settings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("dday_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_DDAYS = "ddays"
        private const val KEY_SETTINGS = "settings"
    }
    
    /**
     * D-day 리스트 저장
     */
    fun saveDDays(ddays: List<DDay>) {
        val json = gson.toJson(ddays)
        prefs.edit().putString(KEY_DDAYS, json).apply()
    }
    
    /**
     * D-day 리스트 로드
     */
    fun loadDDays(): List<DDay> {
        val json = prefs.getString(KEY_DDAYS, null) ?: return emptyList()
        val type = object : TypeToken<List<DDay>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 설정 저장
     */
    fun saveSettings(settings: Settings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_SETTINGS, json).apply()
    }
    
    /**
     * 설정 로드
     */
    fun loadSettings(): Settings {
        val json = prefs.getString(KEY_SETTINGS, null) ?: return Settings()
        return try {
            gson.fromJson(json, Settings::class.java) ?: Settings()
        } catch (e: Exception) {
            Settings()
        }
    }
    
    /**
     * 모든 데이터 삭제
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
