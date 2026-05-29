package com.example.modu.domain.entity.cart

import java.math.BigDecimal

data class CartItem(
    val id: Int = 0,
    val productId: Int,
    val productVariantId: Int,
    val currentStock: Int,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val title: String,
    val imageUrl: String,
    val size: String,
    val color: String,
    val oldPriceAlert: BigDecimal? = null,
    val stockAlertAvailable: Int? = null,
    val isAvailableAlert: Boolean? = null
)