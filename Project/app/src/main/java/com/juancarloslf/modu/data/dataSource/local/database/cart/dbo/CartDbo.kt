package com.juancarloslf.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

private const val DEFAULT_CART_ID = 1

@Entity(tableName = "cart")
data class CartDbo(
    @PrimaryKey
    val id: Int = DEFAULT_CART_ID,
    val subTotal: BigDecimal,
    val shippingCost: BigDecimal,
    val total: BigDecimal,
    val createdAt: String,
    val updatedAt: String
)