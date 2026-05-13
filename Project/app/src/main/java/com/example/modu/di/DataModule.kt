package com.example.modu.di

import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.ProductRemoteDataSourceImpl
import com.example.modu.data.dataSource.remote.product.exception.DataErrorHandlerImpl
import com.example.modu.data.dataSource.remote.product.exception.ErrorHandler
import com.example.modu.data.repository.product.ProductRepositoryImpl
import com.example.modu.domain.repository.products.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorHandlerModule {

    @Binds
    @Singleton
    abstract fun bindErrorHandler(dataErrorHandlerImpl: DataErrorHandlerImpl): ErrorHandler

    @Binds
    @Singleton
    abstract fun bindProductDataSource(dataSourceImpl: ProductRemoteDataSourceImpl) : ProductDataSource

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl) : ProductRepository
}