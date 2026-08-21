package com.example.modu.di

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSourceImpl
import com.example.modu.data.dataSource.remote.exception.DataErrorHandlerImpl
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonDataModule {

    @Binds
    @Singleton
    abstract fun bindErrorHandler(dataErrorHandlerImpl: DataErrorHandlerImpl): ErrorHandler

    @Binds
    @Singleton
    abstract fun bindCartLocalDataSource(dataSourceImpl: CartLocalDataSourceImpl): CartLocalDataSource
}