package com.example.modu.di

import com.example.modu.data.repository.cart.DemoCartRepositoryImpl
import com.example.modu.data.repository.product.DemoProductRepositoryImpl
import com.example.modu.domain.repository.cart.CartRepository
import com.example.modu.domain.repository.product.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoDataModule {

    @Binds
    @Singleton
    abstract fun provideDemoCartRepository(demoCartRepository: DemoCartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun provideDemoProductRepository(demoProductRepository: DemoProductRepositoryImpl): ProductRepository
}