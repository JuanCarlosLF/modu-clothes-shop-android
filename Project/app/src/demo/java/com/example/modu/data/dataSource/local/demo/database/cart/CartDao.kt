package com.example.modu.data.dataSource.local.demo.database.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createCart(cart: CartDbo)
}