package com.example.seniorapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class AppChangeHandler(
    private val context: Context,
    private val onAppListUpdated: (List<AppInfo>) -> Unit
) {

    private val appChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_PACKAGE_ADDED ||
                action == Intent.ACTION_PACKAGE_REMOVED ||
                action == Intent.ACTION_PACKAGE_CHANGED) {
                val updatedApps = fetchInstalledApps(context)
                onAppListUpdated(updatedApps)
            }
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(appChangeReceiver, filter)
    }

    fun unregister() {
        context.unregisterReceiver(appChangeReceiver)
    }
}
