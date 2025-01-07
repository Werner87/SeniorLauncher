package com.example.seniorapp

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppGrid(
    apps: List<AppInfo>,
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
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(apps, key = { _, appInfo -> appInfo.packageName }) { index, appInfo ->
            AppButton(
                modifier = Modifier.padding(top=8.dp),
                appInfo = appInfo,
                onClick = { openApp(context, appInfo.packageName) },
                isDraggingLocked = isDraggingLocked,
                index = index,
                totalApps = apps.size,
                buttonSize = buttonSize,
                onReorder = { fromIndex, toIndex -> onReorder(fromIndex, toIndex) },
                onDeleteClick = { onDeleteClick(appInfo) },
                isDeleting = isDeleting
            )
        }
    }
}
