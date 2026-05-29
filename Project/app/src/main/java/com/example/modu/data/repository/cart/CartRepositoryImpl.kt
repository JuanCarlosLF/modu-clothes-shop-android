package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

private const val DEFAULT_ID = 0
private const val FALLBACK_NEW_ID = -1
private const val ID_DECREMENT_STEP = 1

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val remoteDataSource: CartRemoteDataSource,
    private val cartPreferences: CartPreferences,
    private val appPreferences: AppPreferences,
    private val errorHandler: ErrorHandler
) : CartRepository {

    override fun getCartFlow(): Flow<Cart> {
        return localDataSource.getCartWithItemsFlow().map { cartWithItemsDbo ->
            cartWithItemsDbo?.toDomain() ?: Cart(
                createdAt = "",
                updatedAt = "",
                subTotal = BigDecimal.ZERO,
                shippingCost = BigDecimal.ZERO,
                total = BigDecimal.ZERO,
                items = emptyList()
            )
        }
    }

    override suspend fun addItem(item: CartItem) {
        try {
            if (cartPreferences.hasPendingSync()) {
                addOrUpdateItemLocal(item)
                updateCart()
                return
            }

            ensureCartExistsOnServer()

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

    override suspend fun deleteItem(id: Int) {
        try {
            if (id < DEFAULT_ID || cartPreferences.hasPendingSync()) {
                deleteItemLocal(id)
                updateCart()
                return
            }

            ensureCartExistsOnServer()

            val response = remoteDataSource.deleteItemById(id)
            saveRemoteCartToLocal(response)

        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                deleteItemLocal(id)
            } else {
                throw handledError
            }
        }
    }

    override suspend fun updateCart() {
        try {
            if (!cartPreferences.hasPendingSync()) return

            ensureCartExistsOnServer()
            val currentItems = localDataSource.getCartWithItems()?.items ?: emptyList()
            val request = UpdateCartRequestDto(cartItems = currentItems.map { it.toDomain().toDto() })
            val response = remoteDataSource.updateCart(request)

            saveRemoteCartToLocal(response)
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun clearCart() {
        try {
            ensureCartExistsOnServer()

            val response = remoteDataSource.clearCart()
            saveRemoteCartToLocal(response)
        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                localDataSource.clearEntireCart()
                cartPreferences.setPendingSync(true)
            } else {
                throw handledError
            }
        }
    }

    private suspend fun ensureCartExistsOnServer() {
        if (appPreferences.isCartGenerated()) return

        val request = UpdateCartRequestDto(cartItems = emptyList())
        remoteDataSource.updateCart(request)

        appPreferences.setCartGenerated(true)
    }

    private suspend fun saveRemoteCartToLocal(response: CartDto) {
        localDataSource.clearEntireCart()

        localDataSource.insertCart(response.toCartDbo())

        val itemsDto = response.cartItems ?: emptyList()
        val priceAlerts = response.priceChangedAlert?.cartItems?.associateBy { it.productVariantId }
        val stockAlerts = response.insufficientStockAlert?.cartItems?.associateBy { it.productVariantId }
        val availAlerts = response.variantAvailabilityAlert?.cartItems?.associateBy { it.productVariantId }

        val mergedDbos = itemsDto.map { dto ->
            val variantId = dto.productVariantId ?: DEFAULT_ID
            dto.toDbo().copy(
                oldPriceAlert = priceAlerts?.get(variantId)?.oldPrice,
                stockAlertAvailable = stockAlerts?.get(variantId)?.availableStock,
                isAvailableAlert = availAlerts?.get(variantId)?.isVariantAvailable
            )
        }

        localDataSource.insertCartItems(mergedDbos)
        cartPreferences.setPendingSync(false)
    }

    private suspend fun addOrUpdateItemLocal(item: CartItem) {
        val currentItems = localDataSource.getCartWithItems()?.items ?: emptyList()
        val existingDbo = currentItems.find {
            (item.id != DEFAULT_ID && it.id == item.id) || it.productVariantId == item.productVariantId
        }

        val dboToSave = if (existingDbo != null) {
            item.copy(id = existingDbo.id).toDbo()
        } else {
            val currentMinId = currentItems.minOfOrNull { it.id } ?: DEFAULT_ID
            val newId = if (currentMinId < DEFAULT_ID) currentMinId - ID_DECREMENT_STEP else FALLBACK_NEW_ID
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