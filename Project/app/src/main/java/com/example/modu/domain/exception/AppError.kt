package com.example.modu.domain.exception

enum class ErrorType {
    NO_INTERNET,
    UNKNOWN,
    VALIDATION_ERROR,
    NOT_FOUND,
    UNAUTHORIZED,
    FORBIDDEN,
    CONFLICT,
    INTERNAL_ERROR
}

data class AppError(
    val type: ErrorType,
    val title: String?,
    override val message: String?,
    val fields: List<NetworkFieldError>? = emptyList()
) : Exception(message)

data class NetworkFieldError(
    val field: String?,
    val message: String?
)