package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class AddItemRequestDto(
    @SerializedName("variant_id")
    val productVariantId: Int? = null,
    val quantity: Int? = null
)