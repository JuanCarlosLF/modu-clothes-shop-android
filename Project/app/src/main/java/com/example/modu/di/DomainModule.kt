package com.example.modu.di

import com.example.modu.domain.repository.clothes.ClothesRepository
import com.example.modu.domain.usecase.ClothesUseCase
import com.example.modu.domain.usecase.ClothesUseCaseImpl
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
    abstract fun bindClothesUseCase(clothesUseCase: ClothesUseCaseImpl) : ClothesUseCase
}