package com.example.modu.data.dataSource.remote.exception

import android.content.Context
import com.example.modu.R
import com.example.modu.data.dataSource.remote.exception.dto.ErrorDto
import com.example.modu.data.dataSource.remote.exception.dto.toDomain
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject

private const val HTTP_UNAUTHORIZED_ERROR_CODE = 401
private const val HTTP_NOT_FOUND_ERROR_CODE = 404

class DataErrorHandlerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ErrorHandler {

    override fun handle(error: Exception): Exception {
        return when (error) {
            is IOException -> AppError(
                type = ErrorType.NO_INTERNET,
                title = context.getString(R.string.error_no_internet_title),
                message = context.getString(R.string.error_no_internet_msg)
            )

            is HttpException -> {
                val parsedError = parse(error)

                if (parsedError.type == ErrorType.UNKNOWN) {
                    when (error.code()) {
                        HTTP_UNAUTHORIZED_ERROR_CODE -> AppError(
                            type = ErrorType.UNAUTHORIZED,
                            title = context.getString(R.string.error_unauthorized_title),
                            message = context.getString(R.string.error_unauthorized_msg)
                        )
                        HTTP_NOT_FOUND_ERROR_CODE -> AppError(
                            type = ErrorType.NOT_FOUND,
                            title = context.getString(R.string.error_not_found_title),
                            message = context.getString(R.string.error_not_found_msg)
                        )
                        in 500..599 -> AppError(
                            type = ErrorType.INTERNAL_ERROR,
                            title = context.getString(R.string.error_internal_error_title),
                            message = context.getString(R.string.error_internal_error_msg)
                        )
                        else -> parsedError
                    }
                } else {
                    parsedError
                }
            }

            else -> AppError(
                type = ErrorType.UNKNOWN,
                title = context.getString(R.string.error_generic_oops),
                message = context.getString(R.string.general_error_msg)
            )
        }
    }

    private fun parse(httpException: HttpException): AppError {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            val type: Type = object : com.google.gson.reflect.TypeToken<ErrorDto>() {}.type
            val parsedError = Gson().fromJson<ErrorDto>(errorBody, type)
            parsedError.toDomain(context)
        } catch (_: Exception) {
            AppError(
                type = ErrorType.UNKNOWN,
                title = context.getString(R.string.error_generic_oops),
                message = context.getString(R.string.general_error_msg)
            )
        }
    }
}
