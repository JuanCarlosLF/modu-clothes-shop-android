package com.example.modu.domain.repository.product

import androidx.paging.PagingData
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>>

    suspend fun getCategories(): List<Category>
    suspend fun getDetailById(id: Int): Detail
    suspend fun getRelatedProducts(category: List<String>): List<Product>
}