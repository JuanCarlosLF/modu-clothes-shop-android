package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

private const val MINIMUM_QUANTITY = 1

class CartUseCaseImpl @Inject constructor(
    private val cartRepository: CartRepository
) : CartUseCase {

    override fun getCartFlow(): Flow<Cart> {
        return cartRepository.getCartFlow()
    }

    override suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal
    ) {
        val newItem = CartItem(
            id = 0,
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

    override suspend fun deleteItem(id: Int) {
        cartRepository.deleteItem(id)
    }

    override suspend fun clearCart() {
        cartRepository.clearCart()
    }
}