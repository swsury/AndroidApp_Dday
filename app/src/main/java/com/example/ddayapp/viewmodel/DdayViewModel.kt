package com.example.ddayapp.viewmodel

import android. app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com. example.ddayapp.data. DDay
import com.example. ddayapp.data.Holiday
import com.example.ddayapp.data.Settings
import com. example.ddayapp.utils. HolidayApi
import com.example.ddayapp.utils.PreferencesHelper
import kotlinx.coroutines.flow. MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines. launch
import java.util.Calendar

class DdayViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(application)

    private val _ddays = MutableStateFlow<List<DDay>>(emptyList())
    val ddays: StateFlow<List<DDay>> = _ddays.asStateFlow()

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _isLoadingHolidays = MutableStateFlow(false)
    val isLoadingHolidays: StateFlow<Boolean> = _isLoadingHolidays. asStateFlow()

    init {
        loadData()
        autoLoadCurrentYearHolidays()
    }

    /**
     * 데이터 로드
     */
    private fun loadData() {
        _ddays.value = prefsHelper.loadDDays()
        _settings.value = prefsHelper.loadSettings()
    }

    /**
     * 현재 연도의 공휴일 자동 로드
     */
    private fun autoLoadCurrentYearHolidays() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val currentSettings = _settings.value

        // 이미 올해 공휴일이 있는지 확인
        val hasCurrentYearHolidays = currentSettings. publicHolidays.any { holiday ->
            holiday.date. startsWith(currentYear)
        }

        // 올해 공휴일이 없으면 자동 로드
        if (!hasCurrentYearHolidays) {
            fetchPublicHolidaysFromApi(currentYear)
        }
    }

    /**
     * D-day 추가
     */
    fun addDDay(dday:  DDay) {
        val updatedList = _ddays.value + dday
        _ddays.value = updatedList
        prefsHelper.saveDDays(updatedList)
    }

    /**
     * D-day 업데이트
     */
    fun updateDDay(dday: DDay) {
        val updatedList = _ddays.value.map { existingDday ->
            if (existingDday.id == dday.id) dday else existingDday
        }
        _ddays.value = updatedList
        prefsHelper.saveDDays(updatedList)
    }

    /**
     * D-day 삭제
     */
    fun deleteDDay(id: Long) {
        val updatedList = _ddays.value.filterNot { dday -> dday.id == id }
        _ddays.value = updatedList
        prefsHelper. saveDDays(updatedList)
    }

    /**
     * 설정 업데이트
     */
    fun updateSettings(settings: Settings) {
        _settings.value = settings
        prefsHelper.saveSettings(settings)
    }

    /**
     * 안식일 추가 (수동)
     */
    fun addCustomDay(holiday: Holiday) {
        val currentSettings = _settings. value
        val updatedCustomDays = (currentSettings.customDays + holiday)
            .distinctBy { it.date }
            .sortedBy { it.date }
        val newSettings = currentSettings.copy(customDays = updatedCustomDays)
        updateSettings(newSettings)
    }

    /**
     * 안식일 삭제 (수동)
     */
    fun removeCustomDay(date:  String) {
        val currentSettings = _settings.value
        val updatedCustomDays = currentSettings.customDays.filter { holiday -> holiday.date != date }
        val newSettings = currentSettings.copy(customDays = updatedCustomDays)
        updateSettings(newSettings)
    }

    /**
     * API에서 공휴일 가져오기 (자동)
     */
    fun fetchPublicHolidaysFromApi(year: String, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoadingHolidays.value = true

            val result = HolidayApi. fetchHolidays(year)

            result.onSuccess { newHolidays ->
                val currentSettings = _settings.value
                val mergedHolidays = (currentSettings.publicHolidays + newHolidays)
                    .distinctBy { it.date }
                    . sortedBy { it.date }
                val newSettings = currentSettings.copy(publicHolidays = mergedHolidays)
                updateSettings(newSettings)

                onComplete?.invoke(true, "${year}년 공휴일 ${newHolidays.size}개를 추가했습니다.")
            }

            result.onFailure { exception ->
                onComplete?. invoke(false, "공휴일 정보를 가져오는데 실패했습니다:  ${exception.message}")
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