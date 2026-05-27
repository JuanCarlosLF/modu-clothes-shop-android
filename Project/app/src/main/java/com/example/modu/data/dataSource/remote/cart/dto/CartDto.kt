package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CartDto(
    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("totalPrice")
    val totalPrice: BigDecimal? = null,

    @SerializedName("cart_items")
    val cartItems: List<CartItemDto>? = null,

    @SerializedName("price_changed_alert")
    val priceChangedAlert: PriceChangedAlertDto? = null,

    @SerializedName("insufficient_stock_alert")
    val insufficientStockAlert: InsufficientStockAlertDto? = null,

    @SerializedName("variant_availability_alert")
    val variantAvailabilityAlert: VariantAvailabilityAlertDto? = null
)