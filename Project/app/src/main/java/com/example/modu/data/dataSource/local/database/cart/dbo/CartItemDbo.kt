package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemDbo(
    @PrimaryKey
    val id: Int,
    val productVariantId: Int,
    val currentStock: Int,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)