package com.example.modu.data.dataSource.remote.product.dto

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class MetaDataDto(
    val page: Int,
    val size: Int,
    @SerializedName("has_next")
    val hasNext: Boolean
)