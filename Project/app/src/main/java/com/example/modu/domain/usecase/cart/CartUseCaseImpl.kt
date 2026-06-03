package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

private const val MINIMUM_QUANTITY = 1

class CartUseCaseImpl @Inject constructor(
    private val cartRepository: CartRepository
) : CartUseCase {

    override fun getCartFlow(): Flow<Cart> = cartRepository.getCartFlow()

    override suspend fun addItem(
        productId: Int,
        productVariantId: Int,
        currentStock: Int,
        quantity: Int,
        unitPrice: BigDecimal,
        title: String,
        imageUrl: String,
        size: String,
        color: String
    ) {
        val validQuantity = quantity.coerceIn(MINIMUM_QUANTITY, currentStock)

        val newItem = CartItem(
            id = productId,
            productId = productId,
            productVariantId = productVariantId,
            currentStock = currentStock,
            quantity = validQuantity,
            unitPrice = unitPrice,
            totalPrice = unitPrice.multiply(validQuantity.toBigDecimal()),
            title = title,
            imageUrl = imageUrl,
            size = size,
            color = color
        )

        cartRepository.addItem(newItem)
    }

    override suspend fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity == item.quantity) return
        if (newQuantity < MINIMUM_QUANTITY) return
        if (newQuantity > item.currentStock) return

        val updatedItem = item.copy(
            quantity = newQuantity,
            totalPrice = item.unitPrice.multiply(newQuantity.toBigDecimal())
        )

        cartRepository.updateQuantityLocal(updatedItem, newQuantity)
    }

    override suspend fun deleteItem(id: Int) {
        cartRepository.deleteItem(id)
    }

    override suspend fun clearCart() {
        cartRepository.clearCart()
    }

    override suspend fun checkout(specialInstructions: String): CheckoutResult {
        return cartRepository.checkout(
            isPaid = true,
            specialInstructions = specialInstructions
        )
    }

    override suspend fun verifyPendingChanges() {
        cartRepository.syncCart()
    }
}