package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.preference.AppPreferences
import com.example.modu.data.dataSource.local.preference.cart.CartPreferences
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.domain.entity.cart.CartItem
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
    fun `addItem given NOT_FOUND error when called then resets cart and throws exception`() = runTest {
        // SETUP
        val fakeItem = createFakeCartItem()
        val notFoundException = Exception("404 Not Found")
        val appError = AppError(ErrorType.NOT_FOUND,"","")

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