package com.example.modu.domain.repository.products

import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.category.Category

interface ProductRepository {
    suspend fun getProductsBy() : List<Product>
    suspend fun getCategories() : List<Category>
}