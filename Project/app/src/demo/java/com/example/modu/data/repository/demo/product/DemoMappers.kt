package com.example.modu.data.repository.demo.product

import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDetailDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.detail.ProductVariant
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import java.math.BigDecimal

private const val ANDROID_ASSET_URI_PREFIX = "file:///android_asset/"
private const val PRICE_SCALE = 2

internal fun ProductDbo.toDomain(): Product =
    Product(
        id = id,
        image = imageAssetPath.toAssetUri()
    )

internal fun CategoryDbo.toDomain(): Category =
    Category(
        name = name
    )

internal fun ProductVariantDbo.toDomain(): ProductVariant =
    ProductVariant(
        id = id,
        name = variantCode,
        color = color,
        size = size,
        stock = stock,
        active = active,
        productId = productId
    )

internal fun ProductDetailDbo.toDomain(): Detail =
    Detail(
        id = product.id,
        name = product.name,
        description = product.description,
        imageUrl = product.imageAssetPath.toAssetUri(),
        price = BigDecimal.valueOf(product.priceInCents, PRICE_SCALE),
        categoriesSet = categories.map { it.toDomain() },
        productVariantsList = variants.map { it.toDomain() }
    )

private fun String.toAssetUri(): String = ANDROID_ASSET_URI_PREFIX + trimStart('/')
