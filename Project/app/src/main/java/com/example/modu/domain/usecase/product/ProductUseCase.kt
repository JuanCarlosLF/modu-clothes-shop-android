package com.example.modu.domain.usecase.product

import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.category.Category

interface ProductUseCase {
    suspend fun getProductsBy() : List<Product>
    suspend fun getCategories() : List<Category>
}