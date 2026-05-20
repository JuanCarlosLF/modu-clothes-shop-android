package com.example.modu.data.dataSource.remote.product

import com.example.modu.data.repository.product.toDomain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.modu.data.dataSource.remote.product.api.ProductApi
import com.example.modu.domain.entity.product.Product

class ProductPagingSource(
    private val api: ProductApi,
    private val title: String?,
    private val orderByPrice: String?,
    private val maxPrice: Int?,
    private val categories: List<String>?
) : PagingSource<Int, Product>() {

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            val currentPage = params.key ?: 1

            val response = api.getProductsBy(
                page = currentPage,
                size = params.loadSize,
                title = title,
                orderByPrice = orderByPrice,
                maxPrice = maxPrice,
                categories = categories
            )

            val products = response.products.map { it.toDomain() }

            LoadResult.Page(
                data = products,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (response.meta.hasNext == true) currentPage + 1 else null
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}