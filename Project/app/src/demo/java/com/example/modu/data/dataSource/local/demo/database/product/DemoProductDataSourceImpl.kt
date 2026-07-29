package com.example.modu.data.dataSource.local.demo.database.product

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDetailDbo
import javax.inject.Inject

class DemoProductDataSourceImpl @Inject constructor(
    private val dao: ProductDao
) : DemoProductDataSource {

    override fun getProductsBy(
        title: String?,
        maxPriceInCents: Long?,
        orderByPrice: String?,
        categoryNames: List<String>,
        categoryCount: Int
    ): PagingSource<Int, ProductDbo> =
        dao.getProductsBy(
            title = title,
            maxPriceInCents = maxPriceInCents,
            orderByPrice = orderByPrice,
            categoryNames = categoryNames,
            categoryCount = categoryCount
        )

    override suspend fun getCategories(): List<CategoryDbo> = dao.getCategories()

    override suspend fun getDetailById(id: Int): ProductDetailDbo? =
        dao.getDetailById(id)

    override suspend fun getRelatedProducts(
        categories: List<String>,
        categoryCount: Int
    ): List<ProductDbo> = dao.getRelatedProducts(
        categoryNames = categories,
        categoryCount = categoryCount
    )
}
