package com.example.modu.data.dataSource.local.database.cart.dbo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

private const val DEFAULT_CART_ID = 1

@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart WHERE id = $DEFAULT_CART_ID")
    fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?>

    @Transaction
    @Query("SELECT * FROM cart WHERE id = $DEFAULT_CART_ID")
    suspend fun getCartWithItems(): CartWithItemsDbo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: CartDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CartItemDbo>)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCartItems()

    @Query("DELETE FROM cart")
    suspend fun clearCartSummary()

    @Transaction
    suspend fun clearCart() {
        clearCartItems()
        clearCartSummary()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductDetails(details: List<CartItemDetailDbo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductVariants(variants: List<ProductVariantDbo>)

    @Query("SELECT EXISTS(SELECT 1 FROM product_details WHERE id = :productId)")
    suspend fun hasProductDetail(productId: Int): Boolean
}