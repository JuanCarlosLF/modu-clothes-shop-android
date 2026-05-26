package com.example.modu.data.dataSource.local.preference

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveDeviceId(deviceId: String) {
        prefs.edit { putString("device_id", deviceId) }
    }

    fun getDeviceId(): String? {
        return prefs.getString("device_id", null)
    }
}