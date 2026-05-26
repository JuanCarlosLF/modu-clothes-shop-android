package com.example.modu.domain.usecase.product

import androidx.paging.PagingData
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.Category
import kotlinx.coroutines.flow.Flow

interface ProductUseCase {
    fun getProductsBy(
        title: String? = null,
        orderByPrice: String? = null,
        maxPrice: Int? = null,
        categories: List<String>? = null
    ): Flow<PagingData<Product>>
    suspend fun getCategories() : List<Category>
    suspend fun getDetailById(id: Int): Detail
}