package com.example.seniorapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun HomePage(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val installedApps = remember { mutableStateListOf<AppInfo>()}
    var filteredApps by remember { mutableStateOf(installedApps.toList()) }
    var searchText by remember { mutableStateOf("") }
    var isDraggingLocked by remember { mutableStateOf(true) }
    val currentTime = remember { mutableStateOf("") }
    val clockManager = remember { ClockManager { newTime -> currentTime.value = newTime } }
    var isDeleting by remember { mutableStateOf(false) }
    var backgroundImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImageBackground by remember { mutableStateOf(false) }
    var backgroundColor by remember { mutableStateOf(Color.White) } // Deklaracja backgroundColor
    val listState = rememberLazyGridState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val backgroundImageUri by context.dataStore.data
        .map { preferences -> preferences[BACKGROUND_IMAGE_URI_KEY] ?: "" }
        .collectAsState(initial = "")

    val buttonSize by context.dataStore.data
        .map { preferences -> preferences[BUTTON_SIZE_KEY]?.toInt() ?: 150 }
        .collectAsState(initial = 150)

    val packageChangedReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REMOVED ||
                    action == Intent.ACTION_PACKAGE_CHANGED) {
                    val fetchedApps = fetchInstalledApps(context)
                    installedApps.clear()
                    installedApps.addAll(fetchedApps)
                    filteredApps = fetchedApps.filter { it.label.contains(searchText, ignoreCase = true) }
                }
            }
        }
    }

    LaunchedEffect(Unit) {

        clockManager.startClock()
        val fetchedApps = loadAppPositions(context)
        installedApps.clear()
        installedApps.addAll(fetchedApps)
        filteredApps = fetchedApps

        context.registerReceiver(packageChangedReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })

        // Load background settings
        val savedColor = context.dataStore.data.firstOrNull()?.get(BACKGROUND_COLOR_KEY)?.let { Color(it) } ?: Color.White
        val savedImageUri = context.dataStore.data.firstOrNull()?.get(BACKGROUND_IMAGE_URI_KEY)

        if (!savedImageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(savedImageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                backgroundImageBitmap = BitmapFactory.decodeStream(inputStream)
                isImageBackground = true
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                isImageBackground = false
            }
        } else {
            backgroundColor = savedColor
            isImageBackground = false
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                Log.d("AppGrid", "First visible item index: $index")
            }
    }

    LaunchedEffect(searchText) {
        filteredApps = installedApps.filter { it.label.contains(searchText, ignoreCase = true) }
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

    DisposableEffect(Unit) {
        onDispose {
            clockManager.stopClock()
            context.unregisterReceiver(packageChangedReceiver)
        }
    }

    val onReorder: (Int, Int) -> Unit = { fromIndex, toIndex ->
        Log.d("AppGrid", "Reordering item: fromIndex=$fromIndex toIndex=$toIndex")
        if (fromIndex in installedApps.indices && toIndex in installedApps.indices) {
            updateIconsOnDrag(installedApps, fromIndex, toIndex)
            filteredApps = installedApps.toList() // Odśwież siatkę
            coroutineScope.launch {
                saveAppPositions(context, installedApps)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isImageBackground) Color.Transparent else backgroundColor)
    ) {
        if (isImageBackground && backgroundImageBitmap != null) {
            Image(
                bitmap = backgroundImageBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavBar(
                currentTime = currentTime.value,
                onNavigateToSettings = onNavigateToSettings,
                isDraggingLocked = isDraggingLocked,
                isDeleting = isDeleting,
                isScrolled = isScrolled, // Przekazanie `isScrolled`
                searchText = searchText, // Przekazanie `searchText`
                onSearchTextChanged = { searchText = it }, // Aktualizacja `searchText`
                onLockToggle = { isDraggingLocked = !isDraggingLocked },
                onDeleteToggle = { isDeleting = !isDeleting }
            )

            AppGrid(
                listState = listState,
                installedApps = filteredApps,
                isDraggingLocked = isDraggingLocked,
                onReorder = onReorder,
                buttonSize = buttonSize,
                onDeleteClick = {appInfo ->
                    promptUninstallApp(context,appInfo.packageName)
                },
                isDeleting = isDeleting,
                context = context
            )
        }
    }
}