package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CartUseCase {
    fun getCartFlow(): Flow<List<CartItem>>
    suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal
    )

    suspend fun updateQuantity(item: CartItem, newQuantity: Int)
    suspend fun deleteItem(id: Int)
    suspend fun clearCart()
    suspend fun updateCart()
}