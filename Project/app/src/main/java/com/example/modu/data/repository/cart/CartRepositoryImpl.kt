package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.CartSummaryDto
import com.example.modu.data.dataSource.remote.cart.dto.InsufficientStockAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceChangedAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.StockAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityItemDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.data.repository.product.toDomain
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
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
            cacheItemDetailsLocally(item)

            if (cartPreferences.hasPendingSync()) {
                addOrUpdateItemLocal(item)
                updateCart()
                return
            }

            addOrUpdateItemLocal(item)

            ensureCartExistsOnServer()

            val request = AddItemRequestDto(item.productVariantId, item.quantity)
            val response = remoteDataSource.addItem(request)

            saveRemoteCartToLocal(response.toCartDto())
        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError !is AppError || handledError.type != ErrorType.NO_INTERNET) {
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
            if (!cartPreferences.hasPendingSync()) return

            ensureCartExistsOnServer()

            val cartWithItems = localDataSource.getCartWithItems()
            val currentItems = cartWithItems?.items ?: emptyList()
            val shipping = cartWithItems?.cart?.shippingCost ?: BigDecimal.ZERO

            val request = UpdateCartRequestDto(
                cartItems = currentItems.map { it.toDomain().toDto() },
                shippingCosts = shipping
            )

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
            saveRemoteCartToLocal(response.toCartDto())
        } catch (error: Exception) {
            val handledError = errorHandler.handle(error)
            if (handledError is AppError && handledError.type == ErrorType.NO_INTERNET) {
                localDataSource.clearCart()
                appPreferences.setCartGenerated(false)
                cartPreferences.setPendingSync(true)
            } else {
                throw handledError
            }
        }
    }

    override suspend fun checkout(isPaid: Boolean, specialInstructions: String): CheckoutResult {
        try {
            ensureCartExistsOnServer()

            val cartWithItems = localDataSource.getCartWithItems() ?: return CheckoutResult.ALERTS_TRIGGERED
            val cart = cartWithItems.toDomain()

            if (specialInstructions == "MOCK_ALERTS") {
                val mockResponse = generateMockAlertResponse(cartWithItems)
                saveRemoteCartToLocal(mockResponse)
                return CheckoutResult.ALERTS_TRIGGERED
            }

            val request = cart.toCheckoutRequestDto(isPaid, specialInstructions)
            val response = remoteDataSource.checkout(request)

            return if (response.orderPlaced == true) {
                localDataSource.clearCart()
                CheckoutResult.SUCCESS
            } else {
                saveRemoteCartToLocal(requireNotNull(response.cartResponse))
                CheckoutResult.ALERTS_TRIGGERED
            }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun syncCart() {
        try {
            if (cartPreferences.hasPendingSync()) {
                updateCart()
                return
            }
            ensureCartExistsOnServer()
            val response = remoteDataSource.getCart()
            saveRemoteCartToLocal(response)
            cartPreferences.setPendingSync(false)
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun updateQuantityLocal(item: CartItem, quantity: Int) {
        val newItem = item.copy(quantity = quantity)
        addOrUpdateItemLocal(newItem)
        cartPreferences.setPendingSync(true)
    }

    private suspend fun ensureCartExistsOnServer() {
        if (appPreferences.isCartGenerated()) return
        remoteDataSource.createCart()
        appPreferences.setCartGenerated(true)
    }

    private suspend fun saveRemoteCartToLocal(response: CartDto) {
        val currentLocalItems = localDataSource.getCartWithItems()?.items ?: emptyList()

        localDataSource.clearCart()
        localDataSource.insertCart(response.toCartDbo())

        val itemsDto = response.cartSummary?.cartItems ?: emptyList()

        fetchAndCacheMissingProducts(itemsDto)

        val priceAlerts = response.priceChangedAlert?.cartItems?.associateBy { it.productVariantId }
        val stockAlerts = response.insufficientStockAlert?.cartItems?.associateBy { it.productVariantId }
        val availAlerts = response.variantAvailabilityAlert?.cartItems?.associateBy { it.productVariantId }

        val mergedDbos = itemsDto.map { dto ->
            val variantId = dto.productVariantId ?: DEFAULT_ID
            val preservedStock = currentLocalItems.find {
                it.cartItem.productVariantId == variantId
            }?.cartItem?.currentStock ?: dto.currentStock

            dto.toDbo().copy(
                currentStock = preservedStock,
                oldPriceAlert = priceAlerts?.get(variantId)?.oldPrice,
                stockAlertAvailable = stockAlerts?.get(variantId)?.availableStock,
                isAvailableAlert = availAlerts?.get(variantId)?.isVariantAvailable
            )
        }

        localDataSource.insertCartItems(mergedDbos)
        cartPreferences.setPendingSync(false)
    }

    private suspend fun addOrUpdateItemLocal(item: CartItem) {
        cacheItemDetailsLocally(item)

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

    private suspend fun cacheItemDetailsLocally(item: CartItem) {
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
                        errorHandler.handle(e)
                    }
                }
            }.awaitAll()
        }
    }

    private fun generateMockAlertResponse(cartWithItems: CartWithItemsDbo): CartDto {
        val currentItems = cartWithItems.items

        val itemsDto = currentItems.map { dbo ->
            CartItemDto(
                id = dbo.cartItem.id,
                productId = dbo.cartItem.productId,
                productVariantId = dbo.cartItem.productVariantId,
                currentStock = dbo.cartItem.currentStock,
                quantity = dbo.cartItem.quantity,
                unitPrice = dbo.cartItem.unitPrice,
                totalPrice = dbo.cartItem.totalPrice
            )
        }

        val cartSummary = CartSummaryDto(
            deviceId = "mock_device",
            createdAt = cartWithItems.cart.createdAt,
            updatedAt = cartWithItems.cart.updatedAt,
            subTotalPrice = cartWithItems.cart.subTotal,
            shippingCosts = cartWithItems.cart.shippingCost,
            totalPrice = cartWithItems.cart.total,
            cartItems = itemsDto
        )

        val priceAlerts = mutableListOf<PriceAlertItemDto>()
        val stockAlerts = mutableListOf<StockAlertItemDto>()
        val availAlerts = mutableListOf<VariantAvailabilityItemDto>()

        if (currentItems.isNotEmpty()) {
            val firstItem = currentItems[0].cartItem
            priceAlerts.add(
                PriceAlertItemDto(
                    productVariantId = firstItem.productVariantId,
                    oldPrice = firstItem.unitPrice.add(BigDecimal("20.00")),
                    newPrice = firstItem.unitPrice
                )
            )
        }

        if (currentItems.size > 1) {
            val secondItem = currentItems[1].cartItem
            stockAlerts.add(
                StockAlertItemDto(
                    productVariantId = secondItem.productVariantId,
                    requestedQuantity = secondItem.quantity,
                    availableStock = 0
                )
            )
        }

        if (currentItems.size > 2) {
            val thirdItem = currentItems[2].cartItem
            availAlerts.add(
                VariantAvailabilityItemDto(
                    cartItemId = thirdItem.id,
                    productVariantId = thirdItem.productVariantId,
                    isVariantAvailable = false
                )
            )
        }

        return CartDto(
            cartSummary = cartSummary,
            priceChangedAlert = if (priceAlerts.isNotEmpty()) PriceChangedAlertDto(priceAlerts) else null,
            insufficientStockAlert = if (stockAlerts.isNotEmpty()) InsufficientStockAlertDto(stockAlerts) else null,
            variantAvailabilityAlert = if (availAlerts.isNotEmpty()) VariantAvailabilityAlertDto(availAlerts) else null
        )
    }
}