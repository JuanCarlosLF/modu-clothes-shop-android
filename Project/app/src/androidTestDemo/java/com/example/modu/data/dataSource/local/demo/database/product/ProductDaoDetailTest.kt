package com.example.modu.data.dataSource.local.demo.database.product

import android.content.Context
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoDetailTest {

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
    fun getDetailById_givenNonexistentProductId_whenCalled_thenReturnsNull() = runBlocking {
        assertNull(productDao.getDetailById(productId = 999))
    }

    @Test
    fun getDetailById_givenProductWithoutRelations_whenCalled_thenReturnsProductWithEmptyRelations() =
        runBlocking {
            // GIVEN
            val product = product(id = 1)
            catalogFixture.insertProducts(listOf(product))

            // WHEN
            val detail = requireNotNull(productDao.getDetailById(productId = product.id))

            // THEN
            assertEquals(product, detail.product)
            assertEquals(emptyList<CategoryDbo>(), detail.categories)
            assertEquals(emptyList<ProductVariantDbo>(), detail.variants)
        }

    @Test
    fun getDetailById_givenExistingProduct_whenCalled_thenReturnsOnlyItsRelations() = runBlocking {
        // GIVEN
        val requestedProduct = product(id = 1)
        val otherProduct = product(id = 2)
        val requestedCategories = listOf(
            CategoryDbo(id = 10, name = "Shirts"),
            CategoryDbo(id = 11, name = "Summer")
        )
        val otherCategory = CategoryDbo(id = 12, name = "Shoes")
        val requestedVariants = listOf(
            variant(id = 20, productId = requestedProduct.id),
            variant(id = 21, productId = requestedProduct.id)
        )
        val otherVariant = variant(id = 22, productId = otherProduct.id)
        catalogFixture.insertCatalog(
            categories = requestedCategories + otherCategory,
            products = listOf(requestedProduct, otherProduct),
            productCategories = listOf(
                ProductCategoryCrossRefDbo(requestedProduct.id, requestedCategories[0].id),
                ProductCategoryCrossRefDbo(requestedProduct.id, requestedCategories[1].id),
                ProductCategoryCrossRefDbo(otherProduct.id, otherCategory.id)
            ),
            productVariants = requestedVariants + otherVariant
        )

        // WHEN
        val detail = requireNotNull(productDao.getDetailById(productId = requestedProduct.id))

        // THEN
        assertEquals(requestedProduct, detail.product)
        assertEquals(requestedCategories.toSet(), detail.categories.toSet())
        assertEquals(requestedVariants.toSet(), detail.variants.toSet())
    }

    private fun product(id: Int) = ProductDbo(
        id = id,
        name = "Product $id",
        description = "Description $id",
        imageAssetPath = "product-$id.jpg",
        priceInCents = id * 1_000L,
        active = true
    )

    private fun variant(id: Int, productId: Int) = ProductVariantDbo(
        id = id,
        variantCode = "variant-$id",
        color = "Blue",
        size = "M",
        productId = productId,
        stock = id,
        active = true
    )
}
