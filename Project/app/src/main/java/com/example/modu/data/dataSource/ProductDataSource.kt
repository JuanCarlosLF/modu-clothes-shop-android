package com.example.modu.data.dataSource

import com.example.modu.data.dataSource.remote.product.dto.ProductDto

interface ProductDataSource {
    suspend fun getProductsBy() : List<ProductDto>
}