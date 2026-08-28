package com.juancarloslf.modu.data.dataSource.local.database.cart

import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartDao
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.CartWithItemsDbo
import com.juancarloslf.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val cartDao: CartDao
) : CartLocalDataSource {

    override fun getCartWithItemsFlow(): Flow<CartWithItemsDbo?> = cartDao.getCartWithItemsFlow()

    override suspend fun getCartWithItems(): CartWithItemsDbo? = cartDao.getCartWithItems()

    override suspend fun insertCart(cart: CartDbo) = cartDao.insertCart(cart)

    override suspend fun insertCartItems(items: List<CartItemDbo>) = cartDao.insertItems(items)

    override suspend fun deleteCartItem(itemId: Int) = cartDao.deleteItem(itemId)

    override suspend fun clearCart() = cartDao.clearCart()

    override suspend fun insertProductDetails(details: List<CartItemDetailDbo>) = cartDao.insertProductDetails(details)

    override suspend fun insertProductVariants(variants: List<ProductVariantDbo>) = cartDao.insertProductVariants(variants)

    override suspend fun hasProductDetail(productId: Int): Boolean = cartDao.hasProductDetail(productId)

    override suspend fun deleteVariantsByProductId(productId: Int) = cartDao.deleteVariantsByProductId(productId)

    override suspend fun getExistingProductDetails(productIds: List<Int>): List<Int> = cartDao.getExistingProductDetails(productIds)
}