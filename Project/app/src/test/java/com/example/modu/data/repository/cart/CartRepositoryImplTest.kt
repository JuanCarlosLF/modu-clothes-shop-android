package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemWithDetailsDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.CartCheckoutResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.CartSummaryDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceChangedAlertDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.repository.cart.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CartRepositoryImplTest {

    private lateinit var localDataSource: CartLocalDataSource
    private lateinit var remoteDataSource: CartRemoteDataSource
    private lateinit var cartPreferences: CartPreferences
    private lateinit var appPreferences: AppPreferences
    private lateinit var productRemoteDataSource: ProductDataSource
    private lateinit var errorHandler: ErrorHandler
    private lateinit var repository: CartRepository

    @Before
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)
        cartPreferences = mockk(relaxed = true)
        appPreferences = mockk(relaxed = true)
        productRemoteDataSource = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)

        repository = CartRepositoryImpl(
            localDataSource,
            remoteDataSource,
            cartPreferences,
            appPreferences,
            productRemoteDataSource,
            errorHandler
        )
    }

    @Test
    fun `addItem given no pending sync when called then calls remote and saves response local`() =
        runTest {
            // SETUP
            val fakeItem = createFakeCartItem()
            val fakeResponse = CartResponseDto(
                deviceId = "123456789123456",
                createdAt = "2023-01-01",
                updatedAt = "2023-01-02",
                subTotalPrice = BigDecimal("5.00"),
                shippingCosts = BigDecimal("5.00"),
                totalPrice = BigDecimal("15.00"),
                cartItems = emptyList()
            )

            // GIVEN
            coEvery { cartPreferences.hasPendingSync() } returns false
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { remoteDataSource.addItem(any()) } returns fakeResponse

            // WHEN
            repository.addItem(fakeItem)

            // THEN
            coVerify(exactly = 2) { localDataSource.insertProductDetails(any()) }
            coVerify(exactly = 2) { localDataSource.insertProductVariants(any()) }
            coVerify(exactly = 2) { localDataSource.insertCartItems(any()) }

            coVerify(exactly = 1) {
                remoteDataSource.addItem(
                    withArg { request ->
                        assertEquals(5, request.productVariantId)
                        assertEquals(1, request.quantity)
                    }
                )
            }

            coVerify(exactly = 1) { localDataSource.clearCart() }
            coVerify(exactly = 1) { localDataSource.insertCart(any()) }
            coVerify(exactly = 1) { cartPreferences.setPendingSync(true) }
            coVerify(exactly = 1) { cartPreferences.setPendingSync(false) }
        }

    @Test
    fun `addItem given pending sync when called then saves local and updates cart`() = runTest {
        // SETUP
        val fakeItem = createFakeCartItem()

        // GIVEN
        coEvery { cartPreferences.hasPendingSync() } returns true
        coEvery { appPreferences.isCartGenerated() } returns true
        coEvery { localDataSource.getCartWithItems() } returns mockk(relaxed = true)
        coEvery { remoteDataSource.updateCart(any()) } returns mockk(relaxed = true)

        // WHEN
        repository.addItem(fakeItem)

        // THEN
        coVerify(exactly = 2) { localDataSource.insertCartItems(any()) }
        coVerify(exactly = 1) { cartPreferences.setPendingSync(true) }
        coVerify(exactly = 1) { remoteDataSource.updateCart(any()) }
        coVerify(exactly = 0) { remoteDataSource.addItem(any()) }
    }

    @Test
    fun `addItem given network error when called then handles NO_INTERNET silently and keeps pending sync`() =
        runTest {
            // SETUP
            val fakeItem = createFakeCartItem()
            val networkException = Exception("Retrofit Timeout")
            val appError = AppError(
                ErrorType.NO_INTERNET,
                title = "Sin conexión a internet",
                message = "Por favor, comprueba tu conexión a la red y vuelve a intentarlo"
            )

            // GIVEN
            coEvery { cartPreferences.hasPendingSync() } returns false
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { remoteDataSource.addItem(any()) } throws networkException
            coEvery { errorHandler.handle(any()) } returns appError

            // WHEN
            repository.addItem(fakeItem)

            // THEN
            coVerify(exactly = 1) { remoteDataSource.addItem(any()) }
            coVerify(exactly = 1) { errorHandler.handle(networkException) }
            coVerify(exactly = 1) { cartPreferences.setPendingSync(true) }
            coVerify(exactly = 0) { cartPreferences.setPendingSync(false) }
            coVerify(exactly = 0) { localDataSource.clearCart() }
        }

    @Test
    fun `addItem given NOT_FOUND error when called then resets cart and throws exception`() =
        runTest {
            // SETUP
            val fakeItem = createFakeCartItem()
            val notFoundException = Exception("404 Not Found")
            val appError = AppError(ErrorType.NOT_FOUND, "", "")

            // GIVEN
            coEvery { cartPreferences.hasPendingSync() } returns false
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { remoteDataSource.addItem(any()) } throws notFoundException
            coEvery { errorHandler.handle(notFoundException) } returns appError

            // WHEN & THEN
            assertThrows(AppError::class.java) {
                runBlocking {
                    repository.addItem(fakeItem)
                }
            }

            coVerify(exactly = 1) { appPreferences.setCartGenerated(false) }
            coVerify(exactly = 1) { errorHandler.handle(notFoundException) }
        }

    @Test
    fun `checkout given success response when called then clears cart and returns success`() =
        runTest {
            // SETUP
            val fakeResponse = CartCheckoutResponseDto(
                orderPlaced = true,
                cartResponse = null
            )

            val fakeCartDbo = CartWithItemsDbo(
                cart = CartDbo(
                    1,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "",
                    ""
                ),
                items = emptyList()
            )

            // GIVEN
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { localDataSource.getCartWithItems() } returns fakeCartDbo
            coEvery { remoteDataSource.checkout(any()) } returns fakeResponse

            // WHEN
            val result =
                repository.checkout(isPaid = true, specialInstructions = "Test instructions")

            // THEN
            assertEquals(CheckoutResult.SUCCESS, result)

            coVerify(exactly = 1) {
                remoteDataSource.checkout(
                    withArg { request ->
                        assertEquals(true, request.isPaid)
                        assertEquals("Test instructions", request.specialInstructions)
                    }
                )
            }

            coVerify(exactly = 1) { localDataSource.clearCart() }
            coVerify(exactly = 1) { appPreferences.setCartGenerated(false) }
        }

    @Test
    fun `checkout given cart alerts when called then saves updated cart and returns alerts triggered`() =
        runTest {
            // SETUP
            val fakeChangedPriceItems = PriceAlertItemDto(
                productVariantId = 5,
                oldPrice = BigDecimal.ZERO,
                newPrice = BigDecimal.ONE
            )

            val fakePriceChangedAlertDto = PriceChangedAlertDto(listOf(fakeChangedPriceItems))

            val updatedCartDto = CartDto(
                cartSummary = null,
                priceChangedAlert = fakePriceChangedAlertDto
            )

            val fakeResponse = CartCheckoutResponseDto(
                orderPlaced = false,
                cartResponse = updatedCartDto,
            )

            val fakeCartDbo = CartWithItemsDbo(
                cart = CartDbo(1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", ""),
                items = emptyList()
            )

            // GIVEN
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { localDataSource.getCartWithItems() } returns fakeCartDbo
            coEvery { remoteDataSource.checkout(any()) } returns fakeResponse

            // WHEN
            val result = repository.checkout(isPaid = true, specialInstructions = "")

            // THEN
            assertEquals(CheckoutResult.ALERTS_TRIGGERED, result)

            coVerify(exactly = 1) { remoteDataSource.checkout(any()) }
            coVerify(exactly = 1) { localDataSource.clearCart() }
            coVerify(exactly = 1) { localDataSource.insertCart(any()) }
            coVerify(exactly = 0) { appPreferences.setCartGenerated(false) }
        }

    @Test
    fun `syncCart given pending sync when called then updates cart and returns`() = runTest {
        // GIVEN
        coEvery { cartPreferences.hasPendingSync() } returns true
        coEvery { appPreferences.isCartGenerated() } returns true
        coEvery { localDataSource.getCartWithItems() } returns mockk(relaxed = true)
        coEvery { remoteDataSource.updateCart(any()) } returns mockk(relaxed = true)

        // WHEN
        repository.syncCart()

        // THEN
        coVerify(exactly = 1) { remoteDataSource.updateCart(any()) }
        coVerify(exactly = 0) { remoteDataSource.getCart() }
    }

    @Test
    fun `syncCart given no pending sync when called then saves remote cart to local`() {
        runTest {
            // GIVEN
            coEvery { cartPreferences.hasPendingSync() } returns false
            coEvery { remoteDataSource.getCart() } returns mockk(relaxed = true)

            // WHEN
            repository.syncCart()

            // THEN
            coVerify(exactly = 2) { cartPreferences.setPendingSync(false) }
        }
    }

    @Test
    fun `syncCart given NotFound error when called then clears cart and set cart generated to false`() =
        runTest {
            // GIVEN
            val notFoundException = Exception("Error 404 del servidor")
            val appError = AppError(ErrorType.NOT_FOUND, "Not Found", "El carrito no existe")

            coEvery { cartPreferences.hasPendingSync() } returns false
            coEvery { appPreferences.isCartGenerated() } returns true
            coEvery { remoteDataSource.getCart() } throws notFoundException
            coEvery { errorHandler.handle(notFoundException) } returns appError

            // WHEN AND THEN
            assertThrows(AppError::class.java) {
                runBlocking {
                    repository.syncCart()
                }
            }
            coVerify(exactly = 1) { errorHandler.handle(notFoundException) }
            coVerify(exactly = 1) { appPreferences.setCartGenerated(false) }
            coVerify(exactly = 1) { localDataSource.clearCart() }
        }

    @Test
    fun `deleteItem given pending sync when called then removes local item and updates cart`() {
        runTest {
            // GIVEN
            val fakeItemDto = createFakeCartItem().toDto()
            coEvery { cartPreferences.hasPendingSync() } returns true


            // WHEN
            repository.deleteItem(fakeItemDto.id!!)

            // THEN
            coVerify(exactly = 0) { remoteDataSource.deleteItemById(any()) }
        }
    }

    @Test
    fun `deleteItem given no pending sync when called then calls remote and updates local`() {
        runTest {
            // GIVEN
            val fakeItemDto = createFakeCartItem().toDto()
            coEvery { cartPreferences.hasPendingSync() } returns false

            // WHEN 
            repository.deleteItem(fakeItemDto.id!!)

            // THEN
            coVerify(exactly = 0) { localDataSource.deleteCartItem(any()) }
            coVerify(exactly = 0) { cartPreferences.setPendingSync(true) }
            coVerify(exactly = 1) { localDataSource.getCartWithItems() }
        }
    }

    @Test
    fun `updateCart given no pending sync when called then returns`() {
        runTest {
            // GIVEN
            coEvery { cartPreferences.hasPendingSync() } returns false

            // WHEN
            repository.updateCart()

            // THEN
            coVerify(exactly = 0) { appPreferences.isCartGenerated() }
        }
    }

    @Test
    fun `updateCart given only existing items when called then updates local`() = runTest {
        // SETUP
        val fakeCartItem = createFakeCartItem().copy(productId = 5, productVariantId = 5, id = 1)
        val fakeCart = CartDbo(
            subTotal = BigDecimal.ZERO, shippingCost = BigDecimal.ZERO, total = BigDecimal.ZERO, createdAt = "", updatedAt = ""
        )
        val fakeCartItems = CartItemWithDetailsDbo(
            cartItem = fakeCartItem.toDbo(),
            productDetail = listOf(CartItemDetailDbo(id = 5, name = "example", imageUrl = "https://example.url")),
            productVariant = listOf(ProductVariantDbo(id = 5, productId = 1, size = "L", color = "Red"))
        )

        val fakeCartWithItems = CartWithItemsDbo(cart = fakeCart, items = listOf(fakeCartItems))
        val fakeResponse = CartDto()

        // GIVEN
        coEvery { cartPreferences.hasPendingSync() } returns true
        coEvery { appPreferences.isCartGenerated() } returns true
        coEvery { localDataSource.getCartWithItems() } returns fakeCartWithItems
        coEvery { remoteDataSource.updateCart(any()) } returns fakeResponse

        // WHEN
        repository.updateCart()

        // THEN
        coVerify(exactly = 1) {
            remoteDataSource.updateCart(
                withArg { request ->
                    assertEquals(fakeCartItem.productVariantId, request.cartItems?.first()?.productVariantId)
                    assertEquals(fakeCartItem.quantity, request.cartItems?.first()?.quantity)
                }
            )
        }
        coVerify(exactly = 0) { remoteDataSource.addItem(any()) }
        coVerify(exactly = 1) { localDataSource.clearCart() }
    }

    @Test
    fun `updateCart given mixed items when called then calls update and adds new offline items`() = runTest {
        // SETUP
        val existingItem = createFakeCartItem().copy(productId = 1, productVariantId = 2, id = 5, quantity = 1)
        val existingCartItemDbo = CartItemWithDetailsDbo(
            cartItem = existingItem.toDbo(),
            productDetail = listOf(CartItemDetailDbo(id = 1, name = "Pantalón", imageUrl = "url")),
            productVariant = listOf(ProductVariantDbo(id = 2, productId = 1, size = "M", color = "Negro"))
        )

        val offlineItem = createFakeCartItem().copy(productId = 3, productVariantId = 8, id = -1, quantity = 2)
        val offlineCartItemDbo = CartItemWithDetailsDbo(
            cartItem = offlineItem.toDbo(),
            productDetail = listOf(CartItemDetailDbo(id = 3, name = "Camiseta", imageUrl = "url")),
            productVariant = listOf(ProductVariantDbo(id = 8, productId = 3, size = "L", color = "Blanco"))
        )

        val fakeCart = CartDbo(
            subTotal = BigDecimal.ZERO, shippingCost = BigDecimal.ZERO, total = BigDecimal.ZERO, createdAt = "", updatedAt = ""
        )

        val fakeCartWithItems = CartWithItemsDbo(
            cart = fakeCart,
            items = listOf(existingCartItemDbo, offlineCartItemDbo)
        )

        val fakeSummary = CartSummaryDto(
            cartItems = listOf(
                CartItemDto(
                    id = 5,
                    productId = 1,
                    productVariantId = 2,
                    quantity = 1,
                    currentStock = 5,
                    unitPrice = BigDecimal.ZERO,
                    totalPrice = BigDecimal.ZERO
                )
            )
        )

        val fakeUpdateResponse = CartDto(
            cartSummary = fakeSummary,
            priceChangedAlert = PriceChangedAlertDto(
                cartItems = listOf(PriceAlertItemDto(productVariantId = 2, oldPrice = BigDecimal("10.00")))
            )
        )

        val fakeAddResponse = CartResponseDto(
            deviceId = "123", createdAt = "", updatedAt = "", subTotalPrice = BigDecimal.ZERO,
            shippingCosts = BigDecimal.ZERO, totalPrice = BigDecimal.ZERO,
            cartItems = listOf(
                CartItemDto(
                    id = 5,
                    productId = 1,
                    productVariantId = 2,
                    quantity = 1,
                    currentStock = 5,
                    unitPrice = BigDecimal.ZERO,
                    totalPrice = BigDecimal.ZERO
                )
            )
        )

        // GIVEN
        coEvery { cartPreferences.hasPendingSync() } returns true
        coEvery { appPreferences.isCartGenerated() } returns true
        coEvery { localDataSource.getCartWithItems() } returns fakeCartWithItems
        coEvery { remoteDataSource.updateCart(any()) } returns fakeUpdateResponse
        coEvery { remoteDataSource.addItem(any()) } returns fakeAddResponse

        // WHEN
        repository.updateCart()

        // THEN
        coVerify(exactly = 1) {
            remoteDataSource.updateCart(
                withArg { request ->
                    assertEquals(1, request.cartItems?.size)
                    assertEquals(existingItem.productVariantId, request.cartItems?.first()?.productVariantId)
                }
            )
        }
        coVerify(exactly = 1) {
            remoteDataSource.addItem(
                withArg { request ->
                    assertEquals(offlineItem.productVariantId, request.productVariantId)
                    assertEquals(offlineItem.quantity, request.quantity)
                }
            )
        }
        coVerify(exactly = 1) { localDataSource.clearCart() }
        coVerify(exactly = 1) {
            localDataSource.insertCartItems(
                withArg { allItems ->
                    val itemWithAlert = allItems.find { it.productVariantId == 2 }
                    assertEquals(BigDecimal("10.00"), itemWithAlert?.oldPriceAlert)
                }
            )
        }
    }

    private fun createFakeCartItem(
        id: Int = 1,
        productVariantId: Int = 5,
        quantity: Int = 1
    ): CartItem {
        return CartItem(
            id = id,
            productId = 1,
            productVariantId = productVariantId,
            currentStock = 2,
            quantity = quantity,
            unitPrice = BigDecimal("5.00"),
            totalPrice = BigDecimal("5.00"),
            title = "Example title",
            imageUrl = "example_image",
            size = "XL",
            color = "Blue"
        )
    }
}