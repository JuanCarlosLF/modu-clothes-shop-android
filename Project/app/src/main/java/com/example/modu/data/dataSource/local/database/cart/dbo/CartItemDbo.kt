package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

private const val DEFAULT_CART_ID = 1

@Entity(tableName = "cart_items")
data class CartItemDbo(
    @PrimaryKey
    val id: Int,
    val cartId: Int = DEFAULT_CART_ID,
    val productId: Int,
    val productVariantId: Int,
    val currentStock: Int,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val oldPriceAlert: BigDecimal? = null,
    val stockAlertAvailable: Int? = null,
    val isAvailableAlert: Boolean? = null
)