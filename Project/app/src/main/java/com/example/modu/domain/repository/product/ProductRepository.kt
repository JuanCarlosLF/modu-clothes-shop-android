package com.example.modu.domain.repository.product

import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product

interface ProductRepository {
    suspend fun getProductsBy() : List<Product>

    suspend fun getCategories() : List<Category>
}