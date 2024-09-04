package com.example.seniorapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.math.roundToInt

@Composable
fun AppButton(
    appInfo: com.example.seniorapp.AppInfo,
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
            .size(150.dp)  // Increase button size
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
                            with(density) {
                                val x = (offset.x / (160.dp.toPx())).roundToInt()
                                val y = (offset.y / (160.dp.toPx())).roundToInt()
                                val newPosition = (initialIndex + y * gridColumnCount + x).coerceIn(0, totalApps - 1)
                                if (newPosition != initialIndex) {
                                    onReorder(initialIndex, newPosition)
                                }
                                offset = Offset.Zero
                            }
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

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
