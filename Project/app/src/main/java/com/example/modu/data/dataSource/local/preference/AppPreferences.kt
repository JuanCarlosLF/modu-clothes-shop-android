package com.example.modu.data.dataSource.local.preference

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

private const val APP_PREFERENCES_KEY = "app_prefs"
private const val APP_PREFERENCES_DEVICE_KEY = "device_id"

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE)

    fun saveDeviceId(deviceId: String) {
        prefs.edit { putString(APP_PREFERENCES_DEVICE_KEY, deviceId) }
    }

    fun getDeviceId(): String? {
        return prefs.getString(APP_PREFERENCES_DEVICE_KEY, null)
    }
}