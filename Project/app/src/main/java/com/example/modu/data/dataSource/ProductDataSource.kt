package com.example.modu.data.dataSource

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.domain.entity.product.Product

interface ProductDataSource {
    fun getProductsPagingSource(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): PagingSource<Int, Product>
    suspend fun getCategories() : List<CategoryDto>
}