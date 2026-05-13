package com.example.modu.data.dataSource.remote.product.dto

import com.google.gson.annotations.SerializedName

data class ProductWrapper(
    @SerializedName("data") val products: List<ProductDto>,
    val meta: MetaDataDto
)