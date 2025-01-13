package com.example.seniorapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.seniorapp.ui.theme.Typography
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(onBackPressed = {
                onBackPressedDispatcher.onBackPressed()
            })
        }
    }
}

@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val buttonSizeOptions = listOf(120, 150, 170)
    var selectedColor by remember { mutableStateOf(Color.White) }
    var selectedButtonSize by remember { mutableIntStateOf(buttonSizeOptions[1]) }
    var backgroundImage by remember { mutableStateOf<Bitmap?>(null) }
    var isImageBackground by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    scope.launch {
                        setBackgroundImage(context, bitmap, uri.toString()) { updatedBitmap, isImage ->
                            backgroundImage = updatedBitmap
                            isImageBackground = isImage
                            selectedColor = Color.Transparent
                        }
                    }
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        val colorValue = context.dataStore.data.first()[BACKGROUND_COLOR_KEY] ?: Color.White.toArgb().toLong()
        selectedColor = Color(colorValue)
        val savedSize = context.dataStore.data.first()[BUTTON_SIZE_KEY] ?: buttonSizeOptions[1].toLong()
        selectedButtonSize = savedSize.toInt()


        val savedImageUri = context.dataStore.data.first()[BACKGROUND_IMAGE_URI_KEY]
        if (savedImageUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(savedImageUri))
                backgroundImage = BitmapFactory.decodeStream(inputStream)
                isImageBackground = true
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val backgroundColor by context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White
        }
        .collectAsState(initial = Color.White)

    fun updateButtonSize(size: Int) {
        selectedButtonSize = size
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[BUTTON_SIZE_KEY] = size.toLong()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isImageBackground) Color.Transparent else backgroundColor)
    ) {
        if (isImageBackground && backgroundImage != null) {
            Image(
                bitmap = backgroundImage!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = stringResource(id = R.string.settings),
                    style = Typography.titleLarge,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(1, 1, 3, 47))
            ) {
                Text(text = stringResource(id = R.string.choose_home_screen), style = Typography.labelSmall, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(30.dp))

            BackgroundSection(
                onPickImage = { imagePickerLauncher.launch("image/*") }
            ) { color ->
                scope.launch {
                    updateBackgroundColor(context, color) { updatedColor, isImage ->
                        selectedColor = updatedColor
                        isImageBackground = isImage
                        backgroundImage = null // Usuń obraz tła z UI
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(text = stringResource(id = R.string.button_size), style = Typography.bodyLarge, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttonSizeOptions.forEachIndexed { _, size ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (size == selectedButtonSize) Color(1, 1, 3, 47) else Color.Transparent)
                            .clickable { updateButtonSize(size) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (size) {
                            120 -> Icon(Icons.Filled.Square, contentDescription = "Mały", tint = Color.Black, modifier = Modifier.size(22.dp))
                            150 -> Icon(Icons.Filled.Square, contentDescription = "Średni", tint = Color.Black, modifier = Modifier.size(25.dp))
                            170 -> Icon(Icons.Filled.Square, contentDescription = "Duży", tint = Color.Black, modifier = Modifier.size(27.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SettingsScreen(onBackPressed = {})
}


