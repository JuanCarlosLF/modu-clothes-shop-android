package com.example.modu.di

import com.example.modu.data.dataSource.local.demo.database.cart.DemoCartDataSource
import com.example.modu.data.dataSource.local.demo.database.cart.DemoCartDataSourceImpl
import com.example.modu.data.dataSource.local.demo.database.product.DemoProductDataSource
import com.example.modu.data.dataSource.local.demo.database.product.DemoProductDataSourceImpl
import com.example.modu.data.repository.cart.DemoCartRepositoryImpl
import com.example.modu.data.repository.demo.product.DemoProductRepositoryImpl
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
    abstract fun provideDemoProductDataSource(demoProductDataSource: DemoProductDataSourceImpl): DemoProductDataSource

    @Binds
    @Singleton
    abstract fun provideDemoProductRepository(demoProductRepository: DemoProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun provideDemoCartDataSource(demoCartDataSource: DemoCartDataSourceImpl): DemoCartDataSource

    @Binds
    @Singleton
    abstract fun provideDemoCartRepository(demoCartRepository: DemoCartRepositoryImpl): CartRepository
}
