package com.example.modu.data.dataSource.remote.cart.api

import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CartApi {

    @GET("cart")
    suspend fun getCart(): CartDto

    @PUT("cart")
    suspend fun createNewCart(): CartDto

    @PATCH("cart/update")
    suspend fun updateCart(
        @Body request: UpdateCartRequestDto
    ): CartDto

    @POST("cart/addItem")
    suspend fun addItem(
        @Body request: AddItemRequestDto
    ): CartDto

    @DELETE("cart/items/{itemId}")
    suspend fun deleteItemById(
        @Path("itemId") itemId: Int,
        @Query("device_id") deviceId: String
    ): CartDto

    @DELETE("cart/items")
    suspend fun clearCart(
        @Query("device_id") deviceId: String
    ): CartDto
}