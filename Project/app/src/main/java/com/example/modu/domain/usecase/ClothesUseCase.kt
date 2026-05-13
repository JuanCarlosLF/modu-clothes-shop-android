package com.example.modu.domain.usecase

import com.example.modu.domain.entity.ClothesItem

interface ClothesUseCase {
    suspend fun getClothes() : List<ClothesItem>
}