package com.example.modu.data.dataSource.remote.cart

import com.example.modu.data.dataSource.remote.cart.api.CartApi
import com.example.modu.data.dataSource.remote.cart.dto.AddItemRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartCheckoutRequestDto
import com.example.modu.data.dataSource.remote.cart.dto.CartCheckoutResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.CartDto
import com.example.modu.data.dataSource.remote.cart.dto.CartResponseDto
import com.example.modu.data.dataSource.remote.cart.dto.UpdateCartRequestDto
import javax.inject.Inject

class CartRemoteDataSourceImpl @Inject constructor(
    private val api: CartApi
) : CartRemoteDataSource {

    override suspend fun getCart(): CartDto = api.getCart()

    override suspend fun updateCart(request: UpdateCartRequestDto): CartDto = api.updateCart(request)

    override suspend fun addItem(request: AddItemRequestDto): CartResponseDto = api.addItem(request)

    override suspend fun deleteItemById(itemId: Int): CartResponseDto = api.deleteItemById(itemId)

    override suspend fun clearCart(): CartResponseDto = api.clearCart()

    override suspend fun createCart(): CartDto = api.createCart()

    override suspend fun executeCheckout(request: CartCheckoutRequestDto): CartCheckoutResponseDto = api.executeCheckout(request)
}