package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.mapper.toDbo
import com.example.modu.data.mapper.toDomain
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val cartPreferences: CartPreferences,
    private val errorHandler: ErrorHandler
) : CartRepository {

    override fun getCartItemsFlow(): Flow<List<CartItem>> {
        return localDataSource.getCartItemsFlow().map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun addOrUpdateItemLocal(item: CartItem) {
        try {
            val currentItems = localDataSource.getCartItems()

            val existingDbo = currentItems.find {
                (item.id != 0 && it.id == item.id) ||
                        it.productVariantId == item.productVariantId
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
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun deleteItemLocal(id: Int) {
        try {
            localDataSource.deleteCartItem(id)
            cartPreferences.setPendingSync(true)
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun clearCartLocal() {
        try {
            localDataSource.clearCart()
            cartPreferences.setPendingSync(true)
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }
}