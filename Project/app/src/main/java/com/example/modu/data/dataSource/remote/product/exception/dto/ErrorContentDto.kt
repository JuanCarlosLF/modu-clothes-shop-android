package com.example.modu.data.dataSource.remote.product.exception.dto

import com.google.gson.annotations.SerializedName

data class ErrorContentDto(
    @SerializedName("type")
    var title: String = "",
    val message: String = "",
    val fields: List<ErrorFieldDto> = emptyList()
)