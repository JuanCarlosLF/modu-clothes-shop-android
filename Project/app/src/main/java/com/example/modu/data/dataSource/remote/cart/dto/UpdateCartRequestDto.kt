package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class UpdateCartRequestDto(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("cart_items"
    ) val cartItems: List<CartItemDto>
)