package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val cartDao: CartDao
) : CartLocalDataSource {

    override fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?> = cartDao.getCartWithItemsFlow()

    override suspend fun getCartWithItems(): CartWithItemsDbo? = cartDao.getCartWithItems()

    override suspend fun insertCart(cart: CartDbo) = cartDao.insertCart(cart)

    override suspend fun insertCartItems(items: List<CartItemDbo>) = cartDao.insertItems(items)

    override suspend fun deleteCartItem(itemId: Int) = cartDao.deleteItem(itemId)

    override suspend fun clearEntireCart() = cartDao.clearEntireCart()
}