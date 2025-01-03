package com.example.seniorapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val lightTransparentStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )
        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

        setContent {
            SeniorAppTheme {
                val navController = rememberNavController()

                // Handle back press navigation
                val backPressedCallback = onBackPressedDispatcher.addCallback(this) {
                    // Check if we're on the home screen
                    if (navController.currentBackStackEntry?.destination?.route == "home") {
                        // Prevent back press if we're on the home screen
//                        Toast.makeText(this@MainActivity, "Jesteś już na stronie głównej!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Otherwise allow back press to navigate
                        navController.popBackStack()
                    }
                }

                AppNavHost(navController = navController)

                // Dispose of callback when the Composable is disposed
                DisposableEffect(Unit) {
                    onDispose {
                        backPressedCallback.remove()
                    }
                }
            }
        }
    }
}

@Composable
fun HomePage(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current

    var installedApps by rememberSaveable { mutableStateOf(emptyList<AppInfo>()) }
    var filteredApps by rememberSaveable { mutableStateOf(emptyList<AppInfo>()) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isDraggingLocked by rememberSaveable { mutableStateOf(true) }
    var currentTime by rememberSaveable { mutableStateOf("") }
    val clockManager = remember { ClockManager { newTime -> currentTime = newTime } }
    var isDeleting by remember { mutableStateOf(false) }
    var backgroundImageBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var isImageBackground by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    val listState = rememberLazyGridState()  // To track scroll position
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val scaleAnim = animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 150, easing = { it * it }),
        label = "scaleAnimation"
    )

    val onDeleteClick: (AppInfo) -> Unit = { appInfo ->
        if (isDeleting) {
            promptUninstallApp(context, appInfo.packageName)
        }
    }
    isImageBackground = backgroundImageBitmap != null

    val backgroundImageUri by context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_IMAGE_URI_KEY] ?: ""
        }
        .collectAsState(initial = "")

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

    suspend fun saveAppPositions(context: Context, appPositions: List<AppInfo>) {
        val preferences = context.dataStore
        val serializedAppPositions = appPositions.joinToString(",") { it.packageName }
        preferences.edit { preferences ->
            preferences[APP_POSITION_KEY] = serializedAppPositions
        }
    }

    suspend fun loadAppPositions(context: Context): List<AppInfo> {
        val preferences = context.dataStore
        val savedPositions = preferences.data
            .map { it[APP_POSITION_KEY] ?: "" }
            .first()

        if (savedPositions.isNotEmpty()) {
            val packageNames = savedPositions.split(",")
            return fetchInstalledApps(context).filter { app ->
                packageNames.contains(app.packageName)
            }
        }

        return fetchInstalledApps(context) // Default order
    }

    val packageChangedReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REMOVED ||
                    action == Intent.ACTION_PACKAGE_CHANGED
                ) {
                    installedApps = fetchInstalledApps(context)
                    filteredApps = if (searchText.isEmpty()) {
                        installedApps
                    } else {
                        installedApps.filter {
                            it.label.contains(searchText, ignoreCase = true)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        installedApps = loadAppPositions(context)
        filteredApps = installedApps
        clockManager.startClock()
        context.registerReceiver(
            packageChangedReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
        )
    }

    LaunchedEffect(searchText) {
        filteredApps = if (searchText.isEmpty()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchText, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(backgroundImageUri) {
        if (backgroundImageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(backgroundImageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                backgroundImageBitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                backgroundImageBitmap = null
            }
        } else {
            backgroundImageBitmap = null
        }
    }

    LaunchedEffect(scale) {
        // Odczekaj chwilę, aby animacja została ukończona, potem zresetuj skalowanie
        delay(150)
        scale = 1f
    }

    // Stopping the clock when the Composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            clockManager.stopClock()
            context.unregisterReceiver(packageChangedReceiver)
        }
    }

    LaunchedEffect(filteredApps) {
        saveAppPositions(context, filteredApps)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (backgroundImageBitmap != null) Color.Transparent else backgroundColor)
    ) {
        // Wyświetlanie obrazu tła tylko, gdy jest dostępny
        if (isImageBackground && backgroundImageBitmap != null) {
            Image(
                bitmap = backgroundImageBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(top = 20.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 35.dp, end = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 55.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .animateContentSize()
                    )
                }

                Icon(
                    imageVector = if (isDraggingLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isDraggingLocked) "Unlock" else "Lock",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            isDraggingLocked = !isDraggingLocked
                            scale = 1.1f
                        }
                        .animateContentSize()
                        .scale(scaleAnim.value),
                    tint = Color.Black
                )

                Icon(
                    imageVector = if (isDeleting) Icons.Filled.Delete else Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            isDeleting = !isDeleting
                            scale = 1.1f
                        }
                        .animateContentSize()
                        .scale(scaleAnim.value),
                    tint = Color.Black
                )

                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onNavigateToSettings()
                            scale = 1.1f
                        }
                        .animateContentSize()
                        .scale(scaleAnim.value),
                    tint = Color.Black
                )
            }
            AnimatedVisibility(
                visible = !isScrolled,  // Show when not scrolled
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp)
            ) {
            TextField(
                value = searchText,
                onValueChange = { newText -> searchText = newText },
                placeholder = { Text("Search apps...") },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true
            )
            }
            LazyVerticalGrid(
                state = listState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                itemsIndexed(filteredApps) { index, appInfo ->
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
                                filteredApps = updatedApps
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
        "Wiadomości" to Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
        "Galeria" to Intent(Intent.ACTION_PICK).apply { type = "image/*" },
        "Aparat" to Intent(MediaStore.ACTION_IMAGE_CAPTURE), //? nwm czy dziala
        "Kontakty" to Intent(Intent.ACTION_PICK).apply { type = "vnd.android.cursor.dir/contact" },
        "Zegar" to Intent(AlarmClock.ACTION_SHOW_ALARMS),
        "Ustawienia" to Intent(Settings.ACTION_SETTINGS)
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
            Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0
        )?.activityInfo?.packageName == packageName

        val isPriorityApp = priorityIntents.values.any { intent ->
            packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName == packageName
        } || knownGalleryPackages.contains(packageName)

        if (isCameraApp) {
            priorityApps.add(app)
        } else if (isPriorityApp) {
            priorityApps.add(app)
        } else {
            otherApps.add(app)
        }
    }

    return (priorityApps + otherApps).map { resolveInfo ->
        AppInfo(
            packageName = resolveInfo.activityInfo.packageName,
            label = resolveInfo.loadLabel(packageManager).toString()
        )
    }
}
fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
    }
}
