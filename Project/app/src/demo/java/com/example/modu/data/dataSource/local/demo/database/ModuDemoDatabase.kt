package com.example.modu.data.dataSource.local.demo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo

@Database(
    entities = [
        ProductDbo::class,
        CategoryDbo::class,
        ProductVariantDbo::class,
        ProductCategoryCrossRefDbo::class,
        CartDbo::class,
        CartItemDbo::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ModuDemoDatabase : RoomDatabase()
