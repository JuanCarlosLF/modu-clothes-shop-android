package com.juancarloslf.modu.data.dataSource.local.preference

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_KEY = "modu_app_prefs"
private const val KEY_CART_GENERATED_PREFIX = "cart_generated_"

@Singleton
class AppPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    fun getDeviceId(): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun isCartGenerated(): Boolean {
        val deviceId = getDeviceId() ?: return false
        return prefs.getBoolean("$KEY_CART_GENERATED_PREFIX$deviceId", false)
    }
    fun setCartGenerated(isGenerated: Boolean) {
        val deviceId = getDeviceId() ?: return
        prefs.edit().putBoolean("$KEY_CART_GENERATED_PREFIX$deviceId", isGenerated).apply()
    }
}
