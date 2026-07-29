package com.example.modu.data.dataSource.local.demo.database.product

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDetailDbo

interface DemoProductDataSource {
    fun getProductsBy(
        title: String?,
        maxPriceInCents: Long?,
        orderByPrice: String?,
        categoryNames: List<String>,
        categoryCount: Int
    ): PagingSource<Int, ProductDbo>

    suspend fun getCategories(): List<CategoryDbo>
    suspend fun getDetailById(id: Int): ProductDetailDbo?
    suspend fun getRelatedProducts(categories: List<String>, categoryCount: Int): List<ProductDbo>
}
