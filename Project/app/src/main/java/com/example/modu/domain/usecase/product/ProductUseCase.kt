package com.example.modu.domain.usecase.product

import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product

interface ProductUseCase {
    suspend fun getProducts(): List<Product>

    suspend fun getCategories(): List<Category>
}