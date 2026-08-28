package com.juancarloslf.modu.domain.usecase.cart

import com.juancarloslf.modu.domain.entity.cart.Cart
import com.juancarloslf.modu.domain.entity.cart.CartItem
import com.juancarloslf.modu.domain.entity.cart.CheckoutResult
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CartUseCase {
    fun getCartFlow(): Flow<Cart>

    suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal,
        title: String,
        imageUrl: String,
        size: String,
        color: String
    )

    suspend fun updateQuantity(item: CartItem, newQuantity: Int)
    suspend fun deleteItem(id: Int)
    suspend fun clearCart()
    suspend fun checkout(specialInstructions: String): CheckoutResult
    suspend fun verifyPendingChanges()
}