package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem

interface CartUseCase {
    suspend fun getCart(deviceId: String): Cart
    suspend fun updateCart(deviceId: String, items: List<CartItem>): Cart
    suspend fun addItem(deviceId: String, productVariantId: Int, quantity: Int): Cart
    suspend fun deleteItemById(itemId: Int, deviceId: String): Cart
    suspend fun deleteAllItems(deviceId: String): Cart
    suspend fun syncOfflineCart(deviceId: String): Cart
}