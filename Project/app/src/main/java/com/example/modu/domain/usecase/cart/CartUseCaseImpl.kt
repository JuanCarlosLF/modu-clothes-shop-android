package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
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
            id = 0,
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

        try {
            cartRepository.addItem(newItem)
        } catch (error: AppError) {
            throw handleNetworkError(error)
        }
    }

    override suspend fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity == item.quantity) return
        if (newQuantity < MINIMUM_QUANTITY) return
        if (newQuantity > item.currentStock) return

        val updatedItem = item.copy(
            quantity = newQuantity,
            totalPrice = item.unitPrice.multiply(newQuantity.toBigDecimal())
        )

        try {
            cartRepository.addItem(updatedItem)
        } catch (error: AppError) {
            throw handleNetworkError(error)
        }
    }

    override suspend fun deleteItem(id: Int) {
        try {
            cartRepository.deleteItem(id)
        } catch (error: AppError) {
            throw handleNetworkError(error)
        }
    }

    override suspend fun clearCart() {
        try {
            cartRepository.clearCart()
        } catch (error: AppError) {
            throw handleNetworkError(error)
        }
    }

    private fun handleNetworkError(error: AppError): Exception {
        return if (error.type == ErrorType.NO_INTERNET) {
            error.copy(
                title = "Sincronización pendiente",
                message = "No hemos podido sincronizar tus cambios con el servidor.\nTus cambios se guardarán en el dispositivo"
            )
        } else {
            error
        }
    }
}