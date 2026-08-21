package com.example.modu.catalog.generator.validation

import com.example.modu.catalog.generator.exception.CatalogSeedValidationException
import com.example.modu.catalog.generator.model.CategoryRow
import com.example.modu.catalog.generator.model.ParsedCatalog
import com.example.modu.catalog.generator.model.ProductCategoryRow
import com.example.modu.catalog.generator.model.ProductRow
import com.example.modu.catalog.generator.model.ProductVariantRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assume.assumeNoException
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class CatalogSeedValidatorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val assetRoot: Path
        get() = temporaryFolder.root.toPath()

    private val validator = CatalogSeedValidator()

    @Test
    fun validate_givenValidCatalog_whenCalled_thenDoesNotThrow() {
        validator.validate(catalog(), assetRoot)
    }

    @Test
    fun validate_givenArbitraryNonblankTextWithOuterWhitespace_whenCalled_thenDoesNotThrow() {
        val valid = catalog()
        validator.validate(
            valid.copy(
                products = valid.products.map { it.copy(name = " Jacket ", description = " Description ") },
                categories = valid.categories.map { it.copy(name = " Streetwear ") },
                productVariants = valid.productVariants.map {
                    it.copy(variantCode = " code ", size = " One size ", color = " Burnt orange ")
                }
            ),
            assetRoot
        )
    }

    @Test
    fun validate_givenInactiveProductWithoutVariants_whenCalled_thenDoesNotThrow() {
        val valid = catalog()
        validator.validate(
            valid.copy(products = valid.products.map { it.copy(active = false) }, productVariants = emptyList()),
            assetRoot
        )
    }

    @Test
    fun validate_givenDatabaseOwnedDuplicateAndForeignKeyValues_whenCalled_thenDoesNotReportThem() {
        val valid = catalog()
        validator.validate(
            valid.copy(
                products = valid.products + valid.products.first(),
                categories = valid.categories + valid.categories.first(),
                productCategories = listOf(ProductCategoryRow(1, 1), ProductCategoryRow(1, 1), ProductCategoryRow(999, 999)),
                productVariants = listOf(
                    valid.productVariants.first(),
                    valid.productVariants.first().copy(id = 1, productId = 999, size = "M")
                )
            ),
            assetRoot
        )
    }

    @Test
    fun validate_givenInvalidScalarValues_whenCalled_thenReportsEachSemanticRule() {
        data class Case(
            val catalog: ParsedCatalog,
            val code: CatalogSeedViolationCode,
            val path: String
        )

        val valid = catalog()
        listOf(
            Case(valid.copy(formatVersion = 2), CatalogSeedViolationCode.UNSUPPORTED_FORMAT_VERSION, "formatVersion"),
            Case(valid.withProduct { it.copy(name = " \n") }, CatalogSeedViolationCode.INVALID_PRODUCT_NAME, "products[0].name"),
            Case(valid.withProduct { it.copy(description = "\t") }, CatalogSeedViolationCode.INVALID_PRODUCT_DESCRIPTION, "products[0].description"),
            Case(valid.withProduct { it.copy(priceInCents = 0) }, CatalogSeedViolationCode.INVALID_PRODUCT_PRICE, "products[0].priceInCents"),
            Case(valid.copy(categories = listOf(CategoryRow(1, " "))), CatalogSeedViolationCode.INVALID_CATEGORY_NAME, "categories[0].name"),
            Case(valid.withVariant { it.copy(variantCode = " ") }, CatalogSeedViolationCode.INVALID_VARIANT_CODE, "productVariants[0].variantCode"),
            Case(valid.withVariant { it.copy(size = "\n") }, CatalogSeedViolationCode.INVALID_VARIANT_SIZE, "productVariants[0].size"),
            Case(valid.withVariant { it.copy(color = "\t") }, CatalogSeedViolationCode.INVALID_VARIANT_COLOR, "productVariants[0].color"),
            Case(
                valid.copy(
                    products = valid.products.map { it.copy(active = false) },
                    productVariants = valid.productVariants.map { it.copy(stock = -1) }
                ),
                CatalogSeedViolationCode.NEGATIVE_STOCK,
                "productVariants[0].stock"
            )
        ).forEach { case ->
            assertEquals(listOf(case.code to case.path), violations(case.catalog))
        }
    }

    @Test
    fun validate_givenMissingRelations_whenCalled_thenReportsBothCoverageRulesInOrder() {
        val valid = catalog()
        val invalid = valid.copy(
            products = valid.products + valid.products.first().copy(id = 2, active = false),
            categories = valid.categories + CategoryRow(2, "Outerwear")
        )

        assertEquals(
            listOf(
                CatalogSeedViolationCode.PRODUCT_WITHOUT_CATEGORY to "products[1]",
                CatalogSeedViolationCode.CATEGORY_WITHOUT_PRODUCT to "categories[1]"
            ),
            violations(invalid)
        )
    }

    @Test
    fun validate_givenDuplicateSelection_whenCalled_thenReportsSelectionRule() {
        val valid = catalog()
        val duplicate = valid.productVariants.first().copy(id = 2, variantCode = "second")

        assertEquals(
            listOf(CatalogSeedViolationCode.DUPLICATE_VARIANT_SELECTION to "productVariants[1]"),
            violations(valid.copy(productVariants = valid.productVariants + duplicate))
        )
    }

    @Test
    fun validate_givenActiveProductWithoutPurchasableVariant_whenCalled_thenReportsAvailabilityRule() {
        assertEquals(
            listOf(CatalogSeedViolationCode.ACTIVE_PRODUCT_WITHOUT_PURCHASABLE_VARIANT to "products[0]"),
            violations(catalog().withVariant { it.copy(active = false, stock = 0) })
        )
    }

    @Test
    fun validate_givenInvalidImagePaths_whenCalled_thenReportsEachPath() {
        listOf(
            "",
            "images/jacket.jpg",
            "catalog/images/",
            "catalog/images\\jacket.jpg",
            "catalog/images//jacket.jpg",
            "catalog/images/./jacket.jpg",
            "catalog/images/../jacket.jpg",
            "catalog/images/invalid\u0000.jpg"
        ).forEach { invalidPath ->
            assertEquals(
                listOf(CatalogSeedViolationCode.INVALID_IMAGE_PATH to "products[0].imageAssetPath"),
                violations(catalog().withProduct { it.copy(imageAssetPath = invalidPath) })
            )
        }
    }

    @Test
    fun validate_givenMissingImageAsset_whenCalled_thenReportsViolation() {
        assertEquals(
            listOf(CatalogSeedViolationCode.MISSING_IMAGE_ASSET to "products[0].imageAssetPath"),
            violations(catalog().withProduct { it.copy(imageAssetPath = "catalog/images/missing.jpg") })
        )
    }

    @Test
    fun validate_givenImageSymlink_whenCalled_thenReportsMissingAsset() {
        val outsideFile = temporaryFolder.newFile("outside.jpg").toPath()
        val link = assetRoot.resolve("catalog/images/link.jpg")
        Files.createDirectories(link.parent)
        try {
            Files.createSymbolicLink(link, outsideFile)
        } catch (error: UnsupportedOperationException) {
            assumeNoException("Symbolic links are unsupported", error)
        } catch (error: IOException) {
            assumeNoException("Symbolic links are unavailable", error)
        } catch (error: SecurityException) {
            assumeNoException("Symbolic links are denied", error)
        }

        assertEquals(
            listOf(CatalogSeedViolationCode.MISSING_IMAGE_ASSET to "products[0].imageAssetPath"),
            violations(catalog().withProduct { it.copy(imageAssetPath = "catalog/images/link.jpg") })
        )
    }

    @Test
    fun validate_givenFailuresAcrossSections_whenCalled_thenAggregatesInDeterministicOrder() {
        val invalid = catalog().copy(
            formatVersion = 2,
            products = listOf(ProductRow(2, " ", " ", "catalog/images/jacket.jpg", 0, true)),
            categories = listOf(CategoryRow(2, " ")),
            productCategories = emptyList(),
            productVariants = listOf(ProductVariantRow(1, 2, " ", " ", " ", -1, false))
        )

        assertEquals(
            listOf(
                CatalogSeedViolationCode.UNSUPPORTED_FORMAT_VERSION,
                CatalogSeedViolationCode.INVALID_PRODUCT_NAME,
                CatalogSeedViolationCode.INVALID_PRODUCT_DESCRIPTION,
                CatalogSeedViolationCode.INVALID_PRODUCT_PRICE,
                CatalogSeedViolationCode.INVALID_CATEGORY_NAME,
                CatalogSeedViolationCode.PRODUCT_WITHOUT_CATEGORY,
                CatalogSeedViolationCode.CATEGORY_WITHOUT_PRODUCT,
                CatalogSeedViolationCode.INVALID_VARIANT_CODE,
                CatalogSeedViolationCode.INVALID_VARIANT_SIZE,
                CatalogSeedViolationCode.INVALID_VARIANT_COLOR,
                CatalogSeedViolationCode.NEGATIVE_STOCK,
                CatalogSeedViolationCode.ACTIVE_PRODUCT_WITHOUT_PURCHASABLE_VARIANT
            ),
            violations(invalid).map { it.first }
        )
    }

    private fun catalog(): ParsedCatalog {
        createAsset("catalog/images/jacket.jpg")
        return ParsedCatalog(
        formatVersion = 1,
        products = listOf(ProductRow(1, "Jacket", "Description", "catalog/images/jacket.jpg", 7_999, true)),
        categories = listOf(CategoryRow(1, "Streetwear")),
        productCategories = listOf(ProductCategoryRow(1, 1)),
        productVariants = listOf(ProductVariantRow(1, 1, "code", "S", "BLACK", 1, true))
        )
    }

    private fun ParsedCatalog.withProduct(transform: (ProductRow) -> ProductRow) = copy(products = products.map(transform))

    private fun ParsedCatalog.withVariant(transform: (ProductVariantRow) -> ProductVariantRow) =
        copy(productVariants = productVariants.map(transform))

    private fun violations(catalog: ParsedCatalog): List<Pair<CatalogSeedViolationCode, String>> {
        val exception = assertThrows(CatalogSeedValidationException::class.java) {
            validator.validate(catalog, assetRoot)
        }
        return exception.report.violations.map { it.code to it.path }
    }

    private fun createAsset(relativePath: String) {
        val path = assetRoot.resolve(relativePath)
        Files.createDirectories(path.parent)
        if (Files.notExists(path)) Files.createFile(path)
    }
}
