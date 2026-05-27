package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import kotlinx.coroutines.flow.Flow

interface CartLocalDataSource {
    fun getCartItemsFlow(): Flow<List<CartItemDbo>>
    suspend fun getCartItems(): List<CartItemDbo>
    suspend fun insertCartItems(items: List<CartItemDbo>)
    suspend fun deleteCartItem(itemId: Int)
    suspend fun clearCart()
}