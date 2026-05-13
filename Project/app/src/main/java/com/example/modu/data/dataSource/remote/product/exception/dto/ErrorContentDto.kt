package com.example.modu.data.dataSource.remote.product.exception.dto

data class ErrorContentDto(
    var type: String = "",
    val message: String = "",
    val fields: List<ErrorFieldDto> = emptyList()
)