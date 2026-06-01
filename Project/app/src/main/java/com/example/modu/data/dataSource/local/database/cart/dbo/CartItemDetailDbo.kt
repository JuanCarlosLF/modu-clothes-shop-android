package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_details")
data class CartItemDetailDbo(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String
)