package com.example.modu.data.dataSource.local.demo.database.product.dbo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ProductDetailDbo(
    @Embedded
    val product: ProductDbo,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductCategoryCrossRefDbo::class,
            parentColumn = "productId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<CategoryDbo>,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val variants: List<ProductVariantDbo>
)
