package com.example.modu.domain.usecase.cart

import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.repository.cart.CartRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CartUseCaseImplTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var cartUseCase: CartUseCase

    @Before
    fun setUp() {
        cartRepository = mockk(relaxed = true)
        cartUseCase = CartUseCaseImpl(cartRepository)
    }

    @Test
    fun `getCartFlow when called then returns flow from repository`() = runTest {
        // SETUP
        val fakeCart = Cart(
            createdAt = "2023-01-01",
            updatedAt = "2023-01-02",
            subTotal = BigDecimal.ZERO,
            shippingCost = BigDecimal.ZERO,
            total = BigDecimal.ZERO,
            items = emptyList()
        )

        every { cartRepository.getCartFlow() } returns flowOf(fakeCart)

        // WHEN
        val result = cartUseCase.getCartFlow().first()

        // THEN
        assertEquals(fakeCart, result)
        verify(exactly = 1) { cartRepository.getCartFlow() }
    }

    @Test
    fun `addItem given valid quantity when called then calls repository with same quantity`() =
        runTest {
            // GIVEN
            val requestedQuantity = 2
            val stock = 5
            val unitPrice = BigDecimal("10.00")

            // WHEN
            cartUseCase.addItem(
                productId = 5, productVariantId = 10, currentStock = stock,
                quantity = requestedQuantity, unitPrice = unitPrice,
                title = "T", imageUrl = "U", size = "S", color = "C"
            )

            // THEN
            coVerify(exactly = 1) {
                cartRepository.addItem(
                    withArg { newItem ->
                        assertEquals(5, newItem.productId)
                        assertEquals(requestedQuantity, newItem.quantity)
                        assertEquals(BigDecimal("20.00"), newItem.totalPrice)
                    }
                )
            }
        }

    @Test
    fun `addItem given quantity over stock limits when called then caps quantity`() =
        runTest {
            // GIVEN
            val requestedQuantity = 50
            val stock = 5
            val unitPrice = BigDecimal("10.00")

            // WHEN
            cartUseCase.addItem(
                productId = 5, productVariantId = 10, currentStock = stock,
                quantity = requestedQuantity, unitPrice = unitPrice,
                title = "T", imageUrl = "U", size = "S", color = "C"
            )

            // THEN
            coVerify(exactly = 1) {
                cartRepository.addItem(
                    withArg { newItem ->
                        assertEquals(5, newItem.quantity)
                        assertEquals(BigDecimal("50.00"), newItem.totalPrice)
                    }
                )
            }
        }

    @Test
    fun `addItem given negative quantity when called then raises quantity to minimum`() = runTest {
        // GIVEN
        val requestedQuantity = -5
        val stock = 5
        val unitPrice = BigDecimal("10.00")
        val expectedMinimum = 1

        // WHEN
        cartUseCase.addItem(
            productId = 5, productVariantId = 10, currentStock = stock,
            quantity = requestedQuantity, unitPrice = unitPrice,
            title = "T", imageUrl = "U", size = "S", color = "C"
        )

        // THEN
        coVerify(exactly = 1) {
            cartRepository.addItem(
                withArg { newItem ->
                    assertEquals(expectedMinimum, newItem.quantity)
                    assertEquals(BigDecimal("10.00"), newItem.totalPrice)
                }
            )
        }
    }

    @Test
    fun `updateQuantity given invalid or unchanged quantities when called then returns early`() = runTest {
        // GIVEN
        val fakeCartItem = createFakeCartItem(quantity = 5, currentStock = 15)
        val invalidQuantities = listOf(5, 0, fakeCartItem.currentStock + 1)

        // WHEN
        invalidQuantities.forEach { quantity ->
            cartUseCase.updateQuantity(fakeCartItem, quantity)
        }

        // THEN
        coVerify(exactly = 0) { cartRepository.updateQuantityLocal(any()) }
    }

    @Test
    fun `updateQuantity given valid quantity when called then updates local repository with calculated price`() = runTest {
        // GIVEN
        val unitPrice = 10.toBigDecimal()
        val fakeCartItem = createFakeCartItem(
            quantity = 5,
            currentStock = 10,
            unitPrice = unitPrice
        )
        val newQuantity = 2

        // WHEN
        cartUseCase.updateQuantity(fakeCartItem, newQuantity)

        // THEN
        val expectedItem = fakeCartItem.copy(
            quantity = newQuantity,
            totalPrice = unitPrice.multiply(newQuantity.toBigDecimal())
        )

        coVerify(exactly = 1) { cartRepository.updateQuantityLocal(expectedItem) }
    }

    private fun createFakeCartItem(
        id: Int = 1,
        productId: Int = 5,
        productVariantId: Int = 10,
        currentStock: Int = 5,
        quantity: Int = 1,
        unitPrice: BigDecimal = BigDecimal("10.00"),
        totalPrice: BigDecimal = BigDecimal("10.00"),
        title: String = "Product Title",
        imageUrl: String = "url_placeholder",
        size: String = "M",
        color: String = "Negro",
        oldPriceAlert: BigDecimal? = null,
        stockAlertAvailable: Int? = null,
        isAvailableAlert: Boolean? = null
    ): CartItem {
        return CartItem(
            id = id,
            productId = productId,
            productVariantId = productVariantId,
            currentStock = currentStock,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            title = title,
            imageUrl = imageUrl,
            size = size,
            color = color,
            oldPriceAlert = oldPriceAlert,
            stockAlertAvailable = stockAlertAvailable,
            isAvailableAlert = isAvailableAlert
        )
    }
}