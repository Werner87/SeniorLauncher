package com.example.seniorapp

import AnimatedIcon
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindow()

        setContent {
            SeniorAppTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
                HandleBackPress(navController)
            }
        }
    }

    override fun onDestroy() {
        backPressedCallback.remove()
        super.onDestroy()
    }

    @Composable
    fun HandleBackPress(navController: NavController) {
        backPressedCallback = onBackPressedDispatcher.addCallback {
            if (navController.currentBackStackEntry?.destination?.route == "home") {
            } else {
                navController.popBackStack()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                backPressedCallback.remove()
            }
        }
    }
}

@Composable
fun HomePage(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var installedApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var filteredApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var searchText by remember { mutableStateOf("") }
    var isDraggingLocked by remember { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf("") }
    val clockManager = remember { ClockManager { newTime -> currentTime = newTime } }
    var isDeleting by remember { mutableStateOf(false) }
    var backgroundImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val isImageBackground by remember { mutableStateOf(false) }

    val listState = rememberLazyGridState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val backgroundImageUri by context.dataStore.data
        .map { preferences -> preferences[BACKGROUND_IMAGE_URI_KEY] ?: "" }
        .collectAsState(initial = "")

    val backgroundColor by context.dataStore.data
        .map { preferences -> preferences[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White }
        .collectAsState(initial = Color.White)

    val buttonSize by context.dataStore.data
        .map { preferences -> preferences[BUTTON_SIZE_KEY]?.toInt() ?: 150 }
        .collectAsState(initial = 150)

    val packageChangedReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REMOVED ||
                    action == Intent.ACTION_PACKAGE_CHANGED) {
                    installedApps = fetchInstalledApps(context)
                    filteredApps = installedApps.filter { it.label.contains(searchText, ignoreCase = true) }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        installedApps = loadAppPositions(context)
        filteredApps = installedApps
        clockManager.startClock()
        context.registerReceiver(packageChangedReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })
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

    Box(
        modifier = Modifier.fillMaxSize().background(if (backgroundImageBitmap != null) Color.Transparent else backgroundColor)
    ) {
        if (isImageBackground && backgroundImageBitmap != null) {
            Image(bitmap = backgroundImageBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 15.dp, top = 35.dp, end = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTime, fontSize = 55.sp, color = Color.Black, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.animateContentSize()
                )

                AnimatedIcon(
                    imageVector = if (isDraggingLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isDraggingLocked) "Unlock" else "Lock",
                    onClick = { isDraggingLocked = !isDraggingLocked })
                AnimatedIcon(
                    imageVector = if (isDeleting) Icons.Filled.Delete else Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    onClick = { isDeleting = !isDeleting })
                AnimatedIcon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    onClick = { onNavigateToSettings() })
            }

            AnimatedVisibility(visible = !isScrolled) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text(text = stringResource(id = R.string.search)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            LazyVerticalGrid(
                state = listState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                itemsIndexed(filteredApps, key = { _, appInfo -> appInfo.packageName }) { index, appInfo ->
                    AnimatedVisibility(visible = !isDeleting || installedApps.contains(appInfo)) {
                        AppButton(
                            appInfo = appInfo,
                            onClick = { openApp(context, appInfo.packageName) },
                            isDraggingLocked = isDraggingLocked,
                            index = index,
                            gridColumnCount = 2,
                            totalApps = installedApps.size,
                            onReorder = { fromIndex, toIndex ->
                                val updatedApps = installedApps.toMutableList()
                                updatedApps.add(toIndex, updatedApps.removeAt(fromIndex))
                                installedApps = updatedApps
                                filteredApps = updatedApps
                                coroutineScope.launch {
                                    saveAppPositions(context, updatedApps)
                                }
                            },
                            buttonSize = buttonSize,
                            onDeleteClick = { promptUninstallApp(context, appInfo.packageName) },
                            isDeleting = isDeleting
                        )
                    }
                }
            }
        }
    }
}