package com.example.modu.data.dataSource.local.demo.database.product

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo

internal class CatalogFixture(database: ModuDemoDatabase) {

    private val writableDatabase = database.openHelper.writableDatabase

    fun insertProducts(products: List<ProductDbo>) = insertCatalog(products = products)

    fun insertCategories(categories: List<CategoryDbo>) = insertCatalog(categories = categories)

    fun insertProductCategories(productCategories: List<ProductCategoryCrossRefDbo>) =
        insertCatalog(productCategories = productCategories)

    fun insertCatalog(
        categories: List<CategoryDbo> = emptyList(),
        products: List<ProductDbo> = emptyList(),
        productCategories: List<ProductCategoryCrossRefDbo> = emptyList(),
        productVariants: List<ProductVariantDbo> = emptyList()
    ) = writableDatabase.inTransaction {
        compileStatement("INSERT INTO categories (id, name) VALUES (?, ?)").use { statement ->
            categories.forEach { category ->
                statement.bindLong(1, category.id.toLong())
                statement.bindString(2, category.name)
                statement.executeInsert()
                statement.clearBindings()
            }
        }
        compileStatement(
            """INSERT INTO products
                (id, name, description, imageAssetPath, priceInCents, active)
                VALUES (?, ?, ?, ?, ?, ?)""".trimIndent()
        ).use { statement ->
            products.forEach { product ->
                statement.bindLong(1, product.id.toLong())
                statement.bindString(2, product.name)
                statement.bindString(3, product.description)
                statement.bindString(4, product.imageAssetPath)
                statement.bindLong(5, product.priceInCents)
                statement.bindLong(6, if (product.active) 1 else 0)
                statement.executeInsert()
                statement.clearBindings()
            }
        }
        compileStatement("INSERT INTO product_categories (productId, categoryId) VALUES (?, ?)").use { statement ->
            productCategories.forEach { relation ->
                statement.bindLong(1, relation.productId.toLong())
                statement.bindLong(2, relation.categoryId.toLong())
                statement.executeInsert()
                statement.clearBindings()
            }
        }
        compileStatement(
            """INSERT INTO product_variants
                (id, variantCode, color, size, productId, stock, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)""".trimIndent()
        ).use { statement ->
            productVariants.forEach { variant ->
                statement.bindLong(1, variant.id.toLong())
                statement.bindString(2, variant.variantCode)
                statement.bindString(3, variant.color)
                statement.bindString(4, variant.size)
                statement.bindLong(5, variant.productId.toLong())
                statement.bindLong(6, variant.stock.toLong())
                statement.bindLong(7, if (variant.active) 1 else 0)
                statement.executeInsert()
                statement.clearBindings()
            }
        }
    }

    private inline fun SupportSQLiteDatabase.inTransaction(block: SupportSQLiteDatabase.() -> Unit) {
        beginTransaction()
        try {
            block()
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }
}
