package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.CartSummaryDto
import com.example.modu.data.dataSource.remote.cart.dto.InsufficientStockAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceChangedAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.StockAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class CartDtoMergeTest {

    @Test
    fun `mergeWithPrevious given previous null when called then returns this`() {
        // SETUP
        val cartDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(createCartItemDto(1)))
        )

        // WHEN
        val result = cartDto.mergeWithPrevious(null)

        // THEN
        assertEquals(cartDto, result)
    }

    @Test
    fun `mergeWithPrevious given only this has cartItems when called then keeps this items`() {
        // SETUP
        val thisDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(createCartItemDto(1), createCartItemDto(2)))
        )
        val previousDto = CartDto(cartSummary = null)

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(2, result.cartSummary?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given only previous has cartItems when called then keeps previous items`() {
        // SETUP
        val thisDto = CartDto(cartSummary = null)
        val previousDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(createCartItemDto(1), createCartItemDto(2)))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(2, result.cartSummary?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given both have distinct cartItems when called then concatenates all`() {
        // SETUP
        val thisDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(createCartItemDto(1), createCartItemDto(2)))
        )
        val previousDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(createCartItemDto(3), createCartItemDto(4)))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(4, result.cartSummary?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given duplicate productVariantId when called then keeps this item`() {
        // SETUP
        val thisItem = createCartItemDto(productVariantId = 1, quantity = 5)
        val previousItem = createCartItemDto(productVariantId = 1, quantity = 10)

        val thisDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(thisItem))
        )
        val previousDto = CartDto(
            cartSummary = CartSummaryDto(cartItems = listOf(previousItem))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(1, result.cartSummary?.cartItems?.size)
        assertEquals(5, result.cartSummary?.cartItems?.first()?.quantity)
    }

    @Test
    fun `mergeWithPrevious given price alerts in both when called then merges and deduplicates`() {
        // SETUP
        val thisAlert = PriceAlertItemDto(productVariantId = 1, oldPrice = BigDecimal("10.00"))
        val previousAlert = PriceAlertItemDto(productVariantId = 2, oldPrice = BigDecimal("20.00"))

        val thisDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(thisAlert))
        )
        val previousDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(2, result.priceChangedAlert?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given price alert duplicate variantId when called then keeps this alert`() {
        // SETUP
        val thisAlert = PriceAlertItemDto(productVariantId = 1, oldPrice = BigDecimal("10.00"))
        val previousAlert = PriceAlertItemDto(productVariantId = 1, oldPrice = BigDecimal("99.00"))

        val thisDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(thisAlert))
        )
        val previousDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(1, result.priceChangedAlert?.cartItems?.size)
        assertEquals(BigDecimal("10.00"), result.priceChangedAlert?.cartItems?.first()?.oldPrice)
    }

    @Test
    fun `mergeWithPrevious given this has null price alert when called then keeps previous alerts`() {
        // SETUP
        val previousAlert = PriceAlertItemDto(productVariantId = 1, oldPrice = BigDecimal("20.00"))

        val thisDto = CartDto(priceChangedAlert = null)
        val previousDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(1, result.priceChangedAlert?.cartItems?.size)
        assertEquals(BigDecimal("20.00"), result.priceChangedAlert?.cartItems?.first()?.oldPrice)
    }

    @Test
    fun `mergeWithPrevious given empty list price alert when called then keeps previous alerts`() {
        // SETUP
        val previousAlert = PriceAlertItemDto(productVariantId = 1, oldPrice = BigDecimal("20.00"))

        val thisDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = emptyList())
        )
        val previousDto = CartDto(
            priceChangedAlert = PriceChangedAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(1, result.priceChangedAlert?.cartItems?.size)
        assertEquals(BigDecimal("20.00"), result.priceChangedAlert?.cartItems?.first()?.oldPrice)
    }

    @Test
    fun `mergeWithPrevious given stock alerts when called then merges correctly`() {
        // SETUP
        val thisAlert = StockAlertItemDto(productVariantId = 1, availableStock = 5)
        val previousAlert = StockAlertItemDto(productVariantId = 2, availableStock = 10)

        val thisDto = CartDto(
            insufficientStockAlert = InsufficientStockAlertDto(cartItems = listOf(thisAlert))
        )
        val previousDto = CartDto(
            insufficientStockAlert = InsufficientStockAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(2, result.insufficientStockAlert?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given availability alerts when called then merges correctly`() {
        // SETUP
        val thisAlert = VariantAvailabilityItemDto(productVariantId = 1, isVariantAvailable = true)
        val previousAlert = VariantAvailabilityItemDto(productVariantId = 2, isVariantAvailable = false)

        val thisDto = CartDto(
            variantAvailabilityAlert = VariantAvailabilityAlertDto(cartItems = listOf(thisAlert))
        )
        val previousDto = CartDto(
            variantAvailabilityAlert = VariantAvailabilityAlertDto(cartItems = listOf(previousAlert))
        )

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(2, result.variantAvailabilityAlert?.cartItems?.size)
    }

    @Test
    fun `mergeWithPrevious given no alerts in either when called then returns null alerts`() {
        // SETUP
        val thisDto = CartDto()
        val previousDto = CartDto()

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertNull(result.priceChangedAlert)
        assertNull(result.insufficientStockAlert)
        assertNull(result.variantAvailabilityAlert)
    }

    @Test
    fun `mergeWithPrevious given cartSummary this null when called then uses previous cartSummary`() {
        // SETUP
        val previousSummary = CartSummaryDto(
            cartItems = listOf(createCartItemDto(1)),
            subTotalPrice = BigDecimal("50.00")
        )

        val thisDto = CartDto(cartSummary = null)
        val previousDto = CartDto(cartSummary = previousSummary)

        // WHEN
        val result = thisDto.mergeWithPrevious(previousDto)

        // THEN
        assertEquals(BigDecimal("50.00"), result.cartSummary?.subTotalPrice)
        assertEquals(1, result.cartSummary?.cartItems?.size)
    }

    private fun createCartItemDto(
        productVariantId: Int = 1,
        quantity: Int = 1
    ): CartItemDto {
        return CartItemDto(
            id = productVariantId,
            productId = 1,
            productVariantId = productVariantId,
            quantity = quantity,
            currentStock = 5,
            unitPrice = BigDecimal("10.00"),
            totalPrice = BigDecimal("10.00")
        )
    }
}
