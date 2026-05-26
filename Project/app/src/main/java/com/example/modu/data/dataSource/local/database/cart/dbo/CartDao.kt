package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItemDbo>>

    @Query("SELECT * FROM cart_items")
    suspend fun getCartItems(): List<CartItemDbo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CartItemDbo>)

    @Query("UPDATE cart_items SET quantity = :quantity, totalPrice = :totalPrice WHERE id = :itemId")
    suspend fun updateQuantityAndPrice(itemId: Int, quantity: Int, totalPrice: Long)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
