package com.example.modu.domain.usecase.cart

import com.example.modu.domain.repository.cart.CartRepository
import com.example.modu.domain.entity.cart.CartItem
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

class CartUseCaseImpl @Inject constructor(
    private val cartRepository: CartRepository
) : CartUseCase {

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
        cartRepository.addOrUpdateItemLocal(newItem)
    }

    override suspend fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity < 1) return

        val updatedItem = item.copy(
            quantity = newQuantity,
            totalPrice = item.unitPrice.multiply(newQuantity.toBigDecimal())
        )
        cartRepository.addOrUpdateItemLocal(updatedItem)
    }

    override suspend fun deleteItem(id: Int) {
        cartRepository.deleteItemLocal(id)
    }

    override suspend fun clearCart() {
        cartRepository.clearCartLocal()
    }
}