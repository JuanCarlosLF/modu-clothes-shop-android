package com.juancarloslf.modu.data.dataSource.local.demo.database.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
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

    @Query("SELECT * FROM cart WHERE id = :cartId LIMIT 1")
    suspend fun getCart(cartId: Int): CartDbo?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createCart(cart: CartDbo)

    @Insert
    suspend fun addItem(cartItem: CartItemDbo)

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId AND productVariantId = :variantId")
    suspend fun getCartItem(variantId: Int, cartId: Int): CartItemDbo?

    @Query("""UPDATE cart_items SET quantity = :quantity WHERE id = :itemId""")
    suspend fun updateQuantity(itemId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Int)

    @Query("DELETE FROM cart_items WHERE cartId = :cartId")
    suspend fun clearCart(cartId: Int)
}
