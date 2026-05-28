package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.InsufficientStockAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceChangedAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.StockAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.VariantAvailabilityItemDto
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.InsufficientStockAlert
import com.example.modu.domain.entity.cart.PriceAlertItem
import com.example.modu.domain.entity.cart.PriceChangedAlert
import com.example.modu.domain.entity.cart.StockAlertItem
import com.example.modu.domain.entity.cart.VariantAvailabilityAlert
import com.example.modu.domain.entity.cart.VariantAvailabilityItem

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
        totalPrice = this.totalPrice
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
        totalPrice = this.totalPrice
    )
}

internal fun CartDto.toDomain(): Cart {
    return Cart(
        createdAt = requireNotNull(this.createdAt),
        updatedAt = requireNotNull(this.updatedAt),
        items = this.cartItems?.map { it.toDomain() } ?: emptyList(),
        priceAlert = this.priceChangedAlert?.toDomain(),
        stockAlert = this.insufficientStockAlert?.toDomain()
    )
}

internal fun PriceChangedAlertDto.toDomain(): PriceChangedAlert =
    PriceChangedAlert(
        items = requireNotNull(this.cartItems).map { it.toDomain() }
    )

internal fun PriceAlertItemDto.toDomain(): PriceAlertItem {
    return PriceAlertItem(
        productVariantId = this.productVariantId,
        oldPrice = requireNotNull(this.oldPrice),
        newPrice = requireNotNull(this.newPrice)
    )
}

internal fun InsufficientStockAlertDto.toDomain(): InsufficientStockAlert =
    InsufficientStockAlert(
        items = requireNotNull(this.cartItems).map { it.toDomain() }
    )

internal fun StockAlertItemDto.toDomain(): StockAlertItem {
    return StockAlertItem(
        productVariantId = requireNotNull(this.productVariantId),
        requestedQuantity = requireNotNull(this.requestedQuantity),
        availableStock = requireNotNull(this.availableStock)
    )
}

internal fun VariantAvailabilityAlertDto.toDomain(): VariantAvailabilityAlert =
    VariantAvailabilityAlert(
        cartItems = requireNotNull(this.cartItems).map { it.toDomain() }
    )

internal fun VariantAvailabilityItemDto.toDomain(): VariantAvailabilityItem {
    return VariantAvailabilityItem(
        cartItemId = requireNotNull(this.cartItemId),
        productVariantId = requireNotNull(this.productVariantId),
        isVariantAvailable = requireNotNull(this.isVariantAvailable)
    )
}