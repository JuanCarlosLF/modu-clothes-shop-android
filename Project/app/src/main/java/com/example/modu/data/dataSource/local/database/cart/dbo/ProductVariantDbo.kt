package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_variants")
data class ProductVariantDbo(
    @PrimaryKey val id: Int,
    val productId: Int,
    val size: String,
    val color: String
)