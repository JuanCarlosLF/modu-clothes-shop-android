package com.example.modu.catalog.generator.validation

import com.example.modu.catalog.generator.exception.CatalogSeedValidationException
import com.example.modu.catalog.generator.model.ParsedCatalog
import com.example.modu.catalog.generator.model.ProductVariantRow

private const val SUPPORTED_FORMAT_VERSION = 1

internal class CatalogSeedValidator {

    fun validate(catalog: ParsedCatalog) {
        val violations = mutableListOf<CatalogSeedViolation>()

        if (catalog.formatVersion != SUPPORTED_FORMAT_VERSION) {
            violations += violation(
                CatalogSeedViolationCode.UNSUPPORTED_FORMAT_VERSION,
                "formatVersion",
                "Expected format version $SUPPORTED_FORMAT_VERSION, but found ${catalog.formatVersion}"
            )
        }
        validateProducts(catalog, violations)
        validateCategories(catalog, violations)
        validateRelations(catalog, violations)
        val variantsByProductId = validateVariants(catalog, violations)
        validateAvailability(catalog, variantsByProductId, violations)

        if (violations.isNotEmpty()) {
            throw CatalogSeedValidationException(CatalogSeedValidationReport(violations))
        }
    }

    private fun validateProducts(catalog: ParsedCatalog, violations: MutableList<CatalogSeedViolation>) {
        catalog.products.forEachIndexed { index, product ->
            val path = "products[$index]"
            if (product.name.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_PRODUCT_NAME,
                    "$path.name",
                    "Expected a non-blank product name"
                )
            }
            if (product.description.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_PRODUCT_DESCRIPTION,
                    "$path.description",
                    "Expected a non-blank product description"
                )
            }
            if (product.priceInCents <= 0) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_PRODUCT_PRICE,
                    "$path.priceInCents",
                    "Expected product price in cents greater than 0, but found ${product.priceInCents}"
                )
            }
        }
    }

    private fun validateCategories(catalog: ParsedCatalog, violations: MutableList<CatalogSeedViolation>) {
        catalog.categories.forEachIndexed { index, category ->
            if (category.name.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_CATEGORY_NAME,
                    "categories[$index].name",
                    "Expected a non-blank category name"
                )
            }
        }
    }

    private fun validateRelations(catalog: ParsedCatalog, violations: MutableList<CatalogSeedViolation>) {
        val connectedProducts = catalog.productCategories.mapTo(mutableSetOf()) { it.productId }
        val connectedCategories = catalog.productCategories.mapTo(mutableSetOf()) { it.categoryId }

        catalog.products.forEachIndexed { index, product ->
            if (product.id !in connectedProducts) {
                violations += violation(
                    CatalogSeedViolationCode.PRODUCT_WITHOUT_CATEGORY,
                    "products[$index]",
                    "Product ID ${product.id} does not appear in a product-category relation"
                )
            }
        }
        catalog.categories.forEachIndexed { index, category ->
            if (category.id !in connectedCategories) {
                violations += violation(
                    CatalogSeedViolationCode.CATEGORY_WITHOUT_PRODUCT,
                    "categories[$index]",
                    "Category ID ${category.id} does not appear in a product-category relation"
                )
            }
        }
    }

    private fun validateVariants(
        catalog: ParsedCatalog,
        violations: MutableList<CatalogSeedViolation>
    ): Map<Int, List<ProductVariantRow>> {
        val firstIndexBySelection = mutableMapOf<Triple<Int, String, String>, Int>()
        val variantsByProductId = mutableMapOf<Int, MutableList<ProductVariantRow>>()

        catalog.productVariants.forEachIndexed { index, variant ->
            val path = "productVariants[$index]"
            if (variant.variantCode.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_VARIANT_CODE,
                    "$path.variantCode",
                    "Expected a non-blank variant code"
                )
            }
            if (variant.size.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_VARIANT_SIZE,
                    "$path.size",
                    "Expected a non-blank variant size"
                )
            }
            if (variant.color.isBlank()) {
                violations += violation(
                    CatalogSeedViolationCode.INVALID_VARIANT_COLOR,
                    "$path.color",
                    "Expected a non-blank variant color"
                )
            }
            if (variant.stock < 0) {
                violations += violation(
                    CatalogSeedViolationCode.NEGATIVE_STOCK,
                    "$path.stock",
                    "Expected variant stock greater than or equal to 0, but found ${variant.stock}"
                )
            }

            val selection = Triple(variant.productId, variant.size, variant.color)
            val firstIndex = firstIndexBySelection.putIfAbsent(selection, index)
            if (firstIndex != null) {
                violations += violation(
                    CatalogSeedViolationCode.DUPLICATE_VARIANT_SELECTION,
                    path,
                    "Expected a unique product, size, and color selection; first declared at productVariants[$firstIndex]"
                )
            }
            variantsByProductId.getOrPut(variant.productId) { mutableListOf() } += variant
        }
        return variantsByProductId
    }

    private fun validateAvailability(
        catalog: ParsedCatalog,
        variantsByProductId: Map<Int, List<ProductVariantRow>>,
        violations: MutableList<CatalogSeedViolation>
    ) {
        catalog.products.forEachIndexed { index, product ->
            if (product.active && variantsByProductId[product.id].orEmpty().none { it.active && it.stock > 0 }) {
                violations += violation(
                    CatalogSeedViolationCode.ACTIVE_PRODUCT_WITHOUT_PURCHASABLE_VARIANT,
                    "products[$index]",
                    "Active product ID ${product.id} has no active variant with stock greater than 0"
                )
            }
        }
    }

    private fun violation(code: CatalogSeedViolationCode, path: String, message: String) =
        CatalogSeedViolation(code, path, message)
}
