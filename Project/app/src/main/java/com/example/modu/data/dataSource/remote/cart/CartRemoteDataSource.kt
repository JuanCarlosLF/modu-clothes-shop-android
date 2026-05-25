package com.example.modu.data.dataSource.remote.cart

import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto

interface CartRemoteDataSource {
    suspend fun getCart(deviceId: String): CartDto
    suspend fun updateCart(request: UpdateCartRequestDto): CartDto
    suspend fun addItem(request: AddItemRequestDto): CartDto
    suspend fun deleteItemById(itemId: Int, deviceId: String): CartDto
    suspend fun deleteAllItems(deviceId: String): CartDto
}