package com.example.modu.data.dataSource.local.demo.database.cart.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartDbo(
    @PrimaryKey
    val id: Int,
    val createdAt: String,
    val updatedAt: String
)
