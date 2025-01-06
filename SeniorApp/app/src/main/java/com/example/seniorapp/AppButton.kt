package com.example.seniorapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppButton(
    appInfo: AppInfo,
    onClick: () -> Unit,
    isDraggingLocked: Boolean,
    index: Int,
    gridColumnCount: Int,
    totalApps: Int,
    buttonSize: Int,
    onReorder: (Int, Int) -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    var initialIndex by remember { mutableIntStateOf(index) }
    var scale by remember { mutableFloatStateOf(1f) }

    val scaleAnim by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 150),
        label = "scaleAnimation"
    )

    val appIconDrawable = remember(appInfo.packageName) {
        context.packageManager.getApplicationIcon(appInfo.packageName)
    }
    val appIconBitmap = appIconDrawable.toBitmap()

    val coroutineScope = rememberCoroutineScope()

    // Custom drag modifier function
    val dragModifier = remember(isDraggingLocked) {
        if (!isDraggingLocked) {
            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        initialIndex = index
                        coroutineScope.launch { scale = 1.1f }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    },
                    onDragEnd = {
                        coroutineScope.launch { scale = 1f }
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
        } else {
            Modifier
        }
    }

    // Main composable layout
    Box(
        modifier = modifier
            .size(buttonSize.dp)
            .padding(8.dp)
            .then(dragModifier)
            .graphicsLayer(
                translationX = offset.x,
                translationY = offset.y,
                scaleX = scaleAnim,
                scaleY = scaleAnim
            )
            .clickable {
                coroutineScope.launch {
                    if (isDeleting) {
                        onDeleteClick()
                    } else {
                        scale = 1.1f
                        delay(150)
                        scale = 1f
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

