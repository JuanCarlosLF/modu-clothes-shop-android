package com.example.modu.data.dataSource.local.demo.database.cart.dbo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.NO_ACTION
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo

@Entity(
    tableName = "cart_items", indices = [
        Index(value = ["cartId", "productVariantId"], unique = true),
        Index(value = ["productVariantId"])
    ], foreignKeys = [
        ForeignKey(
            entity = CartDbo::class,
            parentColumns = ["id"],
            childColumns = ["cartId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = ProductVariantDbo::class,
            parentColumns = ["id"],
            childColumns = ["productVariantId"],
            onDelete = NO_ACTION
        )
    ]
)
data class CartItemDbo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cartId: Int,
    val productVariantId: Int,
    val quantity: Int
)
