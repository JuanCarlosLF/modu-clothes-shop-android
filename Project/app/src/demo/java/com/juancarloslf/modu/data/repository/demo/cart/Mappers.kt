package com.juancarloslf.modu.data.repository.demo.cart

import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import com.juancarloslf.modu.domain.entity.cart.Cart
import com.juancarloslf.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

private const val ANDROID_ASSET_URI_PREFIX = "file:///android_asset/"
private const val PRICE_SCALE = 2

private const val DEFAULT_SHIPPING_COST = 0L

internal fun CartItemDetailDbo.toDomain(): CartItem {
    val unitPriceDecimal = BigDecimal.valueOf(unitPrice, PRICE_SCALE)
    return CartItem(
        id = id,
        productId = productId,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPriceDecimal,
        totalPrice = unitPriceDecimal * quantity.toBigDecimal(),
        title = title,
        imageUrl = imageUrl.toAssetUri(),
        size = size,
        color = color,
    )
}

internal fun CartDbo.toDomain(items: List<CartItem>): Cart {
    val subTotal = items.sumOf { it.totalPrice }
    val shippingCost = BigDecimal.valueOf(DEFAULT_SHIPPING_COST, PRICE_SCALE)

    return Cart(
        createdAt = createdAt,
        updatedAt = updatedAt,
        subTotal = subTotal,
        shippingCost = shippingCost,
        total = subTotal + shippingCost,
        items = items
    )
}

internal fun CartItem.toDbo(cartId: Int, quantity: Int): CartItemDbo =
    CartItemDbo(
        cartId = cartId,
        productVariantId = productVariantId,
        quantity = quantity
    )

private fun String.toAssetUri(): String = ANDROID_ASSET_URI_PREFIX + trimStart('/')
