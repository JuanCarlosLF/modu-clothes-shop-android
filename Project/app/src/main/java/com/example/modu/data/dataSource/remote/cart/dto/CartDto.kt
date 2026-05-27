package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CartDto(
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val subTotalPrice: BigDecimal? = null,
    val shippingCost: BigDecimal? = null,
    val totalPrice: BigDecimal? = null,
    val cartItems: List<CartItemDto>? = null,
    val priceChangedAlert: PriceChangedAlertDto? = null,
    val insufficientStockAlert: InsufficientStockAlertDto? = null,
    val variantAvailabilityAlert: VariantAvailabilityAlertDto? = null
)