package com.example.seniorapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppGrid(
    installedApps: List<AppInfo>,
    isDraggingLocked: Boolean,
    onReorder: (Int, Int) -> Unit,
    onDeleteClick: (AppInfo) -> Unit,
    buttonSize: Int,
    isDeleting: Boolean,
    context: Context,
    listState: LazyGridState
) {
    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),

    ) {
        itemsIndexed(installedApps, key = { index, appInfo -> "${appInfo.packageName}-$index" }) { index, appInfo ->
            Log.d("LazyVerticalGrid", "Rendering item: $appInfo at index: $index")
            AppButton(
                appInfo = appInfo,
                onClick = { openApp(context, appInfo.packageName) },
                isDraggingLocked = isDraggingLocked,
                index = index,
                totalApps = installedApps.size,
                apps = installedApps,
                buttonSize = buttonSize,
                onReorder = onReorder,
                onDeleteClick = { onDeleteClick(appInfo) },
                isDeleting = isDeleting
            )
        }
    }
}
