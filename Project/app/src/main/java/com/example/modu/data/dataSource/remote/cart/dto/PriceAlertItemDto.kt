package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class PriceAlertItemDto(
    val id: Int,
    @SerializedName("old_price")
    val oldPrice: Double,
    @SerializedName("new_price")
    val newPrice: Double
)