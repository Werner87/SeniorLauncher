package com.example.seniorapp

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ustawienia transparentnego paska nawigacyjnego
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarColor(Color.White.toArgb())
            window.setNavigationBarDividerColor(Color.Black.toArgb())
        }

        setContent {
            SeniorAppTheme {
                // Pamiętamy kontroler nawigacji
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
@Composable
fun HomePage(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current

    var installedApps by rememberSaveable { mutableStateOf(emptyList<AppInfo>()) }
    var isDraggingLocked by rememberSaveable { mutableStateOf(true) }
    var currentTime by rememberSaveable { mutableStateOf("") }
    val clockManager = remember { ClockManager { newTime -> currentTime = newTime } }
    var isDeleting by remember { mutableStateOf(false) }

    val onDeleteClick: (AppInfo) -> Unit = { appInfo ->
        if (isDeleting) {
            promptUninstallApp(context, appInfo.packageName)
            installedApps = installedApps.filter { it.packageName != appInfo.packageName }
        }
    }

    // Observe changes to the background color from DataStore
    val backgroundColor by context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White
        }
        .collectAsState(initial = Color.White)

    val buttonSize by context.dataStore.data
        .map { preferences ->
            preferences[BUTTON_SIZE_KEY]?.toInt() ?: 150
        }
        .collectAsState(initial = 150)

    // Zainicjuj nasłuchiwanie zmian w aplikacjach
    val appChangeReceiver = remember { AppChangeReceiver(onAppChanged = {
        // Zaktualizuj listę aplikacji po instalacji/wyjątku
        installedApps = fetchInstalledApps(context)
    }) }

    LaunchedEffect(Unit) {
        installedApps = fetchInstalledApps(context)
        clockManager.startClock()  // Start clock
        appChangeReceiver.register(context)
    }

    // Stopping the clock when the Composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            clockManager.stopClock()
            appChangeReceiver.unregister(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp, end = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.padding(start = 15.dp)
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 50.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.animateContentSize()
                    )
                }

                Icon(
                    imageVector = if (isDraggingLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isDraggingLocked) "Unlock" else "Lock",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            isDraggingLocked = !isDraggingLocked
                        }
                        .animateContentSize(),
                    tint = Color.Black
                )

                Icon(
                    imageVector = if (isDeleting) Icons.Filled.Delete else Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            isDeleting = !isDeleting
                        }
                        .animateContentSize(),
                    tint = Color.Black
                )

                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            onNavigateToSettings()
                        }
                        .animateContentSize(),
                    tint = Color.Black
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                itemsIndexed(installedApps) { index, appInfo ->
                    AnimatedVisibility(
                        visible = !isDeleting || installedApps.contains(appInfo),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
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
                            },
                            buttonSize = buttonSize,
                            onDeleteClick = {
                                onDeleteClick(appInfo)
                            },
                            isDeleting = isDeleting
                        )
                    }
                }
            }
        }
    }
}
fun promptUninstallApp(context: Context, packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE)
    intent.data = Uri.parse("package:$packageName")
    context.startActivity(intent)
    Toast.makeText(context, "Uninstalling $packageName", Toast.LENGTH_SHORT).show()
}
fun fetchInstalledApps(context: Context): List<AppInfo> {
    val packageManager = context.packageManager

    // Pobieramy listę wszystkich aplikacji, które można uruchomić
    val apps = packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }, 0)

    // Zdefiniuj intencje dla aplikacji priorytetowych
    val priorityIntents = mapOf(
        "Telefon" to Intent(Intent.ACTION_DIAL),
        "Wiadomości" to Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:")),
        "Galeria" to Intent(Intent.ACTION_PICK).apply { type = "image/*" },
        "Aparat" to Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), //? nwm czy dziala
        "Kontakty" to Intent(Intent.ACTION_PICK).apply { type = "vnd.android.cursor.dir/contact" },
        "Zegar" to Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS),
        "Ustawienia" to Intent(android.provider.Settings.ACTION_SETTINGS)
    )

    val knownGalleryPackages = listOf(
        "com.google.android.apps.photos", // Google Photos
        "com.sec.android.gallery3d", // Samsung Gallery
        "com.miui.gallery", // Xiaomi Gallery
        "com.sonyericsson.album", // Sony Album
        "com.htc.album" // HTC Album
    )

    val priorityApps = mutableListOf<ResolveInfo>()
    val otherApps = mutableListOf<ResolveInfo>()

    for (app in apps) {
        val packageName = app.activityInfo.packageName
        val isCameraApp = packageManager.resolveActivity(
            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), 0
        )?.activityInfo?.packageName == packageName

        val isPriorityApp = priorityIntents.values.any { intent ->
            packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName == packageName
        } || knownGalleryPackages.contains(packageName)

        if (isCameraApp) {
            // Dodaj aparat bezpośrednio na koniec listy priorytetowych
            priorityApps.add(app)
        } else if (isPriorityApp) {
            priorityApps.add(app)
        } else {
            otherApps.add(app)
        }
    }

    val sortedApps = priorityApps + otherApps

    return sortedApps.map { resolveInfo ->
        val packageName = resolveInfo.activityInfo.packageName
        val label = resolveInfo.loadLabel(packageManager).toString()
        val icon = resolveInfo.loadIcon(packageManager)
        AppInfo(packageName, label, icon)
    }
}
fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
    }
}
