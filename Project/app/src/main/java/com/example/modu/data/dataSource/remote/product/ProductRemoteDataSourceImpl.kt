package com.example.modu.data.dataSource.remote.product

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val api: ProductApi
) : ProductDataSource {

    override fun getPaginatedProducts(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?,
        mapException: (Exception) -> Exception
    ): PagingSource<Int, ProductDto> {
        return ProductPagingSource(
            api = api,
            title = title,
            orderByPrice = orderByPrice,
            maxPrice = maxPrice,
            categories = categories,
            mapException = mapException
        )
    }

    override suspend fun getCategories(): List<CategoryDto> = api.getCategories()
    override suspend fun getDetailById(id: Int): DetailDto = api.getDetailById(id)
}