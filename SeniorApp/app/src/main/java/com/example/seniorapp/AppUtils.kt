package com.example.seniorapp

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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

    val priorityIntents = mapOf(
        "Telefon" to Intent(Intent.ACTION_DIAL),
        "Wiadomości" to Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
        "Galeria" to Intent(Intent.ACTION_PICK).apply { type = "image/*" },
        "Aparat" to Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        "Kontakty" to Intent(Intent.ACTION_PICK).apply { type = "vnd.android.cursor.dir/contact" },
        "Zegar" to Intent(AlarmClock.ACTION_SHOW_ALARMS),
        "Ustawienia" to Intent(Settings.ACTION_SETTINGS)
    )

    val knownGalleryPackages = listOf(
        "com.google.android.apps.photos", // Google Photos
        "com.sec.android.gallery3d", // Samsung Gallery
        "com.sec.android.app.camera", // Samsung Camera Gallery
        "com.miui.gallery", // Xiaomi Gallery
        "com.sonyericsson.album", // Sony Album
        "com.htc.album", // HTC Album
        "com.huawei.photos", // Huawei Gallery
        "com.huawei.hidisk", // Huawei Gallery with cloud integration
        "com.oneplus.gallery", // OnePlus Gallery
        "com.coloros.gallery", // Oppo Gallery
        "com.vivo.gallery", // Vivo Gallery
        "com.realme.gallery", // Realme Gallery
        "com.lge.gallery", // LG Gallery
        "com.motorola.MotGallery2", // Motorola Gallery
        "com.asus.gallery", // Asus Gallery
        "com.hmdglobal.app.gallery", // Nokia Gallery
        "cn.nubia.gallery", // ZTE Gallery
        "com.lenovo.scg", // Lenovo Gallery
        "com.micromax.gallery", // Micromax Gallery
        "com.yulong.android.gallery", // Coolpad Gallery
        "com.meizu.media.gallery", // Meizu Gallery
        "com.transsion.gallery" // Transsion Gallery (Infinix, TECNO, Itel)
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

suspend fun saveAppPositions(context: Context, appPositions: List<AppInfo>) {
    val preferences = context.dataStore
    val serializedAppPositions = appPositions.joinToString(",") { it.packageName }
    preferences.edit { it[APP_POSITION_KEY] = serializedAppPositions }
}

suspend fun loadAppPositions(context: Context): SnapshotStateList<AppInfo> {
    val preferences = context.dataStore
    val savedPositions = preferences.data.map { it[APP_POSITION_KEY] ?: "" }.first()

    val installedApps = fetchInstalledApps(context)
    val resultApps = if (savedPositions.isNotEmpty()) {
        val packageNames = savedPositions.split(",")
        val sortedApps = packageNames.mapNotNull { packageName ->
            installedApps.find { it.packageName == packageName }
        }
        val newApps = installedApps.filter { it.packageName !in packageNames }
        sortedApps + newApps
    } else {
        installedApps
    }

    return mutableStateListOf(*resultApps.toTypedArray())
}


fun calculateNewIndex(
    offset: Offset,
    index: Int,
    columnCount: Int,
    buttonSizePx: Float,
    totalApps: Int
): Int {
    val tag = "CalculateNewIndex"

    // Oblicz przesunięcie w wierszach i kolumnach
    val rowOffset = ((offset.y / buttonSizePx).coerceIn(-1f, 1f)).toInt()
    val columnOffset = ((offset.x / buttonSizePx).coerceIn(-1f, 1f)).toInt()

    // Obecne współrzędne w siatce
    val currentRow = index / columnCount
    val currentColumn = index % columnCount

    val newRow = (currentRow + rowOffset).coerceIn(0, (totalApps - 1) / columnCount)
    val newColumn = (currentColumn + columnOffset).coerceIn(0, columnCount - 1)

    // Obliczenie nowego indeksu
    val newIndex = newRow * columnCount + newColumn

    Log.d(tag, "Offset: $offset")
    Log.d(tag, "RowOffset: $rowOffset, ColumnOffset: $columnOffset")
    Log.d(tag, "CurrentRow: $currentRow, CurrentColumn: $currentColumn")
    Log.d(tag, "NewRow: $newRow, NewColumn: $newColumn")
    Log.d(tag, "NewIndex: $newIndex")

    // Sprawdzenie, czy indeks jest w granicach listy
    return if (newIndex in 0 until totalApps) {
        Log.d(tag, "NewIndex calculated: $newIndex")
        newIndex
    } else {
        Log.w(tag, "NewIndex out of bounds: $newIndex")
        index
    }
}
fun updateIconsOnDrag(
    apps: MutableList<AppInfo>,
    fromIndex: Int,
    toIndex: Int
) {
    if (fromIndex != toIndex && fromIndex in apps.indices && toIndex in apps.indices) {
        val draggedApp = apps[fromIndex]
        apps.removeAt(fromIndex)
        apps.add(toIndex, draggedApp)
        Log.d("AppUtils", "Icons updated: fromIndex=$fromIndex, toIndex=$toIndex")
    }
}

suspend fun updateBackgroundColor(
    context: Context,
    color: Color,
    onUpdateUI: (Color, Boolean) -> Unit
) {
    onUpdateUI(color, false) // Aktualizuj UI, ustawiając isImageBackground na false
    context.dataStore.edit { preferences ->
        preferences[BACKGROUND_COLOR_KEY] = color.toArgb().toLong()
        preferences.remove(BACKGROUND_IMAGE_URI_KEY) // Usuń URI obrazu tła
    }
}

suspend fun updateButtonSize(context: Context, size: Int, onUpdateUI: (Int) -> Unit) {
    onUpdateUI(size) // Aktualizuj UI
    context.dataStore.edit { preferences ->
        preferences[BUTTON_SIZE_KEY] = size.toLong()
    }
}

suspend fun setBackgroundImage(
    context: Context,
    bitmap: Bitmap?,
    uri: String?,
    onUpdateUI: (Bitmap?, Boolean) -> Unit
) {
    onUpdateUI(bitmap, bitmap != null) // Aktualizuj UI, ustawiając isImageBackground na true
    uri?.let {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_IMAGE_URI_KEY] = it
            preferences.remove(BACKGROUND_COLOR_KEY) // Usuń zapisany kolor tła
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