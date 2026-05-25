package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import jakarta.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val cartDao: CartDao
) : CartLocalDataSource {

    override suspend fun getCartItems(): List<CartItemDbo> = cartDao.getCartItems()

    override suspend fun insertCartItems(items: List<CartItemDbo>) = cartDao.insertItems(items)

    override suspend fun clearCart() = cartDao.clearCart()
}