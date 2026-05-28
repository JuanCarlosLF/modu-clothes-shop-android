package com.example.modu.data.dataSource.remote.cart.dto

import java.math.BigDecimal

data class PriceAlertItemDto(
    val productVariantId: Int,
    val oldPrice: BigDecimal? = null,
    val newPrice: BigDecimal? = null
)