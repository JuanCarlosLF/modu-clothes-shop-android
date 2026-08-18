package com.example.modu.data.dataSource.local.demo.database.cart

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.example.modu.data.dataSource.local.demo.database.cart.dbo.CartItemDetailDbo
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartDaoTest {

    private lateinit var database: ModuDemoDatabase
    private lateinit var cartDao: CartDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ModuDemoDatabase::class.java
        ).build()

        cartDao = database.cartDao()
        chargeDatabase()
    }

    @Test
    fun observeCartItems_GivenCartId_WhenCalled_ThenReturnsCartItemDetailDbo() = runBlocking {
        // WHEN
        val cartItem = cartDao.observeCartItems(1).first().first()


        // THEN
        val expectedCartItem = CartItemDetailDbo(
            id = 1,
            productId = 1,
            productVariantId = 4,
            currentStock = 14,
            quantity = 5,
            unitPrice = 4000,
            title = "Example Product",
            imageUrl = "Example img path",
            size = "XL",
            color = "BLACK"
        )
        assertEquals(expectedCartItem, cartItem)
    }

    @After
    fun tearDown() = database.close()

    private fun chargeDatabase() {
        database.executeTransaction {
            execSQL(
                "INSERT INTO products (id, name, description, imageAssetPath, priceInCents, active) VALUES(?, ?, ?, ?, ?, ?)",
                bindArgs = arrayOf<Any>(
                    1,
                    "Example Product",
                    "This is de example product",
                    "Example img path",
                    4000,
                    1
                )
            )

            execSQL(
                "INSERT INTO categories (id, name) VALUES (?, ?)",
                bindArgs = arrayOf<Any>(1, "Example Category")
            )

            execSQL(
                "INSERT INTO product_categories (productId, categoryId) VALUES (?, ?)",
                arrayOf<Any>(1, 1)
            )

            execSQL(
                "INSERT INTO product_variants (id, variantCode, color, size, productId, stock, active) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(4, "EXAMPLE_CODE", "BLACK", "XL", 1, 14, 1)
            )

            execSQL(
                "INSERT INTO cart (id, createdAt, updatedAt) VALUES (?, ?, ?)",
                arrayOf<Any>(1, "", "")
            )

            execSQL(
                "INSERT INTO cart_items (id, cartId, productVariantId, quantity) VALUES (?, ?, ?, ?)",
                arrayOf(1, 1, 4, 5)
            )
        }
    }

    private inline fun RoomDatabase.executeTransaction(block: SupportSQLiteDatabase.() -> Unit) {
        with(openHelper.writableDatabase) {
            beginTransaction()
            try {
                block()
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }
}
