package com.example.modu.data.dataSource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.modu.data.dataSource.local.database.cart.Converters
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo

@Database(
    entities = [
        CartDbo::class,
        CartItemDbo::class,
        CartItemDetailDbo::class,
        ProductVariantDbo::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}