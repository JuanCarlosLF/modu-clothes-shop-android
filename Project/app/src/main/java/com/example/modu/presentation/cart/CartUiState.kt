package com.example.modu.presentation.cart

import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import java.math.BigDecimal

data class CartUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val subTotal: BigDecimal = BigDecimal.ZERO,
    val shippingCost: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val isCartEmpty: Boolean = false,
    val errorMessageRes: Int? = null,
    val showClearCartDialog: Boolean = false,
    val itemToUndo: CartItem? = null
)