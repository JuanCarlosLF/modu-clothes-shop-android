package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.CartAlertEvent
import com.example.modu.domain.repository.cart.CartRepository
import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

private const val MINIMUM_QUANTITY = 1

class CartUseCaseImpl @Inject constructor(
    private val cartRepository: CartRepository
) : CartUseCase {

    override val cartAlertsFlow: Flow<CartAlertEvent>
        get() = cartRepository.cartAlertsFlow

    override fun getCartFlow(): Flow<List<CartItem>> {
        return cartRepository.getCartItemsFlow()
    }

    override suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal
    ) {
        val newItem = CartItem(
            productId = productId,
            productVariantId = productVariantId,
            currentStock = currentStock,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = unitPrice.multiply(quantity.toBigDecimal())
        )
        cartRepository.addItem(newItem)
    }

    override suspend fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity < MINIMUM_QUANTITY) return

        val updatedItem = item.copy(
            quantity = newQuantity,
            totalPrice = item.unitPrice.multiply(newQuantity.toBigDecimal())
        )
        cartRepository.addItem(updatedItem)
    }

    override suspend fun deleteItemById(item: CartItem) {
        cartRepository.deleteItem(item)
    }

    override suspend fun clearCart() {
        cartRepository.clearCart()
    }
}