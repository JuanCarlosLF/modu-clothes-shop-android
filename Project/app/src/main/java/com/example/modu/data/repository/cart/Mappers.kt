package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

private const val BID_DECIMAL_CONVERSION_MULT = 100

internal fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = id,
        productId = productId,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = BigDecimal.valueOf(unitPrice).divide(BigDecimal(BID_DECIMAL_CONVERSION_MULT)),
        totalPrice = BigDecimal.valueOf(totalPrice).divide(BigDecimal(BID_DECIMAL_CONVERSION_MULT))
    )
}

internal fun CartItem.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = id,
        productId = productId,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice.multiply(BigDecimal(BID_DECIMAL_CONVERSION_MULT)).toLong(),
        totalPrice = totalPrice.multiply(BigDecimal(BID_DECIMAL_CONVERSION_MULT)).toLong()
    )
}