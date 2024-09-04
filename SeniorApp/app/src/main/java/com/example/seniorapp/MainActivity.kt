package com.example.seniorapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.map

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

    var installedApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var isDraggingLocked by remember { mutableStateOf(false) }

    // Observe changes to the background color from DataStore
    val backgroundColor by context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_COLOR_KEY]?.let { Color(it) } ?: Color.White
        }
        .collectAsState(initial = Color.White)

    // Fetch installed apps
    LaunchedEffect(Unit) {
        installedApps = fetchInstalledApps(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Use the observed background color
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
                        .size(30.dp)
                        .clickable {
                            isDraggingLocked = !isDraggingLocked
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
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                        .animateContentSize(),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
    }
}
