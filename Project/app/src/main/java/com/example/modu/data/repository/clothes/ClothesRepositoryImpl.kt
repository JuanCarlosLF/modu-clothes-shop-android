package com.example.modu.data.repository.clothes

import com.example.modu.data.dataSource.ClothesDataSource
import com.example.modu.data.dataSource.remote.clothes.exception.ErrorHandler
import com.example.modu.domain.entity.ClothesItem
import com.example.modu.domain.repository.clothes.ClothesRepository
import javax.inject.Inject

class ClothesRepositoryImpl @Inject constructor(private val dataSource: ClothesDataSource, private val errorHandler: ErrorHandler) : ClothesRepository {
    override suspend fun getClothes(): List<ClothesItem> {
        return try {
            dataSource.getClothes().map { it.toDomain() }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }
}