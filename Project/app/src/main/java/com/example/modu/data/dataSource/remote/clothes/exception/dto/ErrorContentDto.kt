package com.example.modu.data.dataSource.remote.clothes.exception.dto

data class ErrorContentDto(
    var type: String = "",
    val message: String = "",
    val fields: List<ErrorFieldDto> = emptyList()
)