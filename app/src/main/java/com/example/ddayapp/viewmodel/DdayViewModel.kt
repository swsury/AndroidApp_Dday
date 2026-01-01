package com.example.ddayapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddayapp.data.DDay
import com.example.ddayapp.data.Holiday
import com.example.ddayapp.data.Settings
import com.example.ddayapp.utils.HolidayApi
import com.example.ddayapp.utils.PreferencesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DdayViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsHelper = PreferencesHelper(application)
    
    private val _ddays = MutableStateFlow<List<DDay>>(emptyList())
    val ddays: StateFlow<List<DDay>> = _ddays.asStateFlow()
    
    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()
    
    private val _isLoadingHolidays = MutableStateFlow(false)
    val isLoadingHolidays: StateFlow<Boolean> = _isLoadingHolidays.asStateFlow()
    
    init {
        loadData()
    }
    
    /**
     * 데이터 로드
     */
    private fun loadData() {
        _ddays.value = prefsHelper.loadDDays()
        _settings.value = prefsHelper.loadSettings()
    }
    
    /**
     * D-day 추가
     */
    fun addDDay(dday: DDay) {
        val updatedList = _ddays.value + dday
        _ddays.value = updatedList
        prefsHelper.saveDDays(updatedList)
    }
    
    /**
     * D-day 업데이트
     */
    fun updateDDay(dday: DDay) {
        val updatedList = _ddays.value.map {
            if (it.id == dday.id) dday else it
        }
        _ddays.value = updatedList
        prefsHelper.saveDDays(updatedList)
    }
    
    /**
     * D-day 삭제
     */
    fun deleteDDay(id: String) {
        val updatedList = _ddays.value.filter { it.id != id }
        _ddays.value = updatedList
        prefsHelper.saveDDays(updatedList)
    }
    
    /**
     * 설정 업데이트
     */
    fun updateSettings(settings: Settings) {
        _settings.value = settings
        prefsHelper.saveSettings(settings)
    }
    
    /**
     * 공휴일 추가
     */
    fun addHoliday(holiday: Holiday) {
        val currentSettings = _settings.value
        val updatedHolidays = (currentSettings.holidays + holiday)
            .distinctBy { it.date }
            .sortedBy { it.date }
        val newSettings = currentSettings.copy(holidays = updatedHolidays)
        updateSettings(newSettings)
    }
    
    /**
     * 공휴일 삭제
     */
    fun removeHoliday(date: String) {
        val currentSettings = _settings.value
        val updatedHolidays = currentSettings.holidays.filter { it.date != date }
        val newSettings = currentSettings.copy(holidays = updatedHolidays)
        updateSettings(newSettings)
    }
    
    /**
     * API에서 공휴일 가져오기
     */
    fun fetchHolidaysFromApi(year: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoadingHolidays.value = true
            
            val result = HolidayApi.fetchHolidays(year)
            
            result.onSuccess { newHolidays ->
                val currentSettings = _settings.value
                val mergedHolidays = (currentSettings.holidays + newHolidays)
                    .distinctBy { it.date }
                    .sortedBy { it.date }
                val newSettings = currentSettings.copy(holidays = mergedHolidays)
                updateSettings(newSettings)
                
                onComplete(true, "${year}년 공휴일 ${newHolidays.size}개를 추가했습니다.")
            }
            
            result.onFailure { exception ->
                onComplete(false, "공휴일 정보를 가져오는데 실패했습니다: ${exception.message}")
            }
            
            _isLoadingHolidays.value = false
        }
    }
    
    /**
     * 모든 데이터 삭제
     */
    fun clearAllData() {
        _ddays.value = emptyList()
        _settings.value = Settings()
        prefsHelper.clearAll()
    }
}
