package com.example.modu.di

import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSource
import com.example.modu.data.dataSource.remote.cart.CartRemoteDataSourceImpl
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
abstract class RemoteDataModule {

@Binds
@Singleton
abstract fun bindProductDataSource(dataSourceImpl: ProductRemoteDataSourceImpl): ProductDataSource

@Binds
@Singleton
abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository


@Binds
@Singleton
abstract fun bindCartRemoteDataSource(dataSourceImpl: CartRemoteDataSourceImpl): CartRemoteDataSource

@Binds
@Singleton
abstract fun bindCartRepository(cartRepositoryImpl: CartRepositoryImpl): CartRepository}