package com.example.modu.data.dataSource

import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto

interface ProductDataSource {
    suspend fun getProductsBy() : List<ProductDto>
    suspend fun getCategories() : List<CategoryDto>
}