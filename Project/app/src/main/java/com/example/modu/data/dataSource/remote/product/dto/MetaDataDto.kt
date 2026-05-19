package com.example.modu.data.dataSource.remote.product.dto

import com.google.gson.annotations.SerializedName

data class MetaDataDto(
    val page: Int? = null,
    val size: Int? = null,
    @SerializedName("has_next")
    val hasNext: Boolean? = null
)