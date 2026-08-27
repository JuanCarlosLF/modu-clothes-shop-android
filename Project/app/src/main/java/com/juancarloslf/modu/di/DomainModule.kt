package com.juancarloslf.modu.di

import com.juancarloslf.modu.domain.usecase.cart.CartUseCase
import com.juancarloslf.modu.domain.usecase.cart.CartUseCaseImpl
import com.juancarloslf.modu.domain.usecase.product.ProductUseCase
import com.juancarloslf.modu.domain.usecase.product.ProductUseCaseImpl
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

    @Binds
    @Singleton
    abstract fun bindCartUseCase(cartUseCaseImpl: CartUseCaseImpl): CartUseCase
}