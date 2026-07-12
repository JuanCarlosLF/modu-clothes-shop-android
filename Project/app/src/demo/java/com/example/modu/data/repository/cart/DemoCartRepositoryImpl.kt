package com.example.modu.data.repository.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.repository.cart.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class DemoCartRepositoryImpl @Inject constructor() : CartRepository {

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
        TODO("Not yet implemented")
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