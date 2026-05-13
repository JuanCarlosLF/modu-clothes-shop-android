package com.example.modu.domain.repository.clothes

import com.example.modu.domain.entity.ClothesItem

interface ClothesRepository {
    suspend fun getClothes() : List<ClothesItem>
}