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
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun updateCart(deviceId: String, items: List<CartItem>): Cart {
        return try {
            val request = UpdateCartRequestDto(
                deviceId = deviceId,
                cartItems = items.map { it.toDto() }
            )
            val response = remoteDataSource.updateCart(request)
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun addItem(deviceId: String, productVariantId: Int, quantity: Int): Cart {
        return try {
            val request = AddItemRequestDto(
                deviceId = deviceId,
                productVariantId = productVariantId,
                quantity = quantity
            )
            val response = remoteDataSource.addItem(request)
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun deleteItemById(itemId: Int, deviceId: String): Cart {
        return try {
            val response = remoteDataSource.deleteItemById(itemId, deviceId)
            response.toDomain()
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun deleteAllItems(deviceId: String): Cart {
        return try {
            val response = remoteDataSource.deleteAllItems(deviceId)
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