package com.example.modu.domain.repository.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartFlow(): Flow<Cart>
    suspend fun addItem(item: CartItem)
    suspend fun deleteItem(id: Int)
    suspend fun updateCart()
    suspend fun clearCart()
}