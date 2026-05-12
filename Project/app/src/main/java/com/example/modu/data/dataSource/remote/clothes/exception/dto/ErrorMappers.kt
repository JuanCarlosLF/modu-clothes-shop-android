package com.example.modu.data.dataSource.remote.clothes.exception.dto

import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.exception.NetworkFieldError


fun ErrorDto.toModel(): AppError = this.error.toModel()

private fun ErrorContentDto.toModel(): AppError {
    val type = try {
        ErrorType.valueOf(this.type)
    } catch (e: IllegalArgumentException) {
        ErrorType.UNKNOWN
    }

    return AppError(
        type = type,
        title = this.type,
        message = this.message,
        fields = this.fields.map { field -> field.toModel() }
    )
}

private fun ErrorFieldDto.toModel(): NetworkFieldError =
    NetworkFieldError(
        field = this.field,
        message = this.message
    )