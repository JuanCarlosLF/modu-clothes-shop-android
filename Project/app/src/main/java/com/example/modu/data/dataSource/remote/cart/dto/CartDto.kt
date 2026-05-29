package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class CartDto(
    val cartSummary: CartSummaryDto? = null,
    val priceChangedAlert: PriceChangedAlertDto? = null,
    val insufficientStockAlert: InsufficientStockAlertDto? = null,
    val variantAvailabilityAlert: VariantAvailabilityAlertDto? = null
)