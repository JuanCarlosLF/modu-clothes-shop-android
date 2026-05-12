package com.example.modu.di

import com.example.modu.data.dataSource.remote.clothes.exception.DataErrorHandlerImpl
import com.example.modu.data.dataSource.remote.clothes.exception.ErrorHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorHandlerModule {

    @Binds
    @Singleton
    abstract fun bindErrorHandler(
        dataErrorHandlerImpl: DataErrorHandlerImpl
    ): ErrorHandler
}