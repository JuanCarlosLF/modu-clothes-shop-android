package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import javax.inject.Inject

class CartUseCaseImpl @Inject constructor(
    private val repository: CartRepository
) : CartUseCase {

    override suspend fun getCart(deviceId: String): Cart {
        return repository.getCart(deviceId)
    }

    override suspend fun updateCart(deviceId: String, items: List<CartItem>): Cart {
        return repository.updateCart(deviceId, items)
    }

    override suspend fun syncOfflineCart(deviceId: String): Cart {
        return repository.syncOfflineCart(deviceId)
    }

    override suspend fun addItem(deviceId: String, productVariantId: Int, quantity: Int): Cart {
        return repository.addItem(deviceId, productVariantId, quantity)
    }

    override suspend fun deleteItemById(itemId: Int, deviceId: String): Cart {
        return repository.deleteItemById(itemId, deviceId)
    }

    override suspend fun deleteAllItems(deviceId: String): Cart {
        return repository.deleteAllItems(deviceId)
    }
}