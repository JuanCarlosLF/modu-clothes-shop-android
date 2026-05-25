package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo

interface CartLocalDataSource {
    suspend fun getCartItems(): List<CartItemDbo>
    suspend fun insertCartItems(items: List<CartItemDbo>)
    suspend fun clearCart()
}