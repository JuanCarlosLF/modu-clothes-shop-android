package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import kotlinx.coroutines.flow.Flow

interface CartLocalDataSource {
    fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?>
    suspend fun getCartWithItems(): CartWithItemsDbo?
    suspend fun insertCart(cart: CartDbo)
    suspend fun insertCartItems(items: List<CartItemDbo>)
    suspend fun deleteCartItem(itemId: Int)
    suspend fun clearEntireCart()
}