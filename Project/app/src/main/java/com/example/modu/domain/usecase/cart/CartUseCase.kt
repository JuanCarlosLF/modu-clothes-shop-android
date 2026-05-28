package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.CartAlertEvent
import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CartUseCase {
    val cartAlertsFlow: Flow<CartAlertEvent>
    fun getCartFlow(): Flow<List<CartItem>>
    suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal
    )
    suspend fun updateQuantity(item: CartItem, newQuantity: Int)
    suspend fun deleteItemById(item: CartItem)
    suspend fun clearCart()
}