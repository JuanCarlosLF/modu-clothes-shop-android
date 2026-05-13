package com.example.modu.di

import com.example.modu.domain.usecase.ProductUseCase
import com.example.modu.domain.usecase.ProductUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindProductUseCase(productsUseCase: ProductUseCaseImpl) : ProductUseCase
}