package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class PriceChangedAlertDto(
    val cartItems: List<PriceAlertItemDto>? = null
)