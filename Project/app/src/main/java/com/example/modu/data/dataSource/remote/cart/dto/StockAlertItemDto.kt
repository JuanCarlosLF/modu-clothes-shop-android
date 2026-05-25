package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class StockAlertItemDto(
    val id: Int,
    @SerializedName("requested_quantity")
    val requestedQuantity: Int,
    @SerializedName("available_quantity")
    val availableQuantity: Int
)