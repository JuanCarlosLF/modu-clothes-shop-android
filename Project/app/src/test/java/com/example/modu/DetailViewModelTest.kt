package com.example.modu

import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.detail.ProductVariant
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.usecase.cart.CartUseCase
import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.presentation.detail.DetailViewModel
import com.example.modu.presentation.productDetail.ProductDetailUiEvent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val productUseCase = mockk<ProductUseCase>()
    private val cartUseCase = mockk<CartUseCase>()

    private lateinit var viewModel: DetailViewModel

    @Before
    fun setup() {
        viewModel = DetailViewModel(
            useCaseProduct = productUseCase,
            useCaseCart = cartUseCase
        )
    }

    @JvmField
    @Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load detail should load product`() = runTest {
        val detail = createDetail()

        coEvery {
            productUseCase.getDetailById(1)
        } returns detail
        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()
        viewModel.loadDetail(1)
        advanceUntilIdle()
        assertEquals(
            detail,
            viewModel.uiState.value.detail
        )
    }

    @Test
    fun `decrease quantity should no go below zero`() {

        viewModel.decreaseQuantity()

        TestCase.assertEquals(
            0,
            viewModel.uiState.value.quantity
        )
    }

    @Test
    fun `increase should not exceed stock`() = runTest {
        val detail = createDetail()

        coEvery {
            productUseCase.getDetailById(1)
        } returns detail
        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()
        viewModel.loadDetail(1)
        advanceUntilIdle()
        repeat(6) {
            viewModel.increaseQuantity()
        }
        assertEquals(
            viewModel.uiState.value.detail?.productVariantsList?.first()?.stock,
            viewModel.uiState.value.quantity
        )
    }

    @Test
    fun `when increase quantity exceeds stock should emit out of stock event`() = runTest {
        val detail = createDetail()

        coEvery {
            productUseCase.getDetailById(1)
        } returns detail
        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()
        viewModel.loadDetail(1)
        advanceUntilIdle()

        var receivedEvent: ProductDetailUiEvent? = null

        val job = launch {
            receivedEvent = viewModel.uiEvent.first()
        }

        runCurrent()

        repeat(6) {
            viewModel.increaseQuantity()
        }

        advanceUntilIdle()

        assertEquals(
            R.string.alert_out_of_stock_detail,
            receivedEvent?.message
        )

        job.cancel()
    }

    @Test
    fun `on size selected should reset quantity`() = runTest {
        val detail = createDetail()
        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()

        viewModel.loadDetail(1)
        advanceUntilIdle()
        viewModel.increaseQuantity()
        viewModel.onSizeSelected("M")
        assertEquals(
            0,
            viewModel.uiState.value.quantity
        )
    }

    @Test
    fun `on color selected should reset quantity`() = runTest {
        val detail = createDetail()
        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()

        viewModel.loadDetail(1)
        advanceUntilIdle()
        viewModel.increaseQuantity()
        viewModel.onColorSelected("Red")
        assertEquals(
            0,
            viewModel.uiState.value.quantity
        )
    }


    @Test
    fun `add item to cart should call cart useCase`() = runTest {

        val detail = createDetail()

        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()

        coEvery {
            cartUseCase.addItem(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } just Runs

        viewModel.loadDetail(1)
        advanceUntilIdle()
        viewModel.increaseQuantity()

        viewModel.addItemToCart()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            cartUseCase.addItem(
                productId = detail.id,
                productVariantId = 1,
                currentStock = 5,
                quantity = 1,
                unitPrice = detail.price,
                title = detail.name,
                imageUrl = detail.imageUrl,
                size = "M",
                color = "Red"
            )
        }
    }

    @Test
    fun `when product add to cart should emit event add to cart event`() = runTest {
        val detail = createDetail()
        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()

        coEvery {
            cartUseCase.addItem(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } just Runs

        viewModel.loadDetail(1)
        advanceUntilIdle()
        viewModel.increaseQuantity()

        var receiveEvent: ProductDetailUiEvent? = null
        val job = launch {
            receiveEvent = viewModel.uiEvent.first()
        }
        runCurrent()
        viewModel.addItemToCart()
        advanceUntilIdle()
        coVerify(exactly = 1) {
            cartUseCase.addItem(
                productId = detail.id,
                productVariantId = 1,
                currentStock = 5,
                quantity = 1,
                unitPrice = detail.price,
                title = detail.name,
                imageUrl = detail.imageUrl,
                size = "M",
                color = "Red"
            )
        }
        assertEquals(
            R.string.text_add_to_cart,
            receiveEvent?.message
        )
        job.cancel()
    }

    @Test
    fun `load detail should load related products`() = runTest {
        val detail = createDetail()
        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()
        viewModel.loadDetail(1)
        advanceUntilIdle()
        coVerify(exactly = 1) {
            productUseCase.getRelatedProducts(
                listOf("Shirt")
            )
        }
    }

    @Test
    fun `when use case throws exception, error state is emitted`() = runTest {
        coEvery { productUseCase.getDetailById(1) } throws RuntimeException("Internal Error")
        viewModel.loadDetail(1)
        advanceUntilIdle()
        assertEquals("Internal Error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `when add item to cart fails should emit error event and update error state`() = runTest {
        val detail = createDetail()

        coEvery {
            productUseCase.getDetailById(1)
        } returns detail

        coEvery {
            productUseCase.getRelatedProducts(any())
        } returns emptyList()

        coEvery {
            cartUseCase.addItem(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws RuntimeException("Ha habido un error, no se ha podido añadir al carrito")

        viewModel.loadDetail(1)
        advanceUntilIdle()
        viewModel.increaseQuantity()

        var receiveEvent: ProductDetailUiEvent? = null
        val job = launch {
            receiveEvent = viewModel.uiEvent.first()
        }
        runCurrent()
        viewModel.addItemToCart()
        advanceUntilIdle()

        assertEquals(
            "Ha habido un error, no se ha podido añadir al carrito",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(
            R.string.error_add_item_to_cart,
            receiveEvent?.message
        )

        job.cancel()
    }

    private fun createDetail() = Detail(
        id = 1,
        name = "Camiseta",
        description = "Descripción",
        imageUrl = "url",
        price = BigDecimal("10.00"),
        categoriesSet = listOf(
            Category("Shirt")
        ),
        productVariantsList = listOf(
            ProductVariant(
                id = 1,
                name = "Variante",
                size = "M",
                color = "Red",
                stock = 5,
                active = true,
                productId = 1
            )
        )
    )
}