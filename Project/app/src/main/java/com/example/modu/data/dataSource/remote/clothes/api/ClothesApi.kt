package com.example.modu.data.dataSource.remote.clothes.api

import com.example.modu.data.dataSource.remote.clothes.dto.ClothesItemDto
import com.example.modu.data.dataSource.remote.clothes.dto.ClothesWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface ClothesApi {

    @GET("clothes")
    suspend fun getClothes(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("title") title: String? = null,
        @Query("orderByPrice") orderByPrice: String? = null,
        @Query("maxPrice") maxPrice: Int? = null
    ): ClothesWrapper
}