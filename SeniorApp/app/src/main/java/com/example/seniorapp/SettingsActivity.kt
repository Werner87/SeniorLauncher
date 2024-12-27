package com.example.seniorapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(onBackPressed = {
                // Use the new OnBackPressedDispatcher API
                onBackPressedDispatcher.onBackPressed()
            })
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        // Sprawdzamy, czy aplikacja została tylko zminimalizowana, a nie zakończona
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Nie zamykamy SettingsActivity tutaj, bo chcemy, aby aktywność była
        // zamknięta, gdy wrócimy do niej z MainActivity.
    }
}

@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val buttonSizeOptions = listOf(120, 150, 170) // Rozmiary przycisków w dp
    val buttonSizeLabels = listOf("Mały", "Średni", "Duży")
    var selectedColor by remember { mutableStateOf(Color.White) }
    var isLauncherEnabled by remember { mutableStateOf(isLauncherActive(context)) }
    var selectedButtonSize by remember { mutableIntStateOf(buttonSizeOptions[1]) }

    // Fetch and set initial color
    LaunchedEffect(Unit) {
        val colorValue = context.dataStore.data.first()[BACKGROUND_COLOR_KEY] ?: Color.White.toArgb().toLong()
        selectedColor = Color(colorValue)
        val savedSize = context.dataStore.data.first()[BUTTON_SIZE_KEY] ?: buttonSizeOptions[1].toLong()
        selectedButtonSize = savedSize.toInt()
    }

    val backgroundColor by context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White
        }
        .collectAsState(initial = Color.White)

    fun updateBackgroundColor(color: Color) {
        selectedColor = color // Immediately update UI
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[BACKGROUND_COLOR_KEY] = color.toArgb().toLong()
            }
        }
    }
    fun updateButtonSize(size: Int) {
        selectedButtonSize = size
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[BUTTON_SIZE_KEY] = size.toLong()
            }
        }
    }
    fun toggleLauncher(enable: Boolean) {
        val packageManager = context.packageManager
        val componentName = ComponentName(context, "com.example.seniorapp.LauncherActivity") // Replace with your launcher activity name
        val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
        isLauncherEnabled = enable
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button on the left
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            IconButton(onClick = { onBackPressed() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Ustawienia",
                fontSize = 40.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            },
            modifier = Modifier
                .fillMaxWidth(),

            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), // Zaokrąglone rogi
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(1, 1, 3, 47),  // Zmieniamy tło na niebieskie
            )
        ) {
            Text(
                text = "Wybierz ekran główny",
                fontSize = 20.sp,  // Text size
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(30.dp ))

        Text(
            text = "Kolor tła",
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
                Color.Gray to "Gray",
                Color(176, 224, 230) to "PowderBlue",
                Color(199, 21, 133) to "RedViolet",
                Color(255, 99, 71) to "Tomato",
                Color(255, 215, 0) to "Gold",
                Color(1, 182, 155, 255) to "Aqua"

            ).forEach { (color) ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color)
                        .clickable { updateBackgroundColor(color) }
                        .padding(8.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text="Rozmiar przycisków",
            fontSize = 30.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttonSizeOptions.forEachIndexed { index, size ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(if (size == selectedButtonSize) Color.LightGray else Color.Transparent)
                        .clickable { updateButtonSize(size) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (size) {
                        120 -> Icon(Icons.Filled.Square, contentDescription = "Mały", tint = Color.Black, modifier = Modifier.size(size=22.dp))
                        150 -> Icon(Icons.Filled.Square, contentDescription = "Średni", tint = Color.Black, modifier = Modifier.size(size=25.dp))
                        170 -> Icon(Icons.Filled.Square, contentDescription = "Duży", tint = Color.Black, modifier = Modifier.size(size=27.dp))
                    }
                }
            }
        }
    }
}
fun isLauncherActive(context: Context): Boolean {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo?.activityInfo?.packageName == context.packageName
}


