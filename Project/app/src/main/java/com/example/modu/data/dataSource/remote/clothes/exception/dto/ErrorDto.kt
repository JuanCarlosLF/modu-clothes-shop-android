package com.example.modu.data.dataSource.remote.clothes.exception.dto

data class ErrorDto(
    var error: ErrorContentDto = ErrorContentDto()
)

data class ErrorContentDto(
    var type: String = "",
    val message: String = "",
    val fields: List<ErrorFieldDto> = emptyList()
)

data class ErrorFieldDto(
    val field: String,
    val message: String
)