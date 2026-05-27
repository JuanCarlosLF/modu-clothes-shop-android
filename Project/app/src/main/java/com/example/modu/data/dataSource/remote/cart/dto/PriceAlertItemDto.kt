package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class PriceAlertItemDto(
    val productVariantId: Int,
    val oldPrice: Double? = null,
    val newPrice: Double? = null
)