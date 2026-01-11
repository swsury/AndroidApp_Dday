package com.example.ddayapp.widget

import android.content.Context
import androidx.compose.runtime. Composable
import androidx.compose. ui.graphics.Color  // 🔥 추가
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit. sp
import androidx.glance. GlanceId
import androidx. glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx. glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance. background
import androidx.glance. layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.ddayapp.data.PrefsHelper
import com.example. ddayapp.utils.DateCalculator

class DdayWidget :  GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id:  GlanceId) {
        provideContent {
            GlanceTheme {
                DdayWidgetContent(context, id)
            }
        }
    }

    @Composable
    private fun DdayWidgetContent(context: Context, glanceId: GlanceId) {
        // 저장된 DDay ID 가져오기
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val widgetId = glanceId.toString().hashCode()
        val ddayId = prefs.getLong("widget_dday_id_$widgetId", -1L)

        if (ddayId == -1L) {
            // 설정 안됨
            EmptyWidgetContent()
        } else {
            // DDay 데이터 로드
            val prefsHelper = PrefsHelper(context)
            val ddays = prefsHelper.loadDDays()
            val dday = ddays.find { it.id == ddayId }

            if (dday != null) {
                val settings = prefsHelper.loadSettings()
                val publicHolidays = settings. publicHolidays. map { it.date }.toSet()
                val customDays = settings.customDays.map { it.date }.toSet()

                // D-Day 계산
                val ddayText = DateCalculator.calculateDDay(
                    targetDate = dday. date,
                    excludePublicHolidays = dday.excludePublicHolidays,
                    excludeCustomDays = dday.excludeCustomDays,
                    excludedWeekdays = dday. excludedWeekdays,
                    publicHolidays = publicHolidays,
                    customDays = customDays
                )

                val formattedDate = DateCalculator.formatDate(dday.date)

                // DDay 위젯 UI
                DdayWidgetCard(
                    labelTitle = dday.labelTitle,
                    title = dday.title,
                    ddayText = ddayText,
                    date = formattedDate,
                    color = parseColor(dday. color)
                )
            } else {
                // DDay 삭제됨
                EmptyWidgetContent()
            }
        }
    }

    @Composable
    private fun DdayWidgetCard(
        labelTitle:  String,
        title: String,
        ddayText: String,
        date: String,
        color: Color  // 🔥 Long → Color
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))  // 🔥 수정
                .clickable {
                    // 앱 실행 액션은 Receiver에서 처리
                }
        ) {
            // 헤더
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(ColorProvider(color))  // 🔥 수정
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = labelTitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color.White)  // 🔥 수정
                    )
                )
            }

            // 내용
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 제목 & D-Day
                Row(
                    modifier = GlanceModifier. fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.Start,
                    verticalAlignment = Alignment. Vertical.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorProvider(Color(0xFF1B1C1F))  // 🔥 수정
                        ),
                        modifier = GlanceModifier. defaultWeight()
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Text(
                        text = ddayText,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(color)  // 🔥 수정
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // 날짜
                Text(
                    text = date,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color(0xFF808080))  // 🔥 수정
                    )
                )
            }
        }
    }

    @Composable
    private fun EmptyWidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))  // 🔥 수정
                .padding(16.dp),
            horizontalAlignment = Alignment. Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "위젯 설정 필요",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF666666))  // 🔥 수정
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "길게 눌러 설정하세요",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorProvider(Color(0xFF999999))  // 🔥 수정
                )
            )
        }
    }

    // 🔥 반환 타입 변경:  Long → Color
    private fun parseColor(colorString: String): Color {
        return try {
            val colorInt = android.graphics.Color. parseColor(colorString)
            Color(colorInt)
        } catch (e: Exception) {
            Color(0xFF24a19c)
        }
    }
}