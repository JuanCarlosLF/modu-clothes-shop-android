package com.example.modu.domain.usecase

import com.example.modu.domain.entity.Product

interface ProductUseCase {
    suspend fun getProducts() : List<Product>
}