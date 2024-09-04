package com.example.seniorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ColorPickerScreen()
        }
    }
}

@Composable
fun ColorPickerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedColor by remember { mutableStateOf(Color.White) }

    // Fetch and set initial color
    LaunchedEffect(Unit) {
        val colorValue = context.dataStore.data.first()[BACKGROUND_COLOR_KEY] ?: Color.White.toArgb().toLong()
        selectedColor = Color(colorValue)
    }

    fun updateBackgroundColor(color: Color) {
        selectedColor = color // Immediately update UI
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[BACKGROUND_COLOR_KEY] = color.toArgb().toLong()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Background Color",
            fontSize = 30.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Color.White to "White",
                Color.LightGray to "LightGray",
                Color.DarkGray to "DarkGray",
                Color.Gray to "Gray",
                Color.Blue to "Blue"
            ).forEach { (color, label) ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color)
                        .clickable { updateBackgroundColor(color) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
