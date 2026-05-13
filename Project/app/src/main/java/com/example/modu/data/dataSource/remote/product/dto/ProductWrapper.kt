package com.example.modu.data.dataSource.remote.product.dto

data class ProductWrapper(
    val data: List<ProductDto>,
    val meta: MetaDataDto
)