package com.example.modu.data.dataSource.remote.product.api

import com.example.modu.data.dataSource.remote.product.dto.ProductWrapperDto
import com.example.modu.data.dataSource.remote.product.dto.detail.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProductsBy(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("title") title: String? = null,
        @Query("orderByPrice") orderByPrice: String? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("categories") categories: List<String>? = null
    ): ProductWrapperDto

    @GET("product/{id}")
    suspend fun getDetailById(@Path("id") id: Int): DetailDto

    @GET("categories")
    suspend fun getCategories() : List<CategoryDto>
}