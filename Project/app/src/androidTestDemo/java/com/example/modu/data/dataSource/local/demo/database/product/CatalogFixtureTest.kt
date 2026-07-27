package com.example.modu.data.dataSource.local.demo.database.product

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogFixtureTest {

    private lateinit var database: ModuDemoDatabase
    private lateinit var fixture: CatalogFixture

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ModuDemoDatabase::class.java).build()
        fixture = CatalogFixture(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insertCatalog_givenInvalidVariantForeignKey_thenRollsBackAllCatalogTables() {
        val product = ProductDbo(1, "Product 1", "Description 1", "product-1.jpg", 1_000L, true)
        val category = CategoryDbo(id = 1, name = "Shirts")
        val relation = ProductCategoryCrossRefDbo(productId = product.id, categoryId = category.id)
        val invalidVariant = ProductVariantDbo(1, "variant-1", "Blue", "M", 999, 1, true)

        assertThrows(SQLiteConstraintException::class.java) {
            fixture.insertCatalog(
                categories = listOf(category),
                products = listOf(product),
                productCategories = listOf(relation),
                productVariants = listOf(invalidVariant)
            )
        }

        assertEquals(0L, countRows("products"))
        assertEquals(0L, countRows("categories"))
        assertEquals(0L, countRows("product_categories"))
        assertEquals(0L, countRows("product_variants"))
    }

    private fun countRows(table: String): Long =
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
}
