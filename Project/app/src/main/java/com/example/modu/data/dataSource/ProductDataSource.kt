package com.example.modu.data.dataSource

import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import com.example.modu.data.dataSource.remote.product.dto.detail.CategoryDto

interface ProductDataSource {
    suspend fun getProductsBy(): List<ProductDto>
    suspend fun getDetailById(id: Int): DetailDto
    suspend fun getCategories() : List<CategoryDto>
}