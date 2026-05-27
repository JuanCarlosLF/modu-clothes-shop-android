package com.example.modu.data.dataSource.remote.cart.dto

data class InsufficientStockAlertDto(
    val cartItems: List<StockAlertItemDto>? = null
)