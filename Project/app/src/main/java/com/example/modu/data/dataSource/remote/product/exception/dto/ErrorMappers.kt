package com.example.modu.data.dataSource.remote.product.exception.dto

import android.content.Context
import com.example.modu.R
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.example.modu.domain.exception.NetworkFieldError

internal fun ErrorDto.toDomain(context: Context): AppError = this.content.toDomain(context)

private fun ErrorContentDto.toDomain(context: Context): AppError {
    val type = try {
        ErrorType.valueOf(requireNotNull(this.title))
    } catch (_: IllegalArgumentException) {
        ErrorType.UNKNOWN
    }

    return AppError(
        type = type,
        title = this.title ?: context.getString(R.string.general_error_title),
        message = this.message ?: context.getString(R.string.general_error_msg),
        fields = this.fields?.map { field -> field.toDomain() }
    )
}

private fun ErrorFieldDto.toDomain(): NetworkFieldError =
    NetworkFieldError(
        field = this.field,
        message = this.message
    )