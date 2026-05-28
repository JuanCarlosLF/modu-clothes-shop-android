package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "cart_items")
data class CartItemDbo(
    @PrimaryKey
    val id: Int,
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