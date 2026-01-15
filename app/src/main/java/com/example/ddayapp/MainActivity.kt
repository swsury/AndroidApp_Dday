package com.example.ddayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ddayapp.ui.DdayScreen
import com.example.ddayapp.ui.theme.DdayAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 위젯에서 전달된 D-day ID 받기
        val ddayIdFromWidget = intent?. getLongExtra("dday_id", -1L) ?: -1L
        val shouldOpenEdit = intent?.getBooleanExtra("open_edit", false) ?: false

        setContent {
            DdayAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme. background
                ) {
                    DdayScreen(
                        initialDdayIdToEdit = if (shouldOpenEdit && ddayIdFromWidget != -1L) {
                            ddayIdFromWidget
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    // 🔥 앱이 이미 실행 중일 때 새 Intent 처리
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        // 새 Intent로 액티비티 재시작
        recreate()
    }
}