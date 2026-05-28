package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.domain.entity.cart.CartItem

internal fun CartItemDto.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        productId = requireNotNull(this.productId),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = requireNotNull(this.currentStock),
        quantity = requireNotNull(this.quantity),
        unitPrice = requireNotNull(this.unitPrice),
        totalPrice = requireNotNull(this.totalPrice)
    )
}

internal fun CartItemDto.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = this.id,
        productId = requireNotNull(this.productId),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = requireNotNull(this.currentStock),
        quantity = requireNotNull(this.quantity),
        unitPrice = requireNotNull(this.unitPrice),
        totalPrice = requireNotNull(this.totalPrice)
    )
}

internal fun CartItem.toDto(): CartItemDto {
    return CartItemDto(
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
        totalPrice = this.totalPrice,
        oldPriceAlert = this.oldPriceAlert,
        stockAlertAvailable = this.stockAlertAvailable,
        isAvailableAlert = this.isAvailableAlert
    )
}

internal fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        productId = this.productId,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
        oldPriceAlert = this.oldPriceAlert,
        stockAlertAvailable = this.stockAlertAvailable,
        isAvailableAlert = this.isAvailableAlert
    )
}