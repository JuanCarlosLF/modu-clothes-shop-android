package com.example.modu.data.dataSource.local.demo.database.product

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: ModuDemoDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ModuDemoDatabase::class.java
        ).build()

        productDao = database.productDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun getProductsBy_givenActiveAndInactiveProducts_whenCalled_thenReturnsOnlyActiveProducts() = runBlocking {
        // GIVEN
        val activeProduct = getProduct(id = 1, name = "active", active = true)
        val inactiveProduct = getProduct(id = 2, name = "inactive", active = false)
        productDao.insertProducts(listOf(activeProduct, inactiveProduct))

        // WHEN
        val products = loadProducts(title = null)

        // THEN
        assertEquals(
            listOf(activeProduct),
            products
        )
    }

    @Test
    fun getProductsBy_givenPartialTitleWithDifferentCase_whenCalled_thenReturnsMatchingProducts() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Blue Cotton Shirt")
            val nonMatchingProduct = getProduct(id = 2, name = "Red Running Shoes")
            productDao.insertProducts(listOf(matchingProduct, nonMatchingProduct))

            // WHEN
            val products = loadProducts(title = "cotton")

            // THEN
            assertEquals(
                listOf(matchingProduct),
                products
            )
        }

    @Test
    fun getProductsBy_givenPercentInTitle_whenCalled_thenTreatsPercentLiterally() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Save 50% Today")
            val unrelatedProduct = getProduct(id = 2, name = "Full Price Product")
            productDao.insertProducts(listOf(matchingProduct, unrelatedProduct))

            // WHEN
            val products = loadProducts(title = "%")

            // THEN
            assertEquals(listOf(matchingProduct), products)
        }

    @Test
    fun getProductsBy_givenMaximumPrice_whenCalled_thenReturnsProductsAtOrBelowPrice() =
        runBlocking {
            // GIVEN
            val cheapProduct = getProduct(id = 1, priceInCents = 1_000L)
            val boundaryProduct = getProduct(id = 2, priceInCents = 2_000L)
            val expensiveProduct = getProduct(id = 3, priceInCents = 2_001L)
            productDao.insertProducts(
                listOf(cheapProduct, boundaryProduct, expensiveProduct)
            )

            // WHEN
            val products = loadProducts(maxPriceInCents = 2_000L)

            // THEN
            assertEquals(
                listOf(cheapProduct, boundaryProduct),
                products
            )
        }

    @Test
    fun getProductsBy_givenAscendingPriceOrder_whenCalled_thenReturnsProductsFromLowestToHighestPrice() =
        runBlocking {
            // GIVEN
            val expensiveProduct = getProduct(id = 1, priceInCents = 3_000L)
            val cheapProduct = getProduct(id = 2, priceInCents = 1_000L)
            val mediumProduct = getProduct(id = 3, priceInCents = 2_000L)
            productDao.insertProducts(
                listOf(expensiveProduct, cheapProduct, mediumProduct)
            )

            // WHEN
            val products = loadProducts(orderByPrice = "asc")

            // THEN
            assertEquals(
                listOf(cheapProduct, mediumProduct, expensiveProduct),
                products
            )
        }

    @Test
    fun getProductsBy_givenDescendingPriceOrder_whenCalled_thenReturnsProductsFromHighestToLowestPrice() =
        runBlocking {
            // GIVEN
            val expensiveProduct = getProduct(id = 1, priceInCents = 3_000L)
            val cheapProduct = getProduct(id = 2, priceInCents = 1_000L)
            val mediumProduct = getProduct(id = 3, priceInCents = 2_000L)
            productDao.insertProducts(
                listOf(expensiveProduct, cheapProduct, mediumProduct)
            )

            // WHEN
            val products = loadProducts(orderByPrice = "desc")

            // THEN
            assertEquals(
                listOf(expensiveProduct, mediumProduct, cheapProduct),
                products
            )
        }

    @Test
    fun getProductsBy_givenProductWithAllSelectedAndExtraCategory_whenCalled_thenIncludesProduct() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Linen Shirt")
            val shirtOnlyProduct = getProduct(id = 2, name = "Cotton Shirt")
            val summerOnlyProduct = getProduct(id = 3, name = "Summer Shorts")
            productDao.insertProducts(
                listOf(matchingProduct, shirtOnlyProduct, summerOnlyProduct)
            )
            productDao.insertCategories(
                listOf(
                    CategoryDbo(id = 1, name = "Shirt"),
                    CategoryDbo(id = 2, name = "Summer"),
                    CategoryDbo(id = 3, name = "Casual")
                )
            )
            productDao.insertProductCategories(
                listOf(
                    ProductCategoryCrossRefDbo(productId = matchingProduct.id, categoryId = 1),
                    ProductCategoryCrossRefDbo(productId = matchingProduct.id, categoryId = 2),
                    ProductCategoryCrossRefDbo(productId = matchingProduct.id, categoryId = 3),
                    ProductCategoryCrossRefDbo(productId = shirtOnlyProduct.id, categoryId = 1),
                    ProductCategoryCrossRefDbo(productId = summerOnlyProduct.id, categoryId = 2)
                )
            )

            // WHEN
            val products = loadProducts(categoryNames = listOf("Shirt", "Summer"))

            // THEN
            assertEquals(listOf(matchingProduct), products)
        }

    @Test
    fun getProductsBy_givenEmptyCategories_whenCalled_thenReturnsCategorizedAndUncategorizedActiveProducts() =
        runBlocking {
            // GIVEN
            val productWithoutCategories = getProduct(id = 1, name = "Uncategorized")
            val categorizedProduct = getProduct(id = 2, name = "Categorized")
            productDao.insertProducts(listOf(productWithoutCategories, categorizedProduct))
            productDao.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
            productDao.insertProductCategories(
                listOf(ProductCategoryCrossRefDbo(productId = categorizedProduct.id, categoryId = 1))
            )

            // WHEN
            val products = loadProducts(categoryNames = emptyList())

            // THEN
            assertEquals(listOf(productWithoutCategories, categorizedProduct), products)
        }

    private suspend fun loadProducts(
        title: String? = null,
        maxPriceInCents: Long? = null,
        orderByPrice: String? = null,
        categoryNames: List<String> = emptyList()
    ): List<ProductDbo> {
        val pagingSource = productDao.getProductsBy(
            title = title,
            maxPriceInCents = maxPriceInCents,
            orderByPrice = orderByPrice,
            categoryNames = categoryNames,
            categoryCount = categoryNames.size
        )
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        return when (result) {
            is PagingSource.LoadResult.Page -> result.data

            is PagingSource.LoadResult.Error ->
                throw AssertionError(
                    "Expected a page but loading failed",
                    result.throwable
                )

            is PagingSource.LoadResult.Invalid ->
                throw AssertionError("Expected a page but PagingSource was invalid")
        }
    }

    private fun getProduct(
        id: Int = 1,
        name: String = "",
        description: String = "",
        imageAssetPath: String = "",
        priceInCents: Long = 0L,
        active: Boolean = true
    ): ProductDbo =
        ProductDbo(
            id = id,
            name = name,
            description = description,
            imageAssetPath = imageAssetPath,
            priceInCents = priceInCents,
            active = active
        )
}
