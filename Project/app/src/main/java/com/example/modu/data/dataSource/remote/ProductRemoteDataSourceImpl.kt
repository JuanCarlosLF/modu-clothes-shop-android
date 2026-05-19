package com.example.modu.data.dataSource.remote

import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(private val api: ProductApi) : ProductDataSource {

    override suspend fun getProductsBy(): List<ProductDto> = api.getProductsBy().products

    override suspend fun getCategories(): List<CategoryDto> = api.getCategories()
}