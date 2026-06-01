package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

internal fun CartDto.toCartDbo(): CartDbo {
    return CartDbo(
        subTotal = this.subTotalPrice ?: BigDecimal.ZERO,
        shippingCost = this.shippingCost ?: BigDecimal.ZERO,
        total = this.totalPrice ?: BigDecimal.ZERO,
        createdAt = this.createdAt.orEmpty(),
        updatedAt = this.updatedAt.orEmpty()
    )
}

internal fun CartWithItemsDbo.toDomain(): Cart {
    return Cart(
        createdAt = this.cart.createdAt,
        updatedAt = this.cart.updatedAt,
        subTotal = this.cart.subTotal,
        shippingCost = this.cart.shippingCost,
        total = this.cart.total,
        items = this.items.map { it.toDomain() }
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
        title = "",
        imageUrl = "",
        size = "",
        color = "",
        oldPriceAlert = this.oldPriceAlert,
        stockAlertAvailable = this.stockAlertAvailable,
        isAvailableAlert = this.isAvailableAlert
    )
}