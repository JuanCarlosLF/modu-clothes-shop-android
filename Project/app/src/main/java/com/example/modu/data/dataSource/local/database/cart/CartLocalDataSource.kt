package com.example.modu.data.dataSource.local.database.cart

import com.example.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import kotlinx.coroutines.flow.Flow

interface CartLocalDataSource {
    fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?>
    suspend fun getCartWithItems(): CartWithItemsDbo?
    suspend fun insertCart(cart: CartDbo)
    suspend fun insertCartItems(items: List<CartItemDbo>)
    suspend fun deleteCartItem(itemId: Int)
    suspend fun clearCart()
    suspend fun clearEntireCart()
    suspend fun insertProductDetails(details: List<CartItemDetailDbo>)
    suspend fun insertProductVariants(variants: List<ProductVariantDbo>)
    suspend fun hasProductDetail(productId: Int): Boolean
}