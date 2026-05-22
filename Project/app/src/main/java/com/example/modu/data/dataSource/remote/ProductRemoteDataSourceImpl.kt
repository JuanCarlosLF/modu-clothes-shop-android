package com.example.modu.data.dataSource.remote

import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val api: ProductApi
) : ProductDataSource {
    override suspend fun getProducts(): List<ProductDto> = api.getProducts().products

    override suspend fun getCategories(): List<CategoryDto> = api.getCategories()
    override suspend fun getDetailById(id: Int): DetailDto = api.getDetailById(id)
}