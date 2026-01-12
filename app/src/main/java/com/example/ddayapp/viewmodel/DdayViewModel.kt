package com.example.ddayapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddayapp.utils.HolidayApi
import com.example.ddayapp.data.DDay
import com.example.ddayapp.data.Holiday
import com.example.ddayapp.data.PrefsHelper
import com.example.ddayapp.data.Settings
import com.example.ddayapp.widget.DdayWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow. StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DdayViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsHelper = PrefsHelper(application)
    private val context = application

    private val _ddays = MutableStateFlow<List<DDay>>(emptyList())
    val ddays: StateFlow<List<DDay>> = _ddays

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings

    private val _isLoadingHolidays = MutableStateFlow(false)
    val isLoadingHolidays: StateFlow<Boolean> = _isLoadingHolidays

    init {
        loadData()
        autoLoadCurrentYearHolidays()
    }

    /**
     * 데이터 로드
     */
    private fun loadData() {
        _ddays.value = prefsHelper. loadDDays()
        _settings.value = prefsHelper.loadSettings()
        checkAndUpdateYearHolidays()
    }

    /**
     * D-day 추가
     */
    fun addDDay(dday: DDay) {
        val currentList = _ddays.value
        // 새 아이템은 맨 뒤에 추가
        val newOrder = (currentList.maxOfOrNull { it.order } ?: -1) + 1
        val newDDay = dday.copy(order = newOrder)

        _ddays.value = currentList + newDDay
        prefsHelper.saveDDays(_ddays.value)

        // 위젯 업데이트
        DdayWidgetProvider. updateAllWidgets(context)
    }

    /**
     * D-day 수정
     */
    fun updateDDay(dday: DDay) {
        _ddays.value = _ddays.value.map { item ->
            if (item.id == dday.id) dday else item
        }
        prefsHelper.saveDDays(_ddays.value)

        // 위젯 업데이트
        DdayWidgetProvider.updateAllWidgets(context)
    }

    /**
     * D-day 삭제
     */
    fun deleteDDay(id: Long) {
        _ddays.value = _ddays.value. filter { it.id != id }
        prefsHelper.saveDDays(_ddays.value)

        // 위젯 업데이트
        DdayWidgetProvider.updateAllWidgets(context)
    }

    /**
     * D-day 순서 변경
     */
    fun reorderDDays(fromIndex: Int, toIndex: Int) {
        val currentList = _ddays.value. toMutableList()

        // 인덱스 유효성 검사
        if (fromIndex == toIndex) return
        if (fromIndex !in currentList.indices) return
        if (toIndex !in currentList. indices) return

        // 아이템 이동
        val item = currentList. removeAt(fromIndex)
        currentList.add(toIndex, item)

        // order 재설정
        val reorderedList = currentList.mapIndexed { index, ddayItem ->
            ddayItem. copy(order = index)
        }

        _ddays.value = reorderedList
        prefsHelper.saveDDays(_ddays.value)

        // 위젯 업데이트
        DdayWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 설정 업데이트
     */
    fun updateSettings(settings: Settings) {
        _settings.value = settings
        prefsHelper.saveSettings(settings)

        // 위젯 업데이트 (설정 변경 시)
        DdayWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 안식일 추가
     */
    fun addCustomDay(holiday: Holiday) {
        val currentSettings = _settings.value
        val updatedCustomDays = (currentSettings.customDays + holiday)
            .distinctBy { it.date }
            .sortedBy { it.date }

        val newSettings = currentSettings.copy(customDays = updatedCustomDays)
        updateSettings(newSettings)
    }

    /**
     * 안식일 삭제
     */
    fun removeCustomDay(date: String) {
        val currentSettings = _settings.value
        val updatedCustomDays = currentSettings.customDays.filter { it.date != date }

        val newSettings = currentSettings.copy(customDays = updatedCustomDays)
        updateSettings(newSettings)
    }

    /**
     * 현재 연도의 공휴일 자동 로드
     */
    private fun autoLoadCurrentYearHolidays() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val currentSettings = _settings.value

        val currentYearHolidays = currentSettings.publicHolidays.filter { holiday ->
            holiday.date.startsWith(currentYear)
        }

        if (currentYearHolidays.isEmpty()) {
            fetchPublicHolidaysFromApi(currentYear)
        }
    }

    /**
     * 시스템 연도 변경 체크 및 공휴일 업데이트
     */
    private fun checkAndUpdateYearHolidays() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val currentSettings = _settings.value

        val currentYearHolidaysCount = currentSettings.publicHolidays.count { holiday ->
            holiday.date.startsWith(currentYear)
        }

        if (currentYearHolidaysCount == 0) {
            fetchPublicHolidaysFromApi(currentYear)
        }
    }

    /**
     * API에서 공휴일 가져오기
     */
    fun fetchPublicHolidaysFromApi(year: String, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoadingHolidays. value = true

            val result = HolidayApi. fetchHolidays(year)

            result.onSuccess { newHolidays:  List<Holiday> ->
                val currentSettings = _settings.value

                val otherYearHolidays = currentSettings.publicHolidays.filter { holiday: Holiday ->
                    ! holiday.date.startsWith(year)
                }

                val mergedHolidays = (otherYearHolidays + newHolidays)
                    .distinctBy { holiday: Holiday -> holiday.date }
                    . sortedBy { holiday: Holiday -> holiday.date }

                val newSettings = currentSettings.copy(publicHolidays = mergedHolidays)
                updateSettings(newSettings)

                onComplete?.invoke(true, "${year}년 공휴일 ${newHolidays.size}개를 추가했습니다.")
            }

            result.onFailure { exception:  Throwable ->
                onComplete?. invoke(false, "공휴일 정보를 가져오는데 실패했습니다:  ${exception.message}")
            }

            _isLoadingHolidays.value = false
        }
    }
}