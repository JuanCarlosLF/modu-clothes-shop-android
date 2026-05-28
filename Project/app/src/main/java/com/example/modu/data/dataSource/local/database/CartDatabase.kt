package com.example.modu.data.dataSource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.modu.data.dataSource.local.database.cart.Converters
import com.example.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo

@Database(
    entities = [CartItemDbo::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}