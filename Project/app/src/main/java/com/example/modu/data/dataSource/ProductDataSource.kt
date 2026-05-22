package com.example.modu.data.dataSource

import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto

interface ProductDataSource {
    suspend fun getProducts(): List<ProductDto>
    suspend fun getDetailById(id: Int): DetailDto
    suspend fun getCategories(): List<CategoryDto>
}