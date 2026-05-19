package com.example.modu.data.dataSource.remote.product.api

import com.example.modu.data.dataSource.remote.product.dto.ProductWrapperDto
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProductsBy(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("title") title: String? = null,
        @Query("orderByPrice") orderByPrice: String? = null,
        @Query("maxPrice") maxPrice: Int? = null
    ): ProductWrapperDto

    @GET("categories")
    suspend fun getCategories() : List<CategoryDto>
}