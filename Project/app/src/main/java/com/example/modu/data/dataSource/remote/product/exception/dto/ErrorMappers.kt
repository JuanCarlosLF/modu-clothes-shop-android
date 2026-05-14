package com.example.modu.data.dataSource.remote.product.exception.dto

import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.exception.NetworkFieldError

internal fun ErrorDto.toDomain(): AppError = this.content.toDomain()

private fun ErrorContentDto.toDomain(): AppError {
    val type = try {
        ErrorType.valueOf(requireNotNull(this.title))
    } catch (_: IllegalArgumentException) {
        ErrorType.UNKNOWN
    }

    return AppError(
        type = type,
        title = this.title,
        message = this.message,
        fields = this.fields?.map { field -> field.toDomain() }
    )
}

private fun ErrorFieldDto.toDomain(): NetworkFieldError =
    NetworkFieldError(
        field = this.field,
        message = this.message
    )