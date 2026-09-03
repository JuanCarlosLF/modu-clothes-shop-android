package com.juancarloslf.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Embedded
import androidx.room.Relation

data class CartItemWithDetailsDbo(
    @Embedded val cartItem: CartItemDbo,

    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val productDetail: List<CartItemDetailDbo>,

    @Relation(
        parentColumn = "productVariantId",
        entityColumn = "id"
    )
    val productVariant: List<ProductVariantDbo>
)