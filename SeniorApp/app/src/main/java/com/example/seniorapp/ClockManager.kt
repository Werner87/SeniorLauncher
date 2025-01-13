package com.example.seniorapp

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

    fun startClock() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val newTime = getCurrentTime()
                if (newTime != currentTime) {
                    currentTime = newTime
                    onTimeChanged(newTime)
                }
                delay(1000L)
            }
        }
    }

    fun stopClock() {
        job?.cancel()
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
