package com.juancarloslf.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Embedded
import androidx.room.Relation

data class CartWithItemsDbo(
    @Embedded val cart: CartDbo,

    @Relation(
        entity = CartItemDbo::class,
        parentColumn = "id",
        entityColumn = "cartId"
    )
    val items: List<CartItemWithDetailsDbo>
)