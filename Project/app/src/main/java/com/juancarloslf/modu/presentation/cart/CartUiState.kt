package com.juancarloslf.modu.presentation.cart

import com.juancarloslf.modu.domain.entity.cart.CartItem
import com.juancarloslf.modu.domain.exception.AppError
import java.math.BigDecimal

data class CartUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val subTotal: BigDecimal = BigDecimal.ZERO,
    val shippingCost: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val isCartEmpty: Boolean = false,
    val error: AppError? = null,
    val itemToUndo: CartItem? = null
)