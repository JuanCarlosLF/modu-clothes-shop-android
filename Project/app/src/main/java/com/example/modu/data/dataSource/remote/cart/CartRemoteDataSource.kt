package com.example.modu.data.dataSource.remote.cart

import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import com.example.modu.domain.entity.cart.Cart

interface CartRemoteDataSource {
    suspend fun getCart(): CartDto
    suspend fun updateCart(request: UpdateCartRequestDto): CartDto
    suspend fun addItem(request: AddItemRequestDto): CartDto
    suspend fun deleteItemById(itemId: Int): CartDto
    suspend fun clearCart(): CartDto
    suspend fun createCart() : CartDto
}