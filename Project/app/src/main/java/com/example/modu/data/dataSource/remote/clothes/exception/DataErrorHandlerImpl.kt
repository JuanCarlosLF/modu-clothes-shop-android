package com.example.modu.data.dataSource.remote.clothes.exception

import android.content.Context
import com.example.modu.R
import com.example.modu.data.dataSource.remote.clothes.exception.dto.ErrorDto
import com.example.modu.data.dataSource.remote.clothes.exception.dto.toModel
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.exception.ErrorType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject

class DataErrorHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ErrorHandler {

    override fun handle(error: Exception): Exception {
        return when (error) {
            is IOException -> AppError(
                type = ErrorType.NO_INTERNET,
                title = context.getString(R.string.no_internet_title),
                message = context.getString(R.string.no_internet_msg)
            )

            is HttpException -> {
                val parsedError = parse(error)

                if (parsedError.type == ErrorType.UNKNOWN) {
                    when (error.code()) {
                        401 -> AppError(
                            type = ErrorType.UNAUTHORIZED,
                            title = context.getString(R.string.error_401_title),
                            message = context.getString(R.string.error_401_msg)
                        )

                        404 -> AppError(
                            type = ErrorType.NOT_FOUND,
                            title = context.getString(R.string.error_404_title),
                            message = context.getString(R.string.error_404_msg)
                        )

                        in 500..599 -> AppError(
                            type = ErrorType.INTERNAL_ERROR,
                            title = context.getString(R.string.error_500_title),
                            message = context.getString(R.string.error_500_msg)
                        )

                        else -> parsedError
                    }
                } else {
                    parsedError
                }
            }

            else -> AppError(
                type = ErrorType.UNKNOWN,
                title = context.getString(R.string.general_error_title),
                message = context.getString(R.string.general_error_msg)
            )
        }
    }

    private fun parse(httpException: HttpException): AppError {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            val type: Type = object : TypeToken<ErrorDto>() {}.type
            val parsedError = Gson().fromJson<ErrorDto>(errorBody, type)
            parsedError.toModel()
        } catch (_: Exception) {
            AppError(
                type = ErrorType.UNKNOWN,
                title = context.getString(R.string.general_error_title),
                message = context.getString(R.string.general_error_msg)
            )
        }
    }
}
