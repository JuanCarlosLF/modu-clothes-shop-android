package com.example.modu.domain.repository.cart

import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItemsFlow(): Flow<List<CartItem>>
    suspend fun addOrUpdateItemLocal(item: CartItem)
    suspend fun deleteItemLocal(id: Int)
    suspend fun clearCartLocal()
}