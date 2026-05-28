package com.example.modu.domain.entity.cart

import java.math.BigDecimal

data class PriceAlertItem(
    val productVariantId: Int,
    val oldPrice: BigDecimal,
    val newPrice: BigDecimal
)