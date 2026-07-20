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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogSeedDaoTest {

    private lateinit var database: ModuDemoDatabase
    private lateinit var catalogSeedDao: CatalogSeedDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ModuDemoDatabase::class.java
        ).build()
        catalogSeedDao = database.catalogSeedDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insertCatalog_givenAllCollections_whenCalled_thenInsertsCompleteCatalog() = runBlocking {
        // GIVEN
        val product = product(id = 1)
        val category = CategoryDbo(id = 2, name = "Shirts")
        val productCategory = ProductCategoryCrossRefDbo(productId = 1, categoryId = 2)
        val variant = variant(id = 3, productId = 1)

        // WHEN
        catalogSeedDao.insertCatalog(
            categories = listOf(category),
            products = listOf(product),
            productCategories = listOf(productCategory),
            productVariants = listOf(variant)
        )

        // THEN
        assertEquals(1, catalogSeedDao.countProducts())
        assertEquals(1L, countRows("categories"))
        assertEquals(1L, countRows("product_categories"))
        assertEquals(1L, countRows("product_variants"))
    }

    @Test
    fun insertCatalog_givenVariantWithMissingProduct_whenCalled_thenRollsBackEveryCollection() =
        runBlocking {
            // GIVEN
            val product = product(id = 1)
            val category = CategoryDbo(id = 2, name = "Shirts")
            val productCategory = ProductCategoryCrossRefDbo(productId = 1, categoryId = 2)
            val invalidVariant = variant(id = 3, productId = 999)

            // WHEN
            val thrownException = try {
                catalogSeedDao.insertCatalog(
                    categories = listOf(category),
                    products = listOf(product),
                    productCategories = listOf(productCategory),
                    productVariants = listOf(invalidVariant)
                )
                null
            } catch (exception: Exception) {
                exception
            }

            // THEN
            assertNotNull("Expected the invalid variant foreign key to fail", thrownException)
            assertTrue(
                "Expected SQLiteConstraintException in the cause chain",
                generateSequence<Throwable>(thrownException) { it.cause }
                    .any { it is SQLiteConstraintException }
            )
            assertEquals(0, catalogSeedDao.countProducts())
            assertEquals(0L, countRows("categories"))
            assertEquals(0L, countRows("product_categories"))
            assertEquals(0L, countRows("product_variants"))
        }

    private fun countRows(table: String): Long =
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }

    private fun product(id: Int) = ProductDbo(
        id = id,
        name = "Product $id",
        description = "",
        imageAssetPath = "",
        priceInCents = 1_000L,
        active = true
    )

    private fun variant(id: Int, productId: Int) = ProductVariantDbo(
        id = id,
        variantCode = "variant-$id",
        color = "Blue",
        size = "M",
        productId = productId,
        stock = 1,
        active = true
    )
}
