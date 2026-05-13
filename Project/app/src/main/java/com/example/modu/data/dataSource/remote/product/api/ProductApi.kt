package com.example.modu.data.dataSource.remote.product.api

import com.example.modu.data.dataSource.remote.product.dto.ProductWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("title") title: String? = null,
        @Query("orderByPrice") orderByPrice: String? = null,
        @Query("maxPrice") maxPrice: Int? = null
    ): ProductWrapper
}