package com.example.modu.presentation.extension

import com.example.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

fun List<CartItem>.calculateSubTotal(): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }
}