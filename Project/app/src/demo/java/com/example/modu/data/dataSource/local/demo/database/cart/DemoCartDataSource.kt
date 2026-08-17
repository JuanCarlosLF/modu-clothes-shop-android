package com.example.modu.data.dataSource.local.demo.database.cart

import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import kotlinx.coroutines.flow.Flow

interface DemoCartDataSource {
    suspend fun createCart(cart: CartDbo)
    suspend fun getCart(cartId: Int): CartDbo?
    fun observeCartItems(cartId: Int): Flow<List<CartItemDetailDbo>>
    suspend fun addItem(cartItem: CartItemDbo)
    suspend fun getCartItem(variantId: Int, cartId: Int): CartItemDbo?
    suspend fun updateItemQuantity(itemId: Int, quantity: Int)
    suspend fun deleteItemById(itemId: Int)
    suspend fun clearCart(cartId: Int)
}
