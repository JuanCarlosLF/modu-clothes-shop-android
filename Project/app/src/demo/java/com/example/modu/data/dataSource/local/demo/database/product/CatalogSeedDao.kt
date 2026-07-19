package com.example.modu.data.dataSource.local.demo.database.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductCategoryCrossRefDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductVariantDbo

@Dao
interface CatalogSeedDao {

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Insert
    suspend fun insertProducts(products: List<ProductDbo>)

    @Insert
    suspend fun insertCategories(categories: List<CategoryDbo>)

    @Insert
    suspend fun insertProductCategories(productCategories: List<ProductCategoryCrossRefDbo>)

    @Insert
    suspend fun insertProductVariants(productVariants: List<ProductVariantDbo>)

    @Transaction
    suspend fun insertCatalog(
        categories: List<CategoryDbo>,
        products: List<ProductDbo>,
        productCategories: List<ProductCategoryCrossRefDbo>,
        productVariants: List<ProductVariantDbo>
    ) {
        insertCategories(categories)
        insertProducts(products)
        insertProductCategories(productCategories)
        insertProductVariants(productVariants)
    }
}
