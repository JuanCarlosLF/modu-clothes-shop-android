package com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo

data class CartItemDetailDbo(
    val id: Int,
    val productId: Int,
    val productVariantId: Int,
    val currentStock: Int,
    val quantity: Int,
    val unitPrice: Long,
    val title: String,
    val imageUrl: String,
    val size: String,
    val color: String,
)