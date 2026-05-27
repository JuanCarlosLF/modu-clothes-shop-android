package com.example.modu.data.dataSource.local.preference.cart

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

private const val CART_PREFERENCES_KEY = "cart_prefs"
private const val CART_PREFERENCES_PENDING_SYNC_KEY = "pending_sync"
@Singleton
class CartPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(CART_PREFERENCES_KEY, Context.MODE_PRIVATE)

    fun setPendingSync(hasPending: Boolean) {
        prefs.edit { putBoolean(CART_PREFERENCES_PENDING_SYNC_KEY, hasPending) }
    }

    fun hasPendingSync(): Boolean {
        return prefs.getBoolean(CART_PREFERENCES_PENDING_SYNC_KEY, false)
    }
}