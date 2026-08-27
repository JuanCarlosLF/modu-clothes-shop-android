package com.juancarloslf.modu.data.dataSource.remote.product.dto

import com.google.gson.annotations.SerializedName

data class ProductWrapperDto(
    @SerializedName("data")
    val products: List<ProductDto>,
    val meta: MetaDataDto
)