package com.example.modu.data.dataSource.remote.cart

import com.example.modu.data.dataSource.remote.cart.api.CartApi
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import jakarta.inject.Inject

class CartRemoteDataSourceImpl @Inject constructor(
    private val api: CartApi
) : CartRemoteDataSource {

    override suspend fun getCart(deviceId: String): CartDto = api.getCart(deviceId)

    override suspend fun updateCart(request: UpdateCartRequestDto): CartDto = api.updateCart(request)

    override suspend fun addItem(request: AddItemRequestDto): CartDto = api.addItem(request)

    override suspend fun deleteItemById(itemId: Int, deviceId: String): CartDto = api.deleteItemById(itemId, deviceId)

    override suspend fun deleteAllItems(deviceId: String): CartDto = api.deleteAllItems(deviceId)
}