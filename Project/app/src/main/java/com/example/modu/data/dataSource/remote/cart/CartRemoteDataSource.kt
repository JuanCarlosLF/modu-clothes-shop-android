package com.example.modu.data.dataSource.remote.cart

import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto

interface CartRemoteDataSource {
    suspend fun getCart(): CartDto
    suspend fun updateCart(request: UpdateCartRequestDto): CartDto
    suspend fun addItem(request: AddItemRequestDto): CartResponseDto
    suspend fun deleteItemById(itemId: Int): CartResponseDto
    suspend fun clearCart(): CartResponseDto
    suspend fun createCart(): CartDto
}