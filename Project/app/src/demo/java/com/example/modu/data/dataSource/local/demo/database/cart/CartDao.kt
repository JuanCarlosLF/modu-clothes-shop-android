package com.example.modu.data.dataSource.local.demo.database.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query(
        """
        SELECT
         ci.id AS id,
         p.id AS productId,
         pv.id AS productVariantId, 
         pv.stock AS currentStock,
         ci.quantity AS quantity,
         p.priceInCents AS unitPrice, 
         p.name AS title, 
         p.imageAssetPath AS imageUrl, 
         pv.size AS size, 
         pv.color AS color
        FROM cart_items AS ci
        INNER JOIN product_variants as pv ON ci.productVariantId = pv.id
            INNER JOIN products AS p ON p.id = pv.productId
        WHERE ci.cartId = :cartId
        """
    )
    fun observeCartItems(cartId: Int): Flow<List<CartItemDetailDbo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createCart(cart: CartDbo)
}