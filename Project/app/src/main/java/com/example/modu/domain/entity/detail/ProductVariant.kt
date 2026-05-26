package com.example.modu.domain.entity.detail

data class ProductVariant(
    val id: Int,
    val name: String,
    val size: String,
    val color: String,
    val stock: Int,
    val active: Boolean,
    val productId: Int
)