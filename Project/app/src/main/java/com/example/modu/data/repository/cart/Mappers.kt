package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemWithDetailsDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.CartSummaryDto
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem

internal fun CartDto.toCartDbo(): CartDbo {
    val summary = requireNotNull(this.cartSummary)
    return CartDbo(
        subTotal = requireNotNull(summary.subTotalPrice),
        shippingCost = requireNotNull(summary.shippingCosts),
        total = requireNotNull(summary.totalPrice),
        createdAt = requireNotNull(summary.createdAt),
        updatedAt = requireNotNull(summary.updatedAt)
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

internal fun CartItemDto.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        productId = requireNotNull(this.productId),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = requireNotNull(this.currentStock),
        quantity = requireNotNull(this.quantity),
        unitPrice = requireNotNull(this.unitPrice),
        totalPrice = requireNotNull(this.totalPrice),
        title = "",
        imageUrl = "",
        size = "",
        color = ""
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
        totalPrice = requireNotNull(this.totalPrice),
        oldPriceAlert = null,
        stockAlertAvailable = null,
        isAvailableAlert = null
    )
}

internal fun CartItem.toDto(): CartItemDto {
    return CartItemDto(
        id = this.id,
        productId = this.productId,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock?: 0,
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

internal fun CartItemWithDetailsDbo.toDomain(): CartItem {
    return CartItem(
        id = this.cartItem.id,
        productId = this.cartItem.productId,
        productVariantId = this.cartItem.productVariantId,
        currentStock = this.cartItem.currentStock,
        quantity = this.cartItem.quantity,
        unitPrice = this.cartItem.unitPrice,
        totalPrice = this.cartItem.totalPrice,
        title = this.productDetail.firstOrNull()?.name.orEmpty(),
        imageUrl = this.productDetail.firstOrNull()?.imageUrl.orEmpty(),
        size = this.productVariant.firstOrNull()?.size.orEmpty(),
        color = this.productVariant.firstOrNull()?.color.orEmpty(),
        oldPriceAlert = this.cartItem.oldPriceAlert,
        stockAlertAvailable = this.cartItem.stockAlertAvailable,
        isAvailableAlert = this.cartItem.isAvailableAlert
    )
}

internal fun CartResponseDto.toCartDto(): CartDto {
    return CartDto(
        cartSummary = CartSummaryDto(
            deviceId = this.deviceId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            subTotalPrice = this.subTotalPrice,
            shippingCosts = this.shippingCosts,
            totalPrice = this.totalPrice,
            cartItems = this.cartItems
        ),
        priceChangedAlert = null,
        insufficientStockAlert = null,
        variantAvailabilityAlert = null
    )
}