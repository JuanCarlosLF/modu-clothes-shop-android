package com.example.modu.data.dataSource.remote.clothes.exception.dto

import com.google.gson.annotations.SerializedName

data class ErrorDto(
    @SerializedName("error")
    var error: ErrorContentDto = ErrorContentDto()
)

data class ErrorContentDto(
    @SerializedName("type")
    var type: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("fields")
    val fields: List<ErrorFieldDto> = emptyList()
)

data class ErrorFieldDto(
    @SerializedName("field")
    val field: String,

    @SerializedName("message")
    val message: String
)