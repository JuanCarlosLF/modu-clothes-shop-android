package com.example.modu.data.repository.product

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.exception.ErrorHandler
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.repository.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dataSource: ProductDataSource,
    private val errorHandler: ErrorHandler
) : ProductRepository {

    override fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>> {

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                dataSource.getProductsPagingSource(
                    title,
                    orderByPrice,
                    maxPrice,
                    categories
                ) { exception ->
                    errorHandler.handle(exception)
                }
            }
        )
            .flow
            .map { pagingDataDto ->
                pagingDataDto.map { productDto ->
                    productDto.toDomain()
                }
            }
    }

    override suspend fun getCategories(): List<Category> {
        return try {
            dataSource.getCategories().map { it.toDomain() }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }
}