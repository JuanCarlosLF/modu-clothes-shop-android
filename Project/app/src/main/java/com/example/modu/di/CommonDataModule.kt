package com.example.modu.di

import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSourceImpl
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSourceImpl
import com.example.modu.data.dataSource.remote.exception.DataErrorHandlerImpl
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.data.dataSource.remote.product.ProductRemoteDataSourceImpl
import com.example.modu.data.repository.cart.CartRepositoryImpl
import com.example.modu.data.repository.product.ProductRepositoryImpl
import com.example.modu.domain.repository.cart.CartRepository
import com.example.modu.domain.repository.product.ProductRepository
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