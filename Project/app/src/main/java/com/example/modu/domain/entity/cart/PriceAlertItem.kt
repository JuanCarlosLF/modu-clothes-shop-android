package com.example.modu.domain.entity.cart

data class PriceAlertItem(
    val productVariantId: Int,
    val oldPrice: Long,
    val newPrice: Long
)