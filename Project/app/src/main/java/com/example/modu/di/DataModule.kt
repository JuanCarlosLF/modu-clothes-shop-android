package com.example.modu.di

import com.example.modu.data.dataSource.ClothesDataSource
import com.example.modu.data.dataSource.remote.ClothesRemoteDataSourceImpl
import com.example.modu.data.dataSource.remote.clothes.exception.DataErrorHandlerImpl
import com.example.modu.data.dataSource.remote.clothes.exception.ErrorHandler
import com.example.modu.data.repository.clothes.ClothesRepositoryImpl
import com.example.modu.domain.repository.clothes.ClothesRepository
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
    abstract fun bindErrorHandler(
        dataErrorHandlerImpl: DataErrorHandlerImpl): ErrorHandler

    @Binds
    @Singleton
    abstract fun bindClothesDataSource(dataSourceImpl: ClothesRemoteDataSourceImpl) : ClothesDataSource

    @Binds
    @Singleton
    abstract fun bindClothesRepository(clothesRepositoryImpl: ClothesRepositoryImpl) : ClothesRepository
}