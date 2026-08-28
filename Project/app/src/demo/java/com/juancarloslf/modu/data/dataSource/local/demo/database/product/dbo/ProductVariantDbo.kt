package com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_variants",
    foreignKeys = [ForeignKey(
        entity = ProductDbo::class,
        parentColumns = ["id"],
        childColumns = ["productId"]
    )],
    indices = [Index(value = ["productId"]), Index(value = ["variantCode"], unique = true)]
)
data class ProductVariantDbo(
    @PrimaryKey
    val id: Int,
    val variantCode: String,
    val color: String,
    val size: String,
    val productId: Int,
    val stock: Int,
    val active: Boolean
)