package com.example.modu.data.dataSource.remote.exception.dto

import com.google.gson.annotations.SerializedName

data class ErrorContentDto(
    @SerializedName("type")
    var title: String? = null,
    val message: String? = null,
    val fields: List<ErrorFieldDto>? = null
)