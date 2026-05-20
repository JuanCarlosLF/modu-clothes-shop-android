package com.example.modu.data.dataSource.remote

import androidx.paging.PagingSource
import com.example.modu.data.dataSource.ProductDataSource
import com.example.modu.data.dataSource.remote.product.ProductPagingSource
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(private val api: ProductApi) :
    ProductDataSource {

    override fun getProductsPagingSource(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): PagingSource<Int, ProductDto> {
        return ProductPagingSource(
            api = api,
            title = title,
            orderByPrice = orderByPrice,
            maxPrice = maxPrice,
            categories = categories
        )
    }

    override suspend fun getCategories(): List<CategoryDto> = api.getCategories()
    override suspend fun getDetailById(id: Int): DetailDto = api.getDetailById(id)
}