package com.example.modu.domain.repository.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem

interface CartRepository {
    suspend fun getCart(deviceId: String): Cart
    suspend fun updateCart(deviceId: String, items: List<CartItem>): Cart
    suspend fun addItem(deviceId: String, productVariantId: Int, quantity: Int): Cart
    suspend fun deleteItemById(itemId: Int, deviceId: String): Cart
    suspend fun deleteAllItems(deviceId: String): Cart
    suspend fun getLocalCartItems(): List<CartItem>
    suspend fun saveLocalCartItems(items: List<CartItem>)
    suspend fun clearLocalCart()
    suspend fun syncOfflineCart(deviceId: String): Cart
}