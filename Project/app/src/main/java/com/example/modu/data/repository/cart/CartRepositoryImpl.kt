package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import jakarta.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val remoteDataSource: CartRemoteDataSource,
    private val localDataSource: CartLocalDataSource,
    private val errorHandler: ErrorHandler
) : CartRepository {

    override suspend fun getCart(deviceId: String): Cart {
        return try {
            val response = remoteDataSource.getCart(deviceId)
            localDataSource.clearCart()
            localDataSource.insertCartItems(response.cartItems?.map { it.toDbo() } ?: emptyList())
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun updateCart(deviceId: String, items: List<CartItem>): Cart {
        return try {
            localDataSource.insertCartItems(items.map { it.toDbo() })
            val localItems = localDataSource.getCartItems().map { it.toDomain() }
            Cart(
                deviceId = deviceId,
                createdAt = "",
                updatedAt = "",
                items = localItems,
                priceAlert = null,
                stockAlert = null
            )
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun syncOfflineCart(deviceId: String): Cart {
        return try {
            val localItems = localDataSource.getCartItems()
            val request = UpdateCartRequestDto(
                deviceId = deviceId,
                cartItems = localItems.map { it.toDto() }
            )
            val response = remoteDataSource.updateCart(request)
            localDataSource.clearCart()
            localDataSource.insertCartItems(response.cartItems?.map { it.toDbo() } ?: emptyList())
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun addItem(deviceId: String, productVariantId: Int, quantity: Int): Cart {
        return try {
            val request = AddItemRequestDto(deviceId, productVariantId, quantity)
            val response = remoteDataSource.addItem(request)
            localDataSource.clearCart()
            localDataSource.insertCartItems(response.cartItems?.map { it.toDbo() } ?: emptyList())
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun deleteItemById(itemId: Int, deviceId: String): Cart {
        return try {
            val response = remoteDataSource.deleteItemById(itemId, deviceId)
            localDataSource.clearCart()
            localDataSource.insertCartItems(response.cartItems?.map { it.toDbo() } ?: emptyList())
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun deleteAllItems(deviceId: String): Cart {
        return try {
            val response = remoteDataSource.deleteAllItems(deviceId)
            localDataSource.clearCart()
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun getLocalCartItems(): List<CartItem> {
        return try {
            localDataSource.getCartItems().map { it.toDomain() }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun saveLocalCartItems(items: List<CartItem>) {
        try {
            localDataSource.insertCartItems(items.map { it.toDbo() })
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun clearLocalCart() {
        try {
            localDataSource.clearCart()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }
}