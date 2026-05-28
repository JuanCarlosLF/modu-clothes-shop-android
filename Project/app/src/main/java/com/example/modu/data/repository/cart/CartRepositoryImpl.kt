package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.mapper.toDbo
import com.example.modu.data.mapper.toDomain
import com.example.modu.data.mapper.toDto
import com.example.modu.domain.entity.cart.CartAlertEvent
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val ALERT_BUFFER_CAPACITY = 10
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

    private val _cartAlertsFlow = MutableSharedFlow<CartAlertEvent>(extraBufferCapacity = ALERT_BUFFER_CAPACITY)
    override val cartAlertsFlow: Flow<CartAlertEvent> = _cartAlertsFlow.asSharedFlow()

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

    override suspend fun deleteItem(item: CartItem) {
        try {
            if (item.id < DEFAULT_ID || cartPreferences.hasPendingSync()) {
                deleteItemLocal(item.id)
                updateCart()
                return
            }

            ensureCartExistsOnServer()

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

            ensureCartExistsOnServer()

            val currentLocalCart = localDataSource.getCartItems()
            val request = UpdateCartRequestDto(cartItems = currentLocalCart.map { it.toDomain().toDto() })
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
                localDataSource.clearCart()
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
        updateLocalCartItems(response)
        emitCartAlertsIfNeeded(response)
        cartPreferences.setPendingSync(false)
    }

    private suspend fun updateLocalCartItems(response: CartDto) {
        localDataSource.clearCart()
        response.cartItems?.let { items ->
            localDataSource.insertCartItems(items.map { it.toDbo() })
        }
    }

    private suspend fun emitCartAlertsIfNeeded(response: CartDto) {
        response.priceChangedAlert?.toDomain()?.let { alert ->
            if (alert.items.isNotEmpty()) {
                _cartAlertsFlow.emit(CartAlertEvent.PriceChanged(alert))
            }
        }

        response.insufficientStockAlert?.toDomain()?.let { alert ->
            if (alert.items.isNotEmpty()) {
                _cartAlertsFlow.emit(CartAlertEvent.StockInsufficient(alert))
            }
        }

        response.variantAvailabilityAlert?.toDomain()?.let { alert ->
            if (alert.cartItems.isNotEmpty()) {
                _cartAlertsFlow.emit(CartAlertEvent.VariantUnavailable(alert))
            }
        }
    }

    private suspend fun addOrUpdateItemLocal(item: CartItem) {
        val currentItems = localDataSource.getCartItems()
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