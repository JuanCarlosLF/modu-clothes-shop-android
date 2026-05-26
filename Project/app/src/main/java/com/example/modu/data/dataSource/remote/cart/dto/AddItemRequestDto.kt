package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class AddItemRequestDto(
    @SerializedName("device_id")
    val deviceId: String? = null,
    @SerializedName("product_variant_id")
    val productVariantId: Int? = null,
    val quantity: Int? = null
)