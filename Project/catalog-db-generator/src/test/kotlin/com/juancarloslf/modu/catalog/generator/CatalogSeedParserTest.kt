package com.juancarloslf.modu.catalog.generator

import com.juancarloslf.modu.catalog.generator.exception.CatalogInputReadException
import com.juancarloslf.modu.catalog.generator.exception.CatalogSeedParseException
import com.juancarloslf.modu.catalog.generator.model.CategoryRow
import com.juancarloslf.modu.catalog.generator.model.ProductCategoryRow
import com.juancarloslf.modu.catalog.generator.model.ProductRow
import com.juancarloslf.modu.catalog.generator.model.ProductVariantRow
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

internal class CatalogSeedParserTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val parser = CatalogSeedParser()

    @Test
    fun parse_givenValidCatalog_whenCalled_thenMapsAllRowCollections() {
        // GIVEN
        val json = """
            {
              "formatVersion": 1,
              "products": [
                {
                  "id": 1,
                  "name": "Classic Denim Jacket",
                  "description": "Description",
                  "imageAssetPath": "catalog/images/jacket.jpg",
                  "priceInCents": 7999,
                  "active": true
                }
              ],
              "categories": [
                {
                  "id": 1,
                  "name": "Streetwear"
                }
              ],
              "productCategories": [
                {
                  "productId": 1,
                  "categoryId": 1
                }
              ],
              "productVariants": [
                {
                  "id": 1,
                  "productId": 1,
                  "variantCode": "1_S_BLUE",
                  "size": "S",
                  "color": "BLUE",
                  "stock": 7,
                  "active": true
                }
              ]
            }
        """.trimIndent()
        val expectedProduct = ProductRow(
            id = 1,
            name = "Classic Denim Jacket",
            description = "Description",
            imageAssetPath = "catalog/images/jacket.jpg",
            priceInCents = 7_999L,
            active = true
        )
        val expectedCategory = CategoryRow(id = 1, name = "Streetwear")
        val expectedProductCategory = ProductCategoryRow(productId = 1, categoryId = 1)
        val expectedProductVariant = ProductVariantRow(
            id = 1,
            productId = 1,
            variantCode = "1_S_BLUE",
            size = "S",
            color = "BLUE",
            stock = 7,
            active = true
        )

        // WHEN
        val catalog = parser.parse(writeCatalog("valid.json", json))

        // THEN
        assertEquals(1, catalog.formatVersion)
        assertEquals(listOf(expectedProduct), catalog.products)
        assertEquals(listOf(expectedCategory), catalog.categories)
        assertEquals(listOf(expectedProductCategory), catalog.productCategories)
        assertEquals(listOf(expectedProductVariant), catalog.productVariants)
    }

    @Test
    fun parse_givenMalformedJson_whenCalled_thenThrowsParseExceptionWithSourceAndCause() {
        // GIVEN
        val json = """
            {
              "formatVersion": 1,
              "products": [
            }
        """.trimIndent()
        val sourcePath = writeCatalog("malformed.json", json)

        // WHEN
        val exception = assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }

        // THEN
        assertEquals(sourcePath, exception.sourcePath)
        assertTrue(exception.cause is SerializationException)
    }

    @Test
    fun parse_givenUnknownField_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = emptyCatalogJson("\"unexpectedField\": true,")
        val sourcePath = writeCatalog("unknown-field.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenNestedUnknownField_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = productCatalogJson(extraProductField = "\"unexpectedField\": true,")
        val sourcePath = writeCatalog("nested-unknown-field.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenMissingRequiredField_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = """
            {
              "formatVersion": 1,
              "products": [
                {
                  "id": 1,
                  "description": "Description",
                  "imageAssetPath": "catalog/images/jacket.jpg",
                  "priceInCents": 7999,
                  "active": true
                }
              ],
              "categories": [],
              "productCategories": [],
              "productVariants": []
            }
        """.trimIndent()
        val sourcePath = writeCatalog("missing-required-field.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenNullRequiredField_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = """
            {
              "formatVersion": 1,
              "products": [
                {
                  "id": 1,
                  "name": null,
                  "description": "Description",
                  "imageAssetPath": "catalog/images/jacket.jpg",
                  "priceInCents": 7999,
                  "active": true
                }
              ],
              "categories": [],
              "productCategories": [],
              "productVariants": []
            }
        """.trimIndent()
        val sourcePath = writeCatalog("null-required-field.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenWrongFieldType_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = emptyCatalogJson("\"formatVersion\": true,", includeFormatVersion = false)
        val sourcePath = writeCatalog("wrong-field-type.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenDecimalPriceInCents_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = productCatalogJson(priceInCents = "79.99")
        val sourcePath = writeCatalog("decimal-price.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenPriceOutsideLongRange_whenCalled_thenThrowsParseException() {
        // GIVEN
        val json = productCatalogJson(priceInCents = "9223372036854775808")
        val sourcePath = writeCatalog("price-outside-long-range.json", json)

        // WHEN / THEN
        assertThrows(CatalogSeedParseException::class.java) {
            parser.parse(sourcePath)
        }
    }

    @Test
    fun parse_givenMissingFile_whenCalled_thenThrowsInputReadExceptionWithSourceAndCause() {
        // GIVEN
        val sourcePath = temporaryFolder.root.toPath().resolve("missing.json")

        // WHEN
        val exception = assertThrows(CatalogInputReadException::class.java) {
            parser.parse(sourcePath)
        }

        // THEN
        assertEquals(sourcePath, exception.inputPath)
        assertTrue(exception.cause is NoSuchFileException)
    }

    private fun writeCatalog(name: String, json: String): Path =
        Files.writeString(temporaryFolder.root.toPath().resolve(name), json, UTF_8)

    private fun emptyCatalogJson(
        extraField: String,
        includeFormatVersion: Boolean = true
    ): String = """
        {
          ${if (includeFormatVersion) "\"formatVersion\": 1," else ""}
          $extraField
          "products": [],
          "categories": [],
          "productCategories": [],
          "productVariants": []
        }
    """.trimIndent()

    private fun productCatalogJson(
        priceInCents: String = "7999",
        extraProductField: String = ""
    ): String = """
        {
          "formatVersion": 1,
          "products": [
            {
              $extraProductField
              "id": 1,
              "name": "Classic Denim Jacket",
              "description": "Description",
              "imageAssetPath": "catalog/images/jacket.jpg",
              "priceInCents": $priceInCents,
              "active": true
            }
          ],
          "categories": [],
          "productCategories": [],
          "productVariants": []
        }
    """.trimIndent()

}
