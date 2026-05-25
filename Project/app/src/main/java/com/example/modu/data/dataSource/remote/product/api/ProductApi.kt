package com.example.modu.data.dataSource.remote.product.api

import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductWrapperDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProductsBy(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("title") title: String?,
        @Query("orderByPrice") orderByPrice: String?,
        @Query("maxPrice") maxPrice: Int?,
        @Query("category") categories: List<String>?
    ): ProductWrapperDto

    @GET("product/{id}")
    suspend fun getDetailById(@Path("id") id: Int): DetailDto

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>
}