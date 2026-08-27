package com.juancarloslf.modu.domain.repository.cart

import com.juancarloslf.modu.domain.entity.cart.Cart
import com.juancarloslf.modu.domain.entity.cart.CartItem
import com.juancarloslf.modu.domain.entity.cart.CheckoutResult
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartFlow(): Flow<Cart>
    suspend fun addItem(item: CartItem)
    suspend fun deleteItem(id: Int)
    suspend fun updateCart()
    suspend fun clearCart()
    suspend fun checkout(isPaid: Boolean, specialInstructions: String): CheckoutResult
    suspend fun syncCart()
    suspend fun updateQuantityLocal(item: CartItem)}