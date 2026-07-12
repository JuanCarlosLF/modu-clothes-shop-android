package com.example.modu.data.repository.product

import androidx.paging.PagingData
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.repository.product.ProductRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class DemoProductRepositoryImpl @Inject constructor() : ProductRepository {

    override suspend fun getCategories(): List<Category> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailById(id: Int): Detail {
        TODO("Not yet implemented")
    }

    override fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRelatedProducts(category: List<String>): List<Product> {
        TODO("Not yet implemented")
    }
}