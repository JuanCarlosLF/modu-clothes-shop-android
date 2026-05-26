package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val cartDao: CartDao
) : CartLocalDataSource {

    override fun getCartItemsFlow(): Flow<List<CartItemDbo>> = cartDao.getCartItemsFlow()

    override suspend fun getCartItems(): List<CartItemDbo> = cartDao.getCartItems()

    override suspend fun insertCartItems(items: List<CartItemDbo>) = cartDao.insertItems(items)

    override suspend fun deleteCartItem(itemId: Int) = cartDao.deleteItem(itemId)

    override suspend fun clearCart() = cartDao.clearCart()
}