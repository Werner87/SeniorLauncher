package com.example.seniorapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val BACKGROUND_COLOR_KEY = longPreferencesKey("background_color")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorInterface()
        }
    }
}

@Composable
fun SeniorInterface() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var isDraggingLocked by remember { mutableStateOf(false) }
    var selectedBackgroundColor by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        selectedBackgroundColor = context.dataStore.data.first()[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White
    }

    fun updateBackgroundColor(color: Color) {
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[BACKGROUND_COLOR_KEY] = color.value.toLong()
            }
            selectedBackgroundColor = color
        }
    }

    LaunchedEffect(Unit) {
        installedApps = fetchInstalledApps(context)
    }

    // Upewnij się, że `Column` wypełnia cały ekran
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Senior Phone",
                    fontSize = 30.sp,
                    color = Color.Black
                )

                Icon(
                    imageVector = if (isDraggingLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isDraggingLocked) "Unlock" else "Lock",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            isDraggingLocked = !isDraggingLocked
                        }
                        .animateContentSize(),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dodanie przycisków do wyboru koloru
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
                    Color.Cyan to "Cyan",
                    Color.Yellow to "Yellow"
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

            Spacer(modifier = Modifier.height(16.dp))

            // Zmiana liczby kolumn na 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(installedApps) { index, appInfo ->
                    AppButton(
                        appInfo = appInfo,
                        onClick = { openApp(context, appInfo.packageName) },
                        isDraggingLocked = isDraggingLocked,
                        index = index,
                        gridColumnCount = 2,
                        totalApps = installedApps.size,
                        onReorder = { fromIndex, toIndex ->
                            val updatedApps = installedApps.toMutableList()
                            val app = updatedApps.removeAt(fromIndex)
                            updatedApps.add(toIndex, app)
                            installedApps = updatedApps
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppButton(
    appInfo: AppInfo,
    onClick: () -> Unit,
    isDraggingLocked: Boolean,
    index: Int,
    gridColumnCount: Int,
    totalApps: Int,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var initialIndex by remember { mutableStateOf(index) }

    val appIcon = remember(appInfo.packageName) {
        context.packageManager.getApplicationIcon(appInfo.packageName)
    }

    Box(
        modifier = modifier
            .size(150.dp)  // Zwiększ rozmiar przycisku
            .padding(8.dp)
            .pointerInput(Unit) {
                if (!isDraggingLocked) {
                    detectDragGestures(
                        onDragStart = {
                            initialIndex = index
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offset += dragAmount
                        },
                        onDragEnd = {
                            if (!isDraggingLocked) {
                                with(density) {
                                    val x = (offset.x / (160.dp.toPx())).roundToInt()
                                    val y = (offset.y / (160.dp.toPx())).roundToInt()
                                    val newPosition = (initialIndex + y * gridColumnCount + x).coerceIn(0, totalApps - 1)
                                    if (newPosition != initialIndex) {
                                        onReorder(initialIndex, newPosition)
                                    }
                                }
                            }
                            offset = Offset.Zero
                        }
                    )
                }
            }
            .graphicsLayer(
                translationX = offset.x,
                translationY = offset.y,
                scaleX = scale,
                scaleY = scale
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Image(
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = appInfo.label,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun fetchInstalledApps(context: Context): List<AppInfo> {
    val packageManager = context.packageManager
    val apps = packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }, 0)
    return apps.map { resolveInfo ->
        val packageName = resolveInfo.activityInfo.packageName
        val label = resolveInfo.loadLabel(packageManager).toString()
        val icon = resolveInfo.loadIcon(packageManager)
        AppInfo(packageName, label, icon)
    }
}

fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    intent?.let { context.startActivity(it) } ?: Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
}

data class AppInfo(val packageName: String, val label: String, val icon: Drawable)

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
