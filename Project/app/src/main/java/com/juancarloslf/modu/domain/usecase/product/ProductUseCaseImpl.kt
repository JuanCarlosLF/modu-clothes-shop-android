package com.juancarloslf.modu.domain.usecase.product

import androidx.paging.PagingData
import com.juancarloslf.modu.domain.entity.detail.Detail
import com.juancarloslf.modu.domain.entity.product.Category
import com.juancarloslf.modu.domain.entity.product.Product
import com.juancarloslf.modu.domain.repository.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductUseCaseImpl @Inject constructor(
    private val repository: ProductRepository
) : ProductUseCase {

    override fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>> {
        return repository.getProductsBy(title, orderByPrice, maxPrice, categories)
    }
    override suspend fun getDetailById(id: Int): Detail = repository.getDetailById(id)
    override suspend fun getCategories(): List<Category> = repository.getCategories()
    override suspend fun getRelatedProducts(category: List<String>): List<Product> =
        repository.getRelatedProducts(category)
}