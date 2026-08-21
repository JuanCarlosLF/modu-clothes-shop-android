package com.example.modu.data.repository.demo.cart

import com.example.modu.data.dataSource.local.demo.database.cart.DemoCartDataSource
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.CheckoutResult
import com.example.modu.domain.repository.cart.CartRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

private const val CART_ID = 1

class DemoCartRepositoryImplTest {

    private lateinit var repository: CartRepository
    private lateinit var dataSource: DemoCartDataSource

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = DemoCartRepositoryImpl(dataSource)
    }

    @Test
    fun `getCartFlow() given not existing cart when called emits created cart with items`() =
        runTest {
            // GIVEN
            val fakeCart: CartDbo = CartDbo(1, "", "")
            val fakeCartItemFlow: Flow<List<CartItemDetailDbo>> =
                flowOf(listOf(CartItemDetailDbo(3, 4, 5, 10, 7, 1400, "", "", "", "")))
            coEvery { dataSource.getCart(CART_ID) } returnsMany (listOf(
                null, fakeCart
            ))
            coEvery { dataSource.createCart(any()) } just Runs
            coEvery { dataSource.observeCartItems(fakeCart.id) } returns fakeCartItemFlow

            // WHEN
            val expectedCartItem =
                CartItem(
                    3,
                    4,
                    5,
                    10,
                    7,
                    BigDecimal("14.00"),
                    BigDecimal.valueOf(1400 * 7, 2),
                    "",
                    "file:///android_asset/",
                    "",
                    ""
                )
            val cartExpected =
                Cart(
                    "",
                    "",
                    subTotal = BigDecimal.valueOf(1400 * 7, 2),
                    shippingCost = BigDecimal("0.00"),
                    total = BigDecimal.valueOf(1400 * 7, 2),
                    listOf(expectedCartItem)
                )
            val cartEmited = repository.getCartFlow().first()

            // THEN
            coVerify(exactly = 2) { dataSource.getCart(CART_ID) }
            coVerify(exactly = 1) { dataSource.createCart(any()) }
            coVerify(exactly = 1) {
                dataSource.observeCartItems(1)
            }
            assertEquals(cartExpected, cartEmited)
            coVerifyOrder {
                dataSource.getCart(CART_ID)
                dataSource.createCart(any())
                dataSource.getCart(CART_ID)
            }
        }

    @Test
    fun `addItem() given new variant when called then inserts item with correct quantity and cartId`() =
        runTest {
            // GIVEN
            val newItem = createFakeCartItem()
            coEvery { dataSource.createCart(any()) } just Runs
            coEvery { dataSource.getCartItem(newItem.productVariantId, CART_ID) } returns null
            coEvery { dataSource.addItem(any()) } just Runs

            // WHEN
            repository.addItem(newItem)

            // THEN
            coVerify(exactly = 0) { dataSource.updateItemQuantity(any(), any()) }
            coVerify(exactly = 1) {
                dataSource.addItem(withArg {
                    assertEquals(CART_ID, it.cartId)
                    assertEquals(newItem.productVariantId, it.productVariantId)
                    assertEquals(newItem.quantity, it.quantity)
                })
            }
        }

    @Test
    fun `addItem() given existing variant and total quantity below stock when called then updates total quantity`() =
        runTest {
            // GIVEN
            val existingItem = CartItemDbo(2, CART_ID, 3, 5)
            val itemToUpdate = createFakeCartItem(currentStock = 20)
            coEvery { dataSource.createCart(any()) } just Runs
            coEvery {
                dataSource.getCartItem(
                    itemToUpdate.productVariantId, CART_ID
                )
            } returns existingItem

            val expectedQuantity = existingItem.quantity + itemToUpdate.quantity
            coEvery { dataSource.updateItemQuantity(existingItem.id, expectedQuantity) } just Runs

            // WHEN
            repository.addItem(itemToUpdate)

            // THEN
            coVerify(exactly = 0) { dataSource.addItem(any()) }
            coVerify(exactly = 1) {
                dataSource.updateItemQuantity(
                    existingItem.id, expectedQuantity
                )
            }
        }

    @Test
    fun `addItem() given existing variant and total quantity above stock when called then caps to max quantity`() =
        runTest {
            // GIVEN
            val itemToUpdate = createFakeCartItem(id = 2, quantity = 10, currentStock = 14)
            val existingItem = CartItemDbo(5, CART_ID, 3, 5)

            coEvery { dataSource.createCart(any()) } just Runs
            coEvery {
                dataSource.getCartItem(
                    itemToUpdate.productVariantId, CART_ID
                )
            } returns existingItem

            val expectedQuantity = itemToUpdate.currentStock
            coEvery { dataSource.updateItemQuantity(existingItem.id, expectedQuantity) } just Runs

            // WHEN
            repository.addItem(itemToUpdate)

            // THEN
            coVerify(exactly = 0) { dataSource.addItem(any()) }
            coVerify(exactly = 1) {
                dataSource.updateItemQuantity(
                    existingItem.id, expectedQuantity
                )
            }
        }

    @Test
    fun `checkout() when called clears cart and returns success`() = runTest {
        // GIVEN
        coEvery { dataSource.clearCart(CART_ID) } just Runs

        // WHEN
        val checkoutResult = repository.checkout(true, "")

        // THEN
        coVerify(exactly = 1) { dataSource.clearCart(CART_ID) }
        assertEquals(CheckoutResult.SUCCESS, checkoutResult)
    }

    private fun createFakeCartItem(
        id: Int = 5,
        productId: Int = 1,
        productVariantId: Int = 3,
        currentStock: Int = 5,
        quantity: Int = 10,
        unitPrice: BigDecimal = BigDecimal.TEN,
        totalPrice: BigDecimal = BigDecimal("100.00"),
        title: String = "New variant",
        imageUrl: String = "",
        size: String = "",
        color: String = ""
    ) = CartItem(
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
        color = color
    )
}
