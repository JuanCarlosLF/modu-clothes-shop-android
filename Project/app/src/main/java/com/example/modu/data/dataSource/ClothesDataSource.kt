package com.example.modu.data.dataSource

import com.example.modu.data.dataSource.remote.clothes.dto.ClothesItemDto
import com.example.modu.domain.entity.ClothesItem

interface ClothesDataSource {
    suspend fun getClothes() : List<ClothesItemDto>
}