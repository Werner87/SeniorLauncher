package com.example.seniorapp

import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClockManager(private val onTimeChanged: (String) -> Unit) {
    private var currentTime: String = getCurrentTime()
    private var job: Job? = null

    @Composable
    fun ClockDisplay(currentTime: String) {
        Text(
            text = currentTime,
            fontSize = 55.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.animateContentSize()
        )
    }

    // Startuje zegar
    fun startClock() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val newTime = getCurrentTime()
                if (newTime != currentTime) {
                    currentTime = newTime
                    onTimeChanged(newTime)  // Zaktualizuj czas w UI
                }
                delay(1000L)  // Odczekaj 1 sekundę przed ponownym pobraniem czasu
            }
        }
    }

    // Zatrzymuje zegar
    fun stopClock() {
        job?.cancel()
    }

    // Funkcja pobierająca aktualny czas w formacie "HH:mm:ss"
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
