package com.example.modu.data.dataSource.remote.product

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto

interface ProductDataSource {
    fun getPaginatedProducts(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?,
        mapException: (Exception) -> Exception
    ): PagingSource<Int, ProductDto>

    suspend fun getCategories(): List<CategoryDto>
    suspend fun getRelatedProducts(categories: List<String>): List<ProductDto>
    suspend fun getDetailById(id: Int): DetailDto
}