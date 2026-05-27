package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource,
    private val cartPreferences: CartPreferences,
    private val errorHandler: ErrorHandler
) : CartRepository {

    override fun getCartItemsFlow(): Flow<List<CartItem>> {
        return localDataSource.getCartItemsFlow().map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun addItem(item: CartItem) {
        try {
            if (cartPreferences.hasPendingSync()) {
                addOrUpdateItemLocal(item)
                updateCart()
                return
            }

            val request = AddItemRequestDto(item.productVariantId, item.quantity)
            val response = remoteDataSource.addItem(request)

            saveRemoteCartToLocal(response)

        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                addOrUpdateItemLocal(item)
            } else {
                throw handledError
            }
        }
    }

    override suspend fun deleteItem(item: CartItem) {
        try {
            if (item.id < 0 || cartPreferences.hasPendingSync()) {
                deleteItemLocal(item.id)
                updateCart()
                return
            }

            val response = remoteDataSource.deleteItemById(item.id)
            saveRemoteCartToLocal(response)

        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                deleteItemLocal(item.id)
            } else {
                throw handledError
            }
        }
    }

    override suspend fun updateCart() {
        try {
            if (!cartPreferences.hasPendingSync()) return

            val currentLocalCart = localDataSource.getCartItems()
            val request = UpdateCartRequestDto(cartItems = currentLocalCart.map { it.toDto() })
            val response = remoteDataSource.updateCart(request)
            saveRemoteCartToLocal(response)
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun clearCart() {
        try {
            val response = remoteDataSource.clearCart()
            saveRemoteCartToLocal(response)
        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                localDataSource.clearCart()
                cartPreferences.setPendingSync(true)
            } else {
                throw handledError
            }
        }
    }

    private suspend fun saveRemoteCartToLocal(response: CartDto) {
        localDataSource.clearCart()
        response.cartItems?.let { items ->
            localDataSource.insertCartItems(items.map { it.toDbo() })
        }
        cartPreferences.setPendingSync(false)
    }

    private suspend fun addOrUpdateItemLocal(item: CartItem) {
        val currentItems = localDataSource.getCartItems()
        val existingDbo = currentItems.find {
            (item.id != 0 && it.id == item.id) || it.productVariantId == item.productVariantId
        }

        val dboToSave = if (existingDbo != null) {
            item.copy(id = existingDbo.id).toDbo()
        } else {
            val currentMinId = currentItems.minOfOrNull { it.id } ?: 0
            val newId = if (currentMinId < 0) currentMinId - 1 else -1
            item.copy(id = newId).toDbo()
        }

        localDataSource.insertCartItems(listOf(dboToSave))
        cartPreferences.setPendingSync(true)
    }

    private suspend fun deleteItemLocal(id: Int) {
        localDataSource.deleteCartItem(id)
        cartPreferences.setPendingSync(true)
    }
}