package com.example.modu.data.repository.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.example.modu.data.dataSource.remote.cart.dto.InsufficientStockAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceAlertItemDto
import com.example.modu.data.dataSource.remote.cart.dto.PriceChangedAlertDto
import com.example.modu.data.dataSource.remote.cart.dto.StockAlertItemDto
import com.example.modu.domain.entity.cart.Cart
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.entity.cart.InsufficientStockAlert
import com.example.modu.domain.entity.cart.PriceAlertItem
import com.example.modu.domain.entity.cart.PriceChangedAlert
import com.example.modu.domain.entity.cart.StockAlertItem

internal fun CartItemDbo.toDomain(): CartItem {
    return CartItem(
        id = id,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}

internal fun CartItem.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = id,
        productVariantId = productVariantId,
        currentStock = currentStock,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}

internal fun CartDto.toDomain(): Cart {
    return Cart(
        deviceId = requireNotNull(this.deviceId),
        createdAt = requireNotNull(this.updatedAt).toString(),
        updatedAt = requireNotNull(this.updatedAt).toString(),
        items = this.cartItems?.map { it.toDomain() } ?: emptyList(),
        priceAlert = this.priceChangedAlert?.toDomain(),
        stockAlert = this.insufficientStockAlert?.toDomain()
    )
}

internal fun CartItemDto.toDomain(): CartItem {
    return CartItem(
        id = requireNotNull(this.id),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = requireNotNull(this.currentStock),
        quantity = requireNotNull(this.quantity),
        unitPrice = requireNotNull(this.unitPrice),
        totalPrice = requireNotNull(this.totalPrice)
    )
}

internal fun PriceChangedAlertDto.toDomain(): PriceChangedAlert {
    return PriceChangedAlert(
        items = this.cartItems?.map { it.toDomain() } ?: emptyList()
    )
}

internal fun PriceAlertItemDto.toDomain(): PriceAlertItem {
    return PriceAlertItem(
        productVariantId = requireNotNull(this.id),
        oldPrice = requireNotNull(this.oldPrice),
        newPrice = requireNotNull(this.newPrice)
    )
}

internal fun InsufficientStockAlertDto.toDomain(): InsufficientStockAlert {
    return InsufficientStockAlert(
        items = this.cartItems?.map { it.toDomain() } ?: emptyList()
    )
}

internal fun StockAlertItemDto.toDomain(): StockAlertItem {
    return StockAlertItem(
        productVariantId = requireNotNull(this.id),
        requestedQuantity = requireNotNull(this.requestedQuantity),
        availableStock = requireNotNull(this.availableQuantity)
    )
}

internal fun CartItemDto.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = requireNotNull(this.id),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = requireNotNull(this.currentStock),
        quantity = requireNotNull(this.quantity),
        unitPrice = requireNotNull(this.unitPrice),
        totalPrice = requireNotNull(this.totalPrice)
    )
}

internal fun CartItemDbo.toDto(): CartItemDto {
    return CartItemDto(
        id = this.id,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}