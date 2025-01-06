package com.example.seniorapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val BACKGROUND_COLOR_KEY = longPreferencesKey("background_color")
val BUTTON_SIZE_KEY = longPreferencesKey("button_size")
val BACKGROUND_IMAGE_URI_KEY = stringPreferencesKey("background_image_uri")
val APP_POSITION_KEY = stringPreferencesKey("app_position_key")

