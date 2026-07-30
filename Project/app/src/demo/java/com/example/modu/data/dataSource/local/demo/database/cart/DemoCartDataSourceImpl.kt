package com.example.modu.data.dataSource.local.demo.database.cart

import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import javax.inject.Inject

class DemoCartDataSourceImpl @Inject constructor(private val dao: CartDao) :
    DemoCartDataSource {

    override suspend fun createCart(cart: CartDbo) =
        dao.createCart(cart = cart)
}