package com.example.modu.data.dataSource.remote

import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(private val api: ProductApi) : ProductDataSource {

    override suspend fun getProductsBy(): List<ProductDto> = api.getProductsByBy().products
}