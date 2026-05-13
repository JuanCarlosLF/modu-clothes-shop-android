package com.example.modu.domain.repository.products

import com.example.modu.domain.entity.Product

interface ProductRepository {
    suspend fun getProducts() : List<Product>
}