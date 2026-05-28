package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

internal fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        productId = this.productId,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}

internal fun CartItem.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = this.id,
        productId = this.productId,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}