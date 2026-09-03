package com.juancarloslf.modu.data.dataSource.local.database.cart

import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import kotlinx.coroutines.flow.Flow

interface CartLocalDataSource {
    fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?>
    suspend fun getCartWithItems(): CartWithItemsDbo?
    suspend fun insertCart(cart: CartDbo)
    suspend fun insertCartItems(items: List<CartItemDbo>)
    suspend fun deleteCartItem(itemId: Int)
    suspend fun clearCart()
    suspend fun insertProductDetails(details: List<CartItemDetailDbo>)
    suspend fun insertProductVariants(variants: List<ProductVariantDbo>)
    suspend fun hasProductDetail(productId: Int): Boolean
    suspend fun deleteVariantsByProductId(productId: Int)
    suspend fun getExistingProductDetails(productIds: List<Int>): List<Int>
}