package com.juancarloslf.modu.data.dataSource.local.demo.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juancarloslf.modu.di.DemoDatabaseModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val DEMO_DATABASE_NAME = "modu_demo_database"

@RunWith(AndroidJUnit4::class)
class PrepackagedCatalogDatabaseTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var database: ModuDemoDatabase? = null

    @Before
    fun setUp() {
        closeDatabase()
        context.deleteDatabase(DEMO_DATABASE_NAME)
    }

    @After
    fun tearDown() {
        closeDatabase()
        context.deleteDatabase(DEMO_DATABASE_NAME)
    }

    @Test
    fun provider_opensPrepackagedCatalogAndPreservesCartAcrossReopen() {
        database = DemoDatabaseModule.provideDemoDatabase(context)

        assertEquals(61L, countRows("products"))
        assertEquals(6L, countRows("categories"))
        assertEquals(80L, countRows("product_categories"))
        assertEquals(1_140L, countRows("product_variants"))
        assertEquals(0L, countRows("cart"))
        assertEquals(0L, countRows("cart_items"))

        requireNotNull(database).openHelper.writableDatabase.execSQL(
            "INSERT INTO cart (id, createdAt, updatedAt) VALUES (?, ?, ?)",
            arrayOf<Any?>(1, "2026-07-24T00:00:00Z", "2026-07-24T00:00:00Z")
        )

        closeDatabase()
        database = DemoDatabaseModule.provideDemoDatabase(context)

        assertEquals(1L, countRows("cart", "id = 1"))
    }

    private fun countRows(table: String, where: String? = null): Long {
        val query = buildString {
            append("SELECT COUNT(*) FROM ")
            append(table)
            if (where != null) append(" WHERE ").append(where)
        }
        return requireNotNull(database).openHelper.readableDatabase.query(query).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
    }

    private fun closeDatabase() {
        database?.close()
        database = null
    }
}
