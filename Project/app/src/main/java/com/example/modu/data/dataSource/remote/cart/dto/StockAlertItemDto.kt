package com.example.modu.data.dataSource.remote.cart.dto

data class StockAlertItemDto(
    val productVariantId: Int? = null,
    val requestedQuantity: Int? = null,
    val availableStock: Int? = null
)