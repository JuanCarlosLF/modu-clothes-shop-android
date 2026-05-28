package com.example.modu.domain.entity.cart

data class VariantAvailabilityItem(
    val cartItemId: Int,
    val productVariantId: Int,
    val isVariantAvailable: Boolean
)