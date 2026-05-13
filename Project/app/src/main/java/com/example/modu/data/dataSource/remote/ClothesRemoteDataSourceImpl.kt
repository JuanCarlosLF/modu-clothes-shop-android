package com.example.modu.data.dataSource.remote

import com.example.modu.data.dataSource.ClothesDataSource
import com.example.modu.data.dataSource.remote.clothes.api.ClothesApi
import com.example.modu.data.dataSource.remote.clothes.dto.ClothesItemDto
import javax.inject.Inject

class ClothesRemoteDataSourceImpl @Inject constructor(private val api: ClothesApi) : ClothesDataSource {

    override suspend fun getClothes(): List<ClothesItemDto> = api.getClothes().data
}