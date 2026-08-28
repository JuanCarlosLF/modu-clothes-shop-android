package com.juancarloslf.modu.data.repository.cart

import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemWithDetailsDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartCheckoutRequestDto
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartDto
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartItemDto
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartSummaryDto
import com.juancarloslf.modu.data.dataSource.remote.cart.dto.CartToOrderDto
import com.juancarloslf.modu.domain.entity.cart.Cart
import com.juancarloslf.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

internal fun CartDto.toCartDbo(): CartDbo {
    val summary = this.cartSummary
    return CartDbo(
        subTotal = summary?.subTotalPrice ?: BigDecimal.ZERO,
        shippingCost = summary?.shippingCosts ?: BigDecimal.ZERO,
        total = summary?.totalPrice ?: BigDecimal.ZERO,
        createdAt = summary?.createdAt ?: "",
        updatedAt = summary?.updatedAt ?: ""
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
        id = this.id ?: 0,
        productId = requireNotNull(this.productId),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = this.currentStock ?: 0,
        quantity = this.quantity ?: 0,
        unitPrice = this.unitPrice ?: BigDecimal.ZERO,
        totalPrice = this.totalPrice ?: BigDecimal.ZERO,
        title = "",
        imageUrl = "",
        size = "",
        color = ""
    )
}

internal fun CartItemDto.toDbo(): CartItemDbo {
    return CartItemDbo(
        id = this.id ?: 0,
        productId = requireNotNull(this.productId),
        productVariantId = requireNotNull(this.productVariantId),
        currentStock = this.currentStock ?: 0,
        quantity = this.quantity ?: 0,
        unitPrice = this.unitPrice ?: BigDecimal.ZERO,
        totalPrice = this.totalPrice ?: BigDecimal.ZERO,
        oldPriceAlert = null,
        stockAlertAvailable = null,
        isAvailableAlert = null
    )
}

internal fun CartItem.toDto(): CartItemDto {
    return CartItemDto(
        id = if (this.id < 0) null else this.id,
        productId = this.productId,
        productVariantId = this.productVariantId,
        currentStock = this.currentStock ?: 0,
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

internal fun Cart.toCheckoutRequestDto(
    isPaid: Boolean,
    specialInstructions: String
): CartCheckoutRequestDto {
    return CartCheckoutRequestDto(
        isPaid = isPaid,
        specialInstructions = specialInstructions,
        shippingCosts = this.shippingCost,
        cartToOrder = CartToOrderDto(
            cartItems = this.items.map { it.toDto() }
        )
    )
}

internal fun CartItem.toDetailDbo(): CartItemDetailDbo {
    return CartItemDetailDbo(
        id = this.productId,
        name = this.title,
        imageUrl = this.imageUrl
    )
}

internal fun CartItem.toVariantDbo(): ProductVariantDbo {
    return ProductVariantDbo(
        id = this.productVariantId,
        productId = this.productId,
        size = this.size,
        color = this.color
    )
}