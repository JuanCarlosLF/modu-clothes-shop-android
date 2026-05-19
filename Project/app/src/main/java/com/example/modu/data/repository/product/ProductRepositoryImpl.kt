package com.example.modu.data.repository.product

import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.exception.ErrorHandler
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.repository.products.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dataSource: ProductDataSource,
    private val errorHandler: ErrorHandler
) : ProductRepository {

    override suspend fun getProductsBy(): List<Product> {
        return try {
            dataSource.getProductsBy().map { it.toDomain() }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
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