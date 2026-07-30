package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.demo.database.cart.DemoCartDataSource
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.repository.cart.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

private const val CART_ID = 1
private const val CART_DEFAULT_CREATE_DATE = ""
private const val CART_DEFAULT_UPDATE_DATE = ""

class DemoCartRepositoryImpl @Inject constructor(
    private val dataSource: DemoCartDataSource
) : CartRepository {

    override suspend fun addItem(item: CartItem) {
        TODO("Not yet implemented")
    }

    override suspend fun checkout(isPaid: Boolean, specialInstructions: String): CheckoutResult {
        TODO("Not yet implemented")
    }

    override suspend fun clearCart() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItem(id: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun syncCart() {
        dataSource.createCart(
            CartDbo(
                id = CART_ID,
                createdAt = CART_DEFAULT_CREATE_DATE,
                updatedAt = CART_DEFAULT_UPDATE_DATE
            )
        )
    }

    override fun getCartFlow(): Flow<Cart> {
        TODO("Not yet implemented")
    }

    override suspend fun updateCart() {
        TODO("Not yet implemented")
    }

    override suspend fun updateQuantityLocal(item: CartItem) {
        TODO("Not yet implemented")
    }
}