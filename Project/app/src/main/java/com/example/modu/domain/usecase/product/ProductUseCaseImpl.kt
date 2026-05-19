package com.example.modu.domain.usecase.product

import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.repository.products.ProductRepository
import javax.inject.Inject

class ProductUseCaseImpl @Inject constructor(
    private val repository: ProductRepository
) : ProductUseCase {

    override suspend fun getProductsBy(): List<Product> = repository.getProductsBy()

    override suspend fun getCategories(): List<Category> = repository.getCategories()
}