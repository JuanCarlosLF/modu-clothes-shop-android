package com.example.modu.data.dataSource.local.database.product.dbo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product_categories",
    primaryKeys = [
        "productId",
        "categoryId"
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductDbo::class,
            parentColumns = ["id"],
            childColumns = ["productId"]
        ),
        ForeignKey(
            entity = CategoryDbo::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class ProductCategoryCrossRefDbo(
    val productId: Int,
    val categoryId: Int
)