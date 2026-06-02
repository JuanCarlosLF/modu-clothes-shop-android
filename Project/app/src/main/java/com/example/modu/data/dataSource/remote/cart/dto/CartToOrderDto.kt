package com.example.modu.data.dataSource.remote.cart.dto

import com.google.gson.annotations.SerializedName

data class CartToOrderDto(
    @SerializedName("cart_items")
    val cartItems: List<CartItemDto>? = null
)