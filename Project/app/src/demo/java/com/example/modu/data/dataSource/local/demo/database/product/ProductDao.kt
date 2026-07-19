package com.example.modu.data.dataSource.local.demo.database.product

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.example.modu.data.dataSource.local.demo.database.product.dbo.CategoryDbo
import com.example.modu.data.dataSource.local.demo.database.product.dbo.ProductDbo

@Dao
interface ProductDao {

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getCategories(): List<CategoryDbo>

    @Query(
        """
        SELECT p.*
        FROM products AS p
        LEFT JOIN product_categories AS pc ON p.id = pc.productId
        LEFT JOIN categories AS c ON pc.categoryId = c.id
        WHERE p.active = 1
          AND (
              :title IS NULL
              OR INSTR(LOWER(p.name), LOWER(:title)) > 0
          )
          AND (
              :maxPriceInCents IS NULL
              OR p.priceInCents <= :maxPriceInCents
          )
          AND (
              :categoryCount = 0
              OR c.name IN (:categoryNames)
          )
        GROUP BY p.id
        HAVING :categoryCount = 0
            OR COUNT(DISTINCT c.id) = :categoryCount
        ORDER BY
            CASE WHEN :orderByPrice = 'asc' THEN p.priceInCents END ASC,
            CASE WHEN :orderByPrice = 'desc' THEN p.priceInCents END DESC,
            p.id DESC
        """
    )
    fun getProductsBy(
        title: String?,
        maxPriceInCents: Long?,
        orderByPrice: String?,
        categoryNames: List<String>,
        categoryCount: Int
    ): PagingSource<Int, ProductDbo>
}
