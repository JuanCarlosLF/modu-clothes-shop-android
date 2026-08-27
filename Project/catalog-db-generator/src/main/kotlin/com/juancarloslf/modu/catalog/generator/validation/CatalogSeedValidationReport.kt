package com.juancarloslf.modu.catalog.generator.validation

internal enum class CatalogSeedViolationCode {
    UNSUPPORTED_FORMAT_VERSION,
    INVALID_PRODUCT_NAME,
    INVALID_PRODUCT_DESCRIPTION,
    INVALID_PRODUCT_PRICE,
    INVALID_IMAGE_PATH,
    MISSING_IMAGE_ASSET,
    INVALID_CATEGORY_NAME,
    PRODUCT_WITHOUT_CATEGORY,
    CATEGORY_WITHOUT_PRODUCT,
    INVALID_VARIANT_CODE,
    INVALID_VARIANT_SIZE,
    INVALID_VARIANT_COLOR,
    NEGATIVE_STOCK,
    DUPLICATE_VARIANT_SELECTION,
    ACTIVE_PRODUCT_WITHOUT_PURCHASABLE_VARIANT
}

internal data class CatalogSeedViolation(
    val code: CatalogSeedViolationCode,
    val path: String,
    val message: String
)

internal class CatalogSeedValidationReport(violations: List<CatalogSeedViolation>) {
    val violations: List<CatalogSeedViolation> = violations.toList()

    fun format(): String = violations.mapIndexed { index, violation ->
        "${index + 1}. [${violation.code}] ${violation.path}\n   ${violation.message}"
    }.joinToString(
        separator = "\n\n",
        prefix = "Catalog seed validation failed with ${violations.size} violations:\n\n"
    )
}
