package com.juancarloslf.modu.data.dataSource.local.demo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.CartDao
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.ProductDao
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo

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
abstract class ModuDemoDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
