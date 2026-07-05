package com.example.modu

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.usecase.cart.CartUseCase
import com.example.modu.presentation.cart.CartUiEvent
import com.example.modu.presentation.cart.CartViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    private val cartUseCase = mockk<CartUseCase>()
    private lateinit var viewModel: CartViewModel

    @Before
    fun setup() {

        coEvery {
            cartUseCase.verifyPendingChanges()
        } just Runs

        every {
            cartUseCase.getCartFlow()
        } returns flowOf(
            Cart(
                items = emptyList(),
                shippingCost = BigDecimal.ZERO,
                createdAt = "08/06/2026",
                updatedAt = "08/06/2026",
                subTotal = BigDecimal.ZERO,
                total = BigDecimal.ZERO
            )
        )

        viewModel = CartViewModel(cartUseCase = cartUseCase)
    }

    @JvmField
    @Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `update quantity should call use case`() = runTest {
        val item = createCartItem()

        coEvery {
            cartUseCase.updateQuantity(item, 3)
        } just Runs

        viewModel.updateQuantity(item, 3)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            cartUseCase.updateQuantity(item, 3)
        }
    }

    @Test
    fun `update quantity should update error state when use case fails`() = runTest {
        val item = createCartItem()
        val error = AppError(
            type = ErrorType.UNKNOWN,
            message = "Error updating quantity",
            title = "Error"
        )
        coEvery {
            cartUseCase.updateQuantity(item, 3)
        } throws error

        viewModel.updateQuantity(item, 3)
        advanceUntilIdle()

        assertEquals(
            error,
            viewModel.uiState.value.error
        )
    }

    @Test
    fun `dismiss error should clear error state`() = runTest {
        val item = createCartItem()
        val error = AppError(
            type = ErrorType.UNKNOWN,
            message = "Error updating quantity",
            title = "Error"
        )

        coEvery {
            cartUseCase.updateQuantity(item, 3)
        } throws error

        viewModel.updateQuantity(item, 3)
        advanceUntilIdle()

        viewModel.dismissError()

        assertEquals(
            null,
            viewModel.uiState.value.error
        )
    }

    @Test
    fun `confirm clear cart should call use case`() = runTest {
        coEvery {
            cartUseCase.clearCart()
        } just Runs

        viewModel.onConfirmClearCart()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            cartUseCase.clearCart()
        }
    }

    @Test
    fun `confirm clear cart should update error state when use case fails`() = runTest {
        val error = AppError(
            type = ErrorType.UNKNOWN,
            message = "Error clearing cart",
            title = "Error"
        )

        coEvery {
            cartUseCase.clearCart()
        } throws error

        viewModel.onConfirmClearCart()
        advanceUntilIdle()

        assertEquals(
            error,
            viewModel.uiState.value.error
        )
    }

    @Test
    fun `clear cart clicked emit dialog event`() = runTest {
        var event: CartUiEvent? = null

        val job = launch {
            event = viewModel.uiEvent.first()
        }

        runCurrent()
        viewModel.onClearCartClicked()
        advanceUntilIdle()

        assertEquals(
            CartUiEvent.ShowClearCartDialog,
            event
        )
        job.cancel()
    }

    @Test
    fun `checkout should emit success event`() = runTest {

        coEvery {
            cartUseCase.checkout("")
        } returns CheckoutResult.SUCCESS

        var receiveEvent: CartUiEvent? = null
        val job = launch {
            receiveEvent = viewModel.uiEvent.first()
        }
        runCurrent()
        viewModel.checkout("")
        advanceUntilIdle()

        assertEquals(
            CartUiEvent.CheckoutSuccess,
            receiveEvent
        )

        coVerify(exactly = 1) {
            cartUseCase.checkout("")
        }

        job.cancel()
    }

    @Test
    fun `checkout should emit offline event when no internet`() = runTest {

        val error = AppError(
            type = ErrorType.NO_INTERNET,
            message = "No internet",
            title = "Error"
        )

        coEvery {
            cartUseCase.checkout("")
        } throws error

        var receiveEvent: CartUiEvent? = null
        val job = launch {
            receiveEvent = viewModel.uiEvent.first()
        }
        runCurrent()

        viewModel.checkout("")
        advanceUntilIdle()

        assertEquals(
            CartUiEvent.OfflineSyncFailed,
            receiveEvent
        )

        job.cancel()
    }

    private fun createCartItem() = CartItem(
        id = 1,
        productId = 100,
        productVariantId = 10,
        currentStock = 5,
        quantity = 2,
        unitPrice = BigDecimal("10.00"),
        totalPrice = BigDecimal("20.00"),
        title = "Camiseta",
        imageUrl = "https://image.test/camiseta.jpg",
        size = "M",
        color = "Red"
    )
}