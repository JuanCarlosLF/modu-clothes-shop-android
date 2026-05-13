package com.example.modu.domain.usecase

import com.example.modu.domain.entity.ClothesItem
import com.example.modu.domain.repository.clothes.ClothesRepository
import javax.inject.Inject

class ClothesUseCaseImpl @Inject constructor(private val repository: ClothesRepository) : ClothesUseCase {

    override suspend fun getClothes(): List<ClothesItem> = repository.getClothes()
}