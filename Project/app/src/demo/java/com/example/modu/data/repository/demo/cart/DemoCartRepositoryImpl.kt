package com.example.modu.data.repository.demo.cart

import com.example.modu.data.dataSource.local.demo.database.cart.DemoCartDataSource
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.repository.cart.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val CART_ID = 1
private const val CART_DEFAULT_CREATE_DATE = ""
private const val CART_DEFAULT_UPDATE_DATE = ""

class DemoCartRepositoryImpl @Inject constructor(
    private val dataSource: DemoCartDataSource
) : CartRepository {

    override fun getCartFlow(): Flow<Cart> = flow {
        val cartDbo: CartDbo = dataSource.getCart(CART_ID) ?: run {
            syncCart()
            checkNotNull(dataSource.getCart(CART_ID)) { "Expected cart to exist" }
        }

        dataSource.observeCartItems(cartDbo.id).collect { itemDbos ->
            val cartItems = itemDbos.map { it.toDomain() }
            val cart = cartDbo.toDomain(cartItems)
            emit(cart)
        }
    }

    override suspend fun addItem(item: CartItem) {
        syncCart()

        val existingItem: CartItemDbo? = dataSource.getCartItem(item.productVariantId, CART_ID)
        if (existingItem != null) {
            val existingItemQuantity = existingItem.quantity
            val requestedItemQuantity = item.quantity
            val normalizedQuantity =
                (existingItemQuantity + requestedItemQuantity).coerceAtMost(item.currentStock)
            updateQuantity(existingItem.id, normalizedQuantity)
        } else {
            dataSource.addItem(item.toDbo(CART_ID, item.quantity))
        }

    }

    override suspend fun checkout(
        isPaid: Boolean,
        specialInstructions: String
    ): CheckoutResult = CheckoutResult.SUCCESS

    override suspend fun clearCart() =
        dataSource.clearCart(CART_ID)

    override suspend fun deleteItem(id: Int) =
        dataSource.deleteItemById(id)

    override suspend fun syncCart() {
        dataSource.createCart(
            CartDbo(
                id = CART_ID,
                createdAt = CART_DEFAULT_CREATE_DATE,
                updatedAt = CART_DEFAULT_UPDATE_DATE
            )
        )
    }

    override suspend fun updateCart() {
        TODO("Not yet implemented")
    }

    override suspend fun updateQuantityLocal(item: CartItem) =
        dataSource.updateItemQuantity(item.id, item.quantity)

    private suspend fun updateQuantity(itemId: Int, quantity: Int) =
        dataSource.updateItemQuantity(itemId, quantity)
}
