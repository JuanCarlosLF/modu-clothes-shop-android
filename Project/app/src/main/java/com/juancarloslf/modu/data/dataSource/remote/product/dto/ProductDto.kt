package com.juancarloslf.modu.data.dataSource.remote.product.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("product_id")
    val id: Int? = null,
    @SerializedName("url")
    val image: String? = null
)