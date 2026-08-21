package com.example.modu.catalog.generator

import com.example.modu.catalog.generator.exception.CatalogDatabaseWriteException
import com.example.modu.catalog.generator.model.CategoryRow
import com.example.modu.catalog.generator.model.ParsedCatalog
import com.example.modu.catalog.generator.model.ProductCategoryRow
import com.example.modu.catalog.generator.model.ProductRow
import com.example.modu.catalog.generator.model.ProductVariantRow
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager

internal class CatalogDatabaseWriterTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val schema by lazy { RoomSchemaParser().parse(roomSchemaPath()) }
    private val writer = CatalogDatabaseWriter()

    @Test
    fun write_givenValidInput_whenCalled_thenCreatesRoomCompatibleDatabaseWithoutSidecars() {
        val output = outputPath("valid")

        writer.write(schema, catalog(), output)

        openDatabase(output).use { connection ->
            assertEquals(1, connection.queryLong("PRAGMA user_version"))
            assertEquals(schema.identityHash, connection.queryString("SELECT identity_hash FROM room_master_table"))
            assertEquals(1, connection.queryLong("SELECT COUNT(*) FROM products"))
            assertEquals("Jacket", connection.queryString("SELECT name FROM products WHERE id = 101"))
            assertEquals(9_223_372_036L, connection.queryLong("SELECT priceInCents FROM products WHERE id = 101"))
            assertEquals("ok", connection.queryString("PRAGMA integrity_check"))
        }
        assertEquals(listOf(output), Files.list(output.parent).use { it.toList() })
        assertFalse(Files.exists(Path.of("$output-wal")))
        assertFalse(Files.exists(Path.of("$output-shm")))
        assertFalse(Files.exists(Path.of("$output-journal")))
    }

    @Test
    fun write_givenSameRowsInDifferentOrders_whenCalled_thenProducesSameBytes() {
        val first = outputPath("first")
        val second = outputPath("second")
        val canonical = multiRowCatalog()

        writer.write(schema, canonical, first)
        writer.write(
            schema,
            canonical.copy(
                products = canonical.products.reversed(),
                categories = canonical.categories.reversed(),
                productCategories = canonical.productCategories.reversed(),
                productVariants = canonical.productVariants.reversed()
            ),
            second
        )

        assertArrayEquals(sha256(first), sha256(second))
    }

    @Test
    fun write_givenInvalidRegeneration_whenCalled_thenPreservesExistingOutputAndCleansTemporaryFiles() {
        val output = outputPath("failed-regeneration")
        writer.write(schema, catalog(), output)
        val originalBytes = Files.readAllBytes(output)
        val invalid = catalog().copy(productCategories = listOf(ProductCategoryRow(101, 999)))

        assertThrows(CatalogDatabaseWriteException::class.java) {
            writer.write(schema, invalid, output)
        }

        assertArrayEquals(originalBytes, Files.readAllBytes(output))
        assertEquals(listOf(output), Files.list(output.parent).use { it.toList() })
    }

    private fun catalog() = ParsedCatalog(
        formatVersion = 1,
        products = listOf(ProductRow(101, "Jacket", "Description", "catalog/images/jacket.jpg", 9_223_372_036L, true)),
        categories = listOf(CategoryRow(201, "Outerwear")),
        productCategories = listOf(ProductCategoryRow(101, 201)),
        productVariants = listOf(ProductVariantRow(301, 101, "jacket-m", "M", "BLUE", 17, true))
    )

    private fun multiRowCatalog(): ParsedCatalog {
        val first = catalog()
        return first.copy(
            products = first.products + ProductRow(102, "Coat", "Description", "catalog/images/coat.jpg", 12_345L, false),
            categories = first.categories + CategoryRow(202, "Coats"),
            productCategories = first.productCategories + ProductCategoryRow(102, 202),
            productVariants = first.productVariants + ProductVariantRow(302, 102, "coat-l", "L", "BLACK", 3, true)
        )
    }

    private fun outputPath(directory: String): Path =
        temporaryFolder.newFolder(directory).toPath().resolve("catalog.db")

    private fun openDatabase(path: Path): Connection =
        DriverManager.getConnection("jdbc:sqlite:${path.toAbsolutePath()}")

    private fun Connection.queryLong(sql: String): Long = createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            check(result.next())
            result.getLong(1)
        }
    }

    private fun Connection.queryString(sql: String): String = createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            check(result.next())
            result.getString(1)
        }
    }

    private fun sha256(path: Path): ByteArray = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))

    private fun roomSchemaPath(): Path {
        val relativePath = Path.of(
            "app",
            "schemas",
            "com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase",
            "1.json"
        )
        var candidate: Path? = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
        while (candidate != null) {
            val path = candidate.resolve(relativePath)
            if (Files.isRegularFile(path)) return path
            candidate = candidate.parent
        }
        throw AssertionError("Could not find committed Room schema '$relativePath'")
    }
}
