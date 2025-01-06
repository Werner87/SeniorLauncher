package com.example.seniorapp

import android.graphics.Color.TRANSPARENT
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

fun ComponentActivity.setupWindow() {
    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

    val lightTransparentStyle = SystemBarStyle.light(
        scrim = TRANSPARENT,
        darkScrim = TRANSPARENT
    )
    enableEdgeToEdge(
        statusBarStyle = lightTransparentStyle,
        navigationBarStyle = lightTransparentStyle
    )
}