package com.example.modu.domain.entity.cart

data class PriceChangedAlert(
    val items: List<PriceAlertItem>
)

data class PriceAlertItem(
    val productVariantId: Int,
    val oldPrice: Double,
    val newPrice: Double
)