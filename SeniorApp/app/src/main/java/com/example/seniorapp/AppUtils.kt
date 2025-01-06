package com.example.seniorapp

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
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

suspend fun loadAppPositions(context: Context): List<AppInfo> {
    val preferences = context.dataStore
    val savedPositions = preferences.data.map { it[APP_POSITION_KEY] ?: "" }.first()

    val installedApps = fetchInstalledApps(context)
    if (savedPositions.isNotEmpty()) {
        val packageNames = savedPositions.split(",")
        val sortedApps = packageNames.mapNotNull { packageName ->
            installedApps.find { it.packageName == packageName }
        }

        val newApps = installedApps.filter { it.packageName !in packageNames }
        return sortedApps + newApps
    }

    return installedApps
}
