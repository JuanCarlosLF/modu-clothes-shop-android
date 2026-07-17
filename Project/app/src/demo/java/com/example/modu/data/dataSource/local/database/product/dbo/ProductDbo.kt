package com.example.modu.data.dataSource.local.database.product.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductDbo(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val imageAssetPath: String,
    val priceInCents: Long,
    val active: Boolean
)