package com.example.modu.domain.repository.cart

import com.example.modu.domain.entity.cart.CartAlertEvent
import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartAlertsFlow: Flow<CartAlertEvent>
    fun getCartItemsFlow(): Flow<List<CartItem>>
    suspend fun addItem(item: CartItem)
    suspend fun deleteItem(item: CartItem)
    suspend fun clearCart()
    suspend fun updateCart()
}