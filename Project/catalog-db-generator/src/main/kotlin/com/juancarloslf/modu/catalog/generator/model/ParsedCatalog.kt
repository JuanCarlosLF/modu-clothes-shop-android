package com.juancarloslf.modu.catalog.generator.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ParsedCatalog(
    val formatVersion: Int,
    val products: List<ProductRow>,
    val categories: List<CategoryRow>,
    val productCategories: List<ProductCategoryRow>,
    val productVariants: List<ProductVariantRow>
)

@Serializable
internal data class CategoryRow(
    val id: Int,
    val name: String
)

@Serializable
internal data class ProductRow(
    val id: Int,
    val name: String,
    val description: String,
    val imageAssetPath: String,
    val priceInCents: Long,
    val active: Boolean
)

@Serializable
internal data class ProductCategoryRow(
    val productId: Int,
    val categoryId: Int
)

@Serializable
internal data class ProductVariantRow(
    val id: Int,
    val productId: Int,
    val variantCode: String,
    val size: String,
    val color: String,
    val stock: Int,
    val active: Boolean
)
