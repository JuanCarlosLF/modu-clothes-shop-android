package com.example.modu.domain.repository.product

import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getCategories(): List<Category>
}