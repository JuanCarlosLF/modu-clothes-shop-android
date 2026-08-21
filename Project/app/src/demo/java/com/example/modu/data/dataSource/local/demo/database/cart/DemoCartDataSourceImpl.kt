package com.example.modu.data.dataSource.local.demo.database.cart

import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DemoCartDataSourceImpl @Inject constructor(private val dao: CartDao) :
    DemoCartDataSource {

    override suspend fun createCart(cart: CartDbo) =
        dao.createCart(cart = cart)

    override suspend fun getCart(cartId: Int): CartDbo? =
        dao.getCart(cartId = cartId)

    override fun observeCartItems(cartId: Int): Flow<List<CartItemDetailDbo>> =
        dao.observeCartItems(cartId)

    override suspend fun addItem(cartItem: CartItemDbo) =
        dao.addItem(cartItem)

    override suspend fun getCartItem(variantId: Int, cartId: Int): CartItemDbo? =
        dao.getCartItem(variantId, cartId)

    override suspend fun updateItemQuantity(itemId: Int, quantity: Int) =
        dao.updateQuantity(itemId, quantity)

    override suspend fun deleteItemById(itemId: Int) =
        dao.deleteItemById(itemId)

    override suspend fun clearCart(cartId: Int) =
        dao.clearCart(cartId)
}
