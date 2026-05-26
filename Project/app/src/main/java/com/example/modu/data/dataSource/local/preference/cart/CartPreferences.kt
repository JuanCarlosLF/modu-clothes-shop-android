package com.example.modu.data.dataSource.local.preference.cart

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class CartPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)

    fun setPendingSync(hasPending: Boolean) {
        prefs.edit { putBoolean("pending_sync", hasPending) }
    }

    fun hasPendingSync(): Boolean {
        return prefs.getBoolean("pending_sync", false)
    }
}