package com.example.modu.data.repository.product

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.modu.data.dataSource.local.demo.database.product.DemoProductDataSource
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.repository.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val ITEMS_PER_PAGE = 20
private const val CENTS_PER_UNIT = 100L

class DemoProductRepositoryImpl @Inject constructor(
    private val dataSource: DemoProductDataSource
) : ProductRepository {

    override suspend fun getCategories(): List<Category> =
        dataSource.getCategories().map { it.toDomain() }

    override suspend fun getDetailById(id: Int): Detail =
        dataSource.getDetailById(id)?.toDomain()
            ?: throw NoSuchElementException("Product $id not found")

    override fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(
                pageSize = ITEMS_PER_PAGE,
                initialLoadSize = ITEMS_PER_PAGE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                val categoryNames = categories.orEmpty().distinct()
                dataSource.getProductsBy(
                    title = title,
                    orderByPrice = orderByPrice,
                    maxPriceInCents = maxPrice?.toLong()?.times(CENTS_PER_UNIT),
                    categoryNames = categoryNames,
                    categoryCount = categoryNames.size
                )
            }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getRelatedProducts(category: List<String>): List<Product> {
        val categoryNames = category.distinct()
        val categoriesDbos = dataSource.getRelatedProducts(categoryNames, categoryNames.size)

        return categoriesDbos.map { it.toDomain() }
    }
}
