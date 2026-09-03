package com.juancarloslf.modu.data.dataSource.local.demo.database.product

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juancarloslf.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoRelatedTest {

    private lateinit var database: ModuDemoDatabase
    private lateinit var productDao: ProductDao
    private lateinit var catalogFixture: CatalogFixture

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ModuDemoDatabase::class.java
        ).build()
        productDao = database.productDao()
        catalogFixture = CatalogFixture(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun getRelatedProducts_givenProductWithAllSelectedAndExtraCategory_whenCalled_thenIncludesProduct() =
        runBlocking {
            // GIVEN
            val matchingProduct = product(id = 3)
            val shirtOnlyProduct = product(id = 2)
            val summerOnlyProduct = product(id = 1)
            catalogFixture.insertProducts(
                listOf(matchingProduct, shirtOnlyProduct, summerOnlyProduct)
            )
            catalogFixture.insertCategories(
                listOf(
                    CategoryDbo(id = 1, name = "Shirt"),
                    CategoryDbo(id = 2, name = "Summer"),
                    CategoryDbo(id = 3, name = "Casual")
                )
            )
            catalogFixture.insertProductCategories(
                listOf(
                    ProductCategoryCrossRefDbo(matchingProduct.id, 1),
                    ProductCategoryCrossRefDbo(matchingProduct.id, 2),
                    ProductCategoryCrossRefDbo(matchingProduct.id, 3),
                    ProductCategoryCrossRefDbo(shirtOnlyProduct.id, 1),
                    ProductCategoryCrossRefDbo(summerOnlyProduct.id, 2)
                )
            )

            // WHEN
            val products = productDao.getRelatedProducts(
                categoryNames = listOf("shirt", "SUMMER"),
                categoryCount = 2
            )

            // THEN
            assertEquals(listOf(matchingProduct), products)
        }

    @Test
    fun getRelatedProducts_givenEmptyCategories_whenCalled_thenReturnsCategorizedAndUncategorizedActiveProducts() =
        runBlocking {
            // GIVEN
            val uncategorizedProduct = product(id = 1)
            val categorizedProduct = product(id = 2)
            catalogFixture.insertProducts(listOf(uncategorizedProduct, categorizedProduct))
            catalogFixture.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
            catalogFixture.insertProductCategories(
                listOf(ProductCategoryCrossRefDbo(categorizedProduct.id, 1))
            )

            // WHEN
            val products = productDao.getRelatedProducts(
                categoryNames = emptyList(),
                categoryCount = 0
            )

            // THEN
            assertEquals(listOf(categorizedProduct, uncategorizedProduct), products)
        }

    @Test
    fun getRelatedProducts_givenUnknownCategory_whenCalled_thenReturnsEmptyResult() = runBlocking {
        // GIVEN
        val categorizedProduct = product(id = 1)
        catalogFixture.insertProducts(listOf(categorizedProduct))
        catalogFixture.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
        catalogFixture.insertProductCategories(
            listOf(ProductCategoryCrossRefDbo(categorizedProduct.id, 1))
        )

        // WHEN
        val products = productDao.getRelatedProducts(
            categoryNames = listOf("Unknown"),
            categoryCount = 1
        )

        // THEN
        assertEquals(emptyList<ProductDbo>(), products)
    }

    @Test
    fun getRelatedProducts_givenMoreThanTwentyMatchesAndHigherIdInactive_whenCalled_thenReturnsLatestTwentyActiveProducts() =
        runBlocking {
            // GIVEN
            val activeProducts = (1..25).map(::product)
            val inactiveProduct = product(id = 26, active = false)
            catalogFixture.insertProducts(activeProducts + inactiveProduct)
            catalogFixture.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
            catalogFixture.insertProductCategories(
                (activeProducts + inactiveProduct).map { product ->
                    ProductCategoryCrossRefDbo(product.id, categoryId = 1)
                }
            )

            // WHEN
            val products = productDao.getRelatedProducts(
                categoryNames = listOf("SHIRT"),
                categoryCount = 1
            )

            // THEN
            assertEquals((25 downTo 6).toList(), products.map(ProductDbo::id))
        }

    private fun product(id: Int, active: Boolean = true) = ProductDbo(
        id = id,
        name = "Product $id",
        description = "",
        imageAssetPath = "",
        priceInCents = 1_000L,
        active = active
    )
}
