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
    fun getCategories_givenMixedCaseCategories_whenCalled_thenReturnsCategoriesOrderedCaseInsensitivelyByName() =
        runBlocking {
            // GIVEN
            val zebraCategory = CategoryDbo(id = 1, name = "zebra")
            val appleCategory = CategoryDbo(id = 2, name = "apple")
            val bananaCategory = CategoryDbo(id = 3, name = "Banana")
            catalogFixture.insertCategories(
                listOf(zebraCategory, appleCategory, bananaCategory)
            )

            // WHEN
            val categories = productDao.getCategories()

            // THEN
            assertEquals(
                listOf(appleCategory, bananaCategory, zebraCategory),
                categories
            )
        }

    @Test
    fun getProductsBy_givenActiveAndInactiveProducts_whenCalled_thenReturnsOnlyActiveProducts() = runBlocking {
        // GIVEN
        val activeProduct = getProduct(id = 1, name = "active", active = true)
        val inactiveProduct = getProduct(id = 2, name = "inactive", active = false)
        catalogFixture.insertProducts(listOf(activeProduct, inactiveProduct))

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
            catalogFixture.insertProducts(listOf(matchingProduct, nonMatchingProduct))

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
            catalogFixture.insertProducts(listOf(matchingProduct, unrelatedProduct))

            // WHEN
            val products = loadProducts(title = "%")

            // THEN
            assertEquals(listOf(matchingProduct), products)
        }

    @Test
    fun getProductsBy_givenUnderscoreInTitle_whenCalled_thenTreatsUnderscoreLiterally() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Cotton_Shirt")
            val unrelatedProduct = getProduct(id = 2, name = "Cotton Shirt")
            catalogFixture.insertProducts(listOf(matchingProduct, unrelatedProduct))

            // WHEN
            val products = loadProducts(title = "_")

            // THEN
            assertEquals(listOf(matchingProduct), products)
        }

    @Test
    fun getProductsBy_givenBackslashInTitle_whenCalled_thenTreatsBackslashLiterally() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Cotton\\Shirt")
            val unrelatedProduct = getProduct(id = 2, name = "Cotton Shirt")
            catalogFixture.insertProducts(listOf(matchingProduct, unrelatedProduct))

            // WHEN
            val products = loadProducts(title = "\\")

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
            catalogFixture.insertProducts(
                listOf(cheapProduct, boundaryProduct, expensiveProduct)
            )

            // WHEN
            val products = loadProducts(maxPriceInCents = 2_000L)

            // THEN
            assertEquals(
                listOf(boundaryProduct, cheapProduct),
                products
            )
        }

    @Test
    fun getProductsBy_givenNoPriceOrder_whenCalled_thenReturnsProductsByDescendingId() =
        runBlocking {
            // GIVEN
            val lowestIdProduct = getProduct(id = 1)
            val middleIdProduct = getProduct(id = 2)
            val highestIdProduct = getProduct(id = 3)
            catalogFixture.insertProducts(
                listOf(lowestIdProduct, middleIdProduct, highestIdProduct)
            )

            // WHEN
            val products = loadProducts()

            // THEN
            assertEquals(
                listOf(highestIdProduct, middleIdProduct, lowestIdProduct),
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
            catalogFixture.insertProducts(
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
    fun getProductsBy_givenEqualPricesInAscendingOrder_whenCalled_thenBreaksTieByDescendingId() =
        runBlocking {
            // GIVEN
            val lowestIdProduct = getProduct(id = 1, priceInCents = 1_000L)
            val highestIdProduct = getProduct(id = 2, priceInCents = 1_000L)
            catalogFixture.insertProducts(listOf(lowestIdProduct, highestIdProduct))

            // WHEN
            val products = loadProducts(orderByPrice = "asc")

            // THEN
            assertEquals(listOf(highestIdProduct, lowestIdProduct), products)
        }

    @Test
    fun getProductsBy_givenDescendingPriceOrder_whenCalled_thenReturnsProductsFromHighestToLowestPrice() =
        runBlocking {
            // GIVEN
            val expensiveProduct = getProduct(id = 1, priceInCents = 3_000L)
            val cheapProduct = getProduct(id = 2, priceInCents = 1_000L)
            val mediumProduct = getProduct(id = 3, priceInCents = 2_000L)
            catalogFixture.insertProducts(
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
    fun getProductsBy_givenEqualPricesInDescendingOrder_whenCalled_thenBreaksTieByDescendingId() =
        runBlocking {
            // GIVEN
            val lowestIdProduct = getProduct(id = 1, priceInCents = 1_000L)
            val highestIdProduct = getProduct(id = 2, priceInCents = 1_000L)
            catalogFixture.insertProducts(listOf(lowestIdProduct, highestIdProduct))

            // WHEN
            val products = loadProducts(orderByPrice = "desc")

            // THEN
            assertEquals(listOf(highestIdProduct, lowestIdProduct), products)
        }

    @Test
    fun getProductsBy_givenSamePriceProductsAcrossPages_whenLoadingRefreshAndAppend_thenReturnsEveryProductByDescendingId() =
        runBlocking {
            // GIVEN
            val products = (1..25).map { id ->
                getProduct(id = id, priceInCents = 1_000L)
            }
            catalogFixture.insertProducts(products)
            val pagingSource = productDao.getProductsBy(
                title = null,
                maxPriceInCents = null,
                orderByPrice = "asc",
                categoryNames = emptyList(),
                categoryCount = 0
            )

            // WHEN
            val refreshPage = requirePage(
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 20,
                        placeholdersEnabled = false
                    )
                )
            )
            val appendPage = requirePage(
                pagingSource.load(
                    PagingSource.LoadParams.Append(
                        key = requireNotNull(refreshPage.nextKey),
                        loadSize = 20,
                        placeholdersEnabled = false
                    )
                )
            )
            val loadedIds = (refreshPage.data + appendPage.data).map(ProductDbo::id)

            // THEN
            assertEquals((25 downTo 1).toList(), loadedIds)
        }

    @Test
    fun getProductsBy_givenProductWithAllSelectedAndExtraCategory_whenCalled_thenIncludesProduct() =
        runBlocking {
            // GIVEN
            val matchingProduct = getProduct(id = 1, name = "Linen Shirt")
            val shirtOnlyProduct = getProduct(id = 2, name = "Cotton Shirt")
            val summerOnlyProduct = getProduct(id = 3, name = "Summer Shorts")
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
            catalogFixture.insertProducts(listOf(productWithoutCategories, categorizedProduct))
            catalogFixture.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
            catalogFixture.insertProductCategories(
                listOf(ProductCategoryCrossRefDbo(productId = categorizedProduct.id, categoryId = 1))
            )

            // WHEN
            val products = loadProducts(categoryNames = emptyList())

            // THEN
            assertEquals(listOf(categorizedProduct, productWithoutCategories), products)
        }

    @Test
    fun getProductsBy_givenUnknownCategory_whenCalled_thenReturnsEmptyResult() = runBlocking {
        // GIVEN
        val categorizedProduct = getProduct(id = 1)
        catalogFixture.insertProducts(listOf(categorizedProduct))
        catalogFixture.insertCategories(listOf(CategoryDbo(id = 1, name = "Shirt")))
        catalogFixture.insertProductCategories(
            listOf(ProductCategoryCrossRefDbo(productId = categorizedProduct.id, categoryId = 1))
        )

        // WHEN
        val products = loadProducts(categoryNames = listOf("Unknown"))

        // THEN
        assertEquals(emptyList<ProductDbo>(), products)
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
        return requirePage(
            pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        ).data
    }

    private fun requirePage(
        result: PagingSource.LoadResult<Int, ProductDbo>
    ): PagingSource.LoadResult.Page<Int, ProductDbo> =
        when (result) {
            is PagingSource.LoadResult.Page -> result

            is PagingSource.LoadResult.Error ->
                throw AssertionError(
                    "Expected a page but loading failed",
                    result.throwable
                )

            is PagingSource.LoadResult.Invalid ->
                throw AssertionError("Expected a page but PagingSource was invalid")
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
