package com.example.modu.data.repository.cart

import android.util.Log
import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.data.repository.product.toDomain
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.repository.cart.CartRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val productRemoteDataSource: ProductDataSource,
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

            Log.d("SYNC_CART", "Haciendo la request")
            val request = AddItemRequestDto(item.productVariantId, item.quantity)
            val response = remoteDataSource.addItem(request)
            Log.d("SYNC_CARTrr", "Respuesta $response")

            (Log.d("SYNC_CART", "Guardando en local... $request"))
            saveRemoteCartToLocal(response.toCartDto())
        } catch (error: Exception) {
            Log.e("CART_REPO", "Add item failed", error)
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                addOrUpdateItemLocal(item)
            } else {
                throw handledError
            }
        }
    }

    //llega bien el id
    override suspend fun deleteItem(id: Int) {
        try {
            if (id < DEFAULT_ID || cartPreferences.hasPendingSync()) {
                deleteItemLocal(id)
                updateCart()
                return
            }

            ensureCartExistsOnServer()

            val response = remoteDataSource.deleteItemById(id)
            saveRemoteCartToLocal(response.toCartDto())

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

            Log.d("UPDATE_CART", "=== START updateCart ===")

            val hasPending = cartPreferences.hasPendingSync()
            Log.d("UPDATE_CART", "hasPendingSync=$hasPending")

            if (!hasPending) {
                Log.d("UPDATE_CART", "EXIT: no pending sync")
                return
            }

            Log.d("UPDATE_CART", "Ensuring cart exists on server")
            ensureCartExistsOnServer()

            Log.d("UPDATE_CART", "Fetching cart from local DB")
            val cartWithItems = localDataSource.getCartWithItems()

            Log.d(
                "UPDATE_CART",
                "cartFromDB=${cartWithItems != null}"
            )

            val currentItems = cartWithItems?.items ?: emptyList()

            Log.d(
                "UPDATE_CART",
                "localItemsCount=${currentItems.size}"
            )

            currentItems.forEachIndexed { index, item ->
                Log.d(
                    "UPDATE_CART",
                    "LOCAL_ITEM[$index] variantId=${item.cartItem.productVariantId} quantity=${item.cartItem.quantity}"
                )
            }

            val shipping = cartWithItems?.cart?.shippingCost ?: BigDecimal.ZERO

            Log.d(
                "UPDATE_CART",
                "shipping=$shipping"
            )

            Log.d("UPDATE_CART", "Mapping DTO request")

            val request = UpdateCartRequestDto(
                cartItems = currentItems.map { it.toDomain().toDto() },
                shippingCosts = shipping
            )

            Log.d(
                "UPDATE_CART",
                "requestItems=${request.cartItems?.size ?: 0}"
            )

            Log.d("UPDATE_CART", "Calling remote updateCart API")

            val response = remoteDataSource.updateCart(request)

            Log.d(
                "UPDATE_CART",
                "API response received = $response"
            )

            Log.d("UPDATE_CART", "Saving remote cart to local DB")

            saveRemoteCartToLocal(response)

            Log.d("UPDATE_CART", "=== END updateCart SUCCESS ===")

        } catch (error: Exception) {

            Log.e("UPDATE_CART", "ERROR in updateCart", error)

            throw errorHandler.handle(error)
        }
    }

    override suspend fun clearCart() {
        try {
            ensureCartExistsOnServer()

            val response = remoteDataSource.clearCart()
            saveRemoteCartToLocal(response.toCartDto())
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
        localDataSource.clearCart()
        localDataSource.insertCart(response.toCartDbo())

        val itemsDto = response.cartSummary?.cartItems ?: emptyList()
        itemsDto.forEachIndexed { index, item ->
            Log.d(
                "CART_SYNC",
                "Item[$index] variantId=${item.productVariantId} quantity=${item.quantity}"
            )
        }

        fetchAndCacheMissingProducts(itemsDto)

        val priceAlerts =
            response.priceChangedAlert?.cartItems?.associateBy { it.productVariantId }

        val stockAlerts =
            response.insufficientStockAlert?.cartItems?.associateBy { it.productVariantId }

        val availAlerts =
            response.variantAvailabilityAlert?.cartItems?.associateBy { it.productVariantId }
        val mergedDbos = itemsDto.map { dto ->

            val variantId = dto.productVariantId ?: DEFAULT_ID

            dto.toDbo().copy(
                oldPriceAlert = priceAlerts?.get(variantId)?.oldPrice,
                stockAlertAvailable = stockAlerts?.get(variantId)?.availableStock,
                isAvailableAlert = availAlerts?.get(variantId)?.isVariantAvailable
            )
        }

        Log.d(
            "CART_SYNC",
            "Merged dbos count=${mergedDbos.size}"
        )

        Log.d("CART_SYNC", "Inserting cart items")
        localDataSource.insertCartItems(mergedDbos)

        Log.d("CART_SYNC", "Setting pendingSync=false")
        cartPreferences.setPendingSync(false)

        val localCart = localDataSource.getCartWithItems()

        Log.d(
            "CART_SYNC",
            "Local cart after save -> items=${localCart?.items?.size ?: 0}"
        )

        localCart?.items?.forEachIndexed { index, item ->
            Log.d(
                "CART_SYNC",
                "LocalItem[$index] id=${item.cartItem.id} variantId=${item.cartItem.productVariantId} quantity=${item.cartItem.quantity}"
            )
        }

        Log.d("CART_SYNC", "=== END saveRemoteCartToLocal ===")
    }

    private suspend fun addOrUpdateItemLocal(item: CartItem) {
        val detailDbo = CartItemDetailDbo(
            id = item.productId,
            name = item.title,
            imageUrl = item.imageUrl
        )
        val variantDbo = ProductVariantDbo(
            id = item.productVariantId,
            productId = item.productId,
            size = item.size,
            color = item.color
        )

        localDataSource.insertProductDetails(listOf(detailDbo))
        localDataSource.insertProductVariants(listOf(variantDbo))

        val currentItems = localDataSource.getCartWithItems()?.items ?: emptyList()
        val existingDbo = currentItems.find {
            (item.id != DEFAULT_ID && it.cartItem.id == item.id) || it.cartItem.productVariantId == item.productVariantId
        }

        val dboToSave = if (existingDbo != null) {
            item.copy(id = existingDbo.cartItem.id).toDbo()
        } else {
            val currentMinId = currentItems.minOfOrNull { it.cartItem.id } ?: DEFAULT_ID
            val newId =
                if (currentMinId < DEFAULT_ID) currentMinId - ID_DECREMENT_STEP else FALLBACK_NEW_ID
            item.copy(id = newId).toDbo()
        }

        localDataSource.insertCartItems(listOf(dboToSave))
        cartPreferences.setPendingSync(true)
    }

    private suspend fun deleteItemLocal(id: Int) {
        localDataSource.deleteCartItem(id)
        cartPreferences.setPendingSync(true)
    }

    private suspend fun fetchAndCacheMissingProducts(itemsDto: List<CartItemDto>) {
        val allProductIdsInCart = itemsDto.mapNotNull { it.productId }.distinct()
        val existingIds = localDataSource.getExistingProductDetails(allProductIdsInCart)
        val missingIds = allProductIdsInCart - existingIds.toSet()

        if (missingIds.isEmpty()) return

        coroutineScope {
            missingIds.map { id ->
                async {
                    try {
                        val detailDto = productRemoteDataSource.getDetailById(id)
                        val domainModel = detailDto.toDomain()

                        val detailDbo = CartItemDetailDbo(
                            id = domainModel.id,
                            name = domainModel.name,
                            imageUrl = domainModel.imageUrl
                        )
                        val variantDbos = domainModel.productVariantsList.map {
                            ProductVariantDbo(
                                id = it.id,
                                productId = domainModel.id,
                                size = it.size,
                                color = it.color
                            )
                        }

                        localDataSource.insertProductDetails(listOf(detailDbo))
                        localDataSource.insertProductVariants(variantDbos)
                    } catch (e: Exception) {
                    }
                }
            }.awaitAll()
        }
    }
}