package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class PriceChangedAlertDto(
    @SerializedName("cart_items")
    val cartItems: List<PriceAlertItemDto>? = null
)