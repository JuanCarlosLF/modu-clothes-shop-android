package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.domain.entity.cart.CartItem

fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = id,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}

fun CartItem.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = id,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}