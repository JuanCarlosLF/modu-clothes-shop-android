package com.example.modu.data.dataSource.local.demo.database.cart

import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo

interface DemoCartDataSource {
    suspend fun createCart(cart: CartDbo)
}