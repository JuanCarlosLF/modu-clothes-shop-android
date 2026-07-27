package com.example.modu.catalog.generator.validation

import org.junit.Assert.assertEquals
import org.junit.Test

internal class CatalogSeedValidationReportTest {

    @Test
    fun format_givenMutableViolations_whenCalled_thenUsesImmutableOrderedSnapshot() {
        val first = CatalogSeedViolation(
            CatalogSeedViolationCode.INVALID_PRODUCT_PRICE,
            "products[0].priceInCents",
            "Expected a value greater than zero, but found 0"
        )
        val source = mutableListOf(first)
        val report = CatalogSeedValidationReport(source)
        source += CatalogSeedViolation(
            CatalogSeedViolationCode.NEGATIVE_STOCK,
            "productVariants[1].stock",
            "Expected a non-negative value, but found -1"
        )

        assertEquals(listOf(first), report.violations)
        assertEquals(
            """
                Catalog seed validation failed with 1 violations:

                1. [INVALID_PRODUCT_PRICE] products[0].priceInCents
                   Expected a value greater than zero, but found 0
            """.trimIndent(),
            report.format()
        )
    }
}
