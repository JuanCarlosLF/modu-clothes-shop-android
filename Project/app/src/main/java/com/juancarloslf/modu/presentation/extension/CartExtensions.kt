package com.juancarloslf.modu.presentation.extension

import com.juancarloslf.modu.domain.entity.cart.CartItem
import java.math.BigDecimal

fun List<CartItem>.calculateSubTotal(): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }
}