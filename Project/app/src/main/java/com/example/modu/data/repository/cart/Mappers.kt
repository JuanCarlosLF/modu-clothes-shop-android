package com.example.modu.data.mapper

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.remote.cart.dto.*
import com.example.modu.domain.entity.cart.*

fun CartItemDto.toDomain(): CartItem {
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

fun CartItemDto.toDbo(): CartItemDbo {
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

fun CartItem.toDto(): CartItemDto {
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

fun CartItem.toDbo(): CartItemDbo {
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

fun CartItemDbo.toDomain(): CartItem {
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

fun CartDto.toDomain(): Cart {
    return Cart(
        createdAt = requireNotNull(this.createdAt),
        updatedAt = requireNotNull(this.updatedAt),
        items = this.cartItems?.map { it.toDomain() } ?: emptyList(),
        priceAlert = this.priceChangedAlert?.toDomain(),
        stockAlert = this.insufficientStockAlert?.toDomain()
    )
}

fun PriceChangedAlertDto.toDomain(): PriceChangedAlert {
    return PriceChangedAlert(
        items = requireNotNull(this.cartItems).map { it.toDomain() }
    )
}

fun PriceAlertItemDto.toDomain(): PriceAlertItem {
    return PriceAlertItem(
        productVariantId = this.productVariantId,
        oldPrice = requireNotNull(this.oldPrice),
        newPrice = requireNotNull(this.newPrice)
    )
}

fun InsufficientStockAlertDto.toDomain(): InsufficientStockAlert {
    return InsufficientStockAlert(
        items = requireNotNull(this.cartItems).map { it.toDomain() }
    )
}

fun StockAlertItemDto.toDomain(): StockAlertItem {
    return StockAlertItem(
        productVariantId = requireNotNull(this.productVariantId),
        requestedQuantity = requireNotNull(this.requestedQuantity),
        availableStock = requireNotNull(this.availableStock)
    )
}

fun VariantAvailabilityAlertDto.toDomain(): VariantAvailabilityAlert {
    return VariantAvailabilityAlert(
        cartItems = requireNotNull(this.cartItems).map { it.toDomain() }
    )
}

fun VariantAvailabilityItemDto.toDomain(): VariantAvailabilityItem {
    return VariantAvailabilityItem(
        cartItemId = requireNotNull(this.cartItemId),
        productVariantId = requireNotNull(this.productVariantId),
        isVariantAvailable = requireNotNull(this.isVariantAvailable)
    )
}