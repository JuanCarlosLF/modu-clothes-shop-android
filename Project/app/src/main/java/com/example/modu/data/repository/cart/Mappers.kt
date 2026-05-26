package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

internal fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = id,
        productId = productId,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = BigDecimal.valueOf(unitPrice).divide(BigDecimal(100)),
        totalPrice = BigDecimal.valueOf(totalPrice).divide(BigDecimal(100))
    )
}

internal fun CartItem.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = id,
        productId = productId,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice.multiply(BigDecimal(100)).toLong(),
        totalPrice = totalPrice.multiply(BigDecimal(100)).toLong()
    )
}