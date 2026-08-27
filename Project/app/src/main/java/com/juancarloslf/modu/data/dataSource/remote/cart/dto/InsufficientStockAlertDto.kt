package com.juancarloslf.modu.data.dataSource.remote.cart.dto

data class InsufficientStockAlertDto(
    val cartItems: List<StockAlertItemDto>? = null
)