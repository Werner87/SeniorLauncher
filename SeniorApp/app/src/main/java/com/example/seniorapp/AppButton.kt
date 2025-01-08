package com.example.seniorapp

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppButton(
    appInfo: AppInfo,
    onClick: () -> Unit,
    isDraggingLocked: Boolean,
    totalApps: Int,
    apps: List<AppInfo>,
    index: Int,
    buttonSize: Int,
    onReorder: (Int, Int) -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    var initialIndex by remember { mutableIntStateOf(index) }
    val scale = remember { Animatable(1f) }

    val appIconDrawable = remember(appInfo.packageName) {
        context.packageManager.getApplicationIcon(appInfo.packageName)
    }
    val appIconBitmap = appIconDrawable.toBitmap()

    val coroutineScope = rememberCoroutineScope()

    val dragModifier = remember(isDraggingLocked) {
        if (!isDraggingLocked) {
            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        coroutineScope.launch {
                            scale.animateTo(1.1f, animationSpec = tween(150))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            scale.animateTo(1f, animationSpec = tween(150))
                        }
                        val newIndex = calculateNewIndex(
                            offset = offset,
                            index = index,
                            columnCount = 2,
                            buttonSizePx = buttonSize.dp.toPx(),
                            totalApps = totalApps
                        )
                        if (newIndex != index) {
                            Log.d("AppButton", "Moving item from $index to $newIndex")
                            onReorder(index, newIndex)
                        }
                        offset = Offset.Zero
                    },
                )
            }
        } else {
            Modifier
        }
    }

    // Main composable layout
    Box(
        modifier = modifier
            .size(buttonSize.dp)
            .then(dragModifier)
            .graphicsLayer(
                translationX = offset.x,
                translationY = offset.y,
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .clickable {
                coroutineScope.launch {
                    if (isDeleting) {
                        onDeleteClick()
                    } else {
                        scale.animateTo(1.2f, animationSpec = tween(150))
                        scale.animateTo(1f, animationSpec = tween(150))
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color.Transparent)
        ) {
            Image(
                bitmap = appIconBitmap.asImageBitmap(),
                contentDescription = appInfo.label,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}



