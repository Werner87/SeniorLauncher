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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedColor by remember { mutableStateOf(Color.White) }
    var isLauncherEnabled by remember { mutableStateOf(isLauncherActive(context)) }

    // Fetch and set initial color
    LaunchedEffect(Unit) {
        val colorValue = context.dataStore.data.first()[BACKGROUND_COLOR_KEY] ?: Color.White.toArgb().toLong()
        selectedColor = Color(colorValue)
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
        Text(
            text = "Ustawienia",
            fontSize = 40.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            },
            modifier = Modifier
                .fillMaxWidth(),

            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), // Zaokrąglone rogi
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(176,224,230),  // Zmieniamy tło na niebieskie
            )
        ) {
            Text(
                text = "Wybierz ekran główny",
                fontSize = 20.sp,  // Text size
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

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
                Color.LightGray to "LightGray",
                Color.Gray to "Gray",
                Color(176, 224, 230) to "PowderBlue",
                Color(199, 21, 133) to "RedViolet",
                Color(255, 99, 71) to "Tomato",
                Color(255, 215, 0) to "Gold"

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
fun isLauncherActive(context: Context): Boolean {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo?.activityInfo?.packageName == context.packageName
}


