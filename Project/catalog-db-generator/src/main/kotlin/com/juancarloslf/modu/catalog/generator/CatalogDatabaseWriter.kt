package com.juancarloslf.modu.catalog.generator

import com.juancarloslf.modu.catalog.generator.exception.CatalogDatabaseWriteException
import com.juancarloslf.modu.catalog.generator.model.ParsedCatalog
import com.juancarloslf.modu.catalog.generator.model.RoomSchema
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.sql.Connection
import java.sql.DriverManager

internal class CatalogDatabaseWriter {

    fun write(schema: RoomSchema, catalog: ParsedCatalog, outputPath: Path) {
        var tempPath: Path? = null

        try {
            val destination = outputPath.toAbsolutePath().normalize()
            val parent = requireNotNull(destination.parent) {
                "Catalog database output must have a parent directory: $destination"
            }
            val fileName = requireNotNull(destination.fileName).toString()
            require(fileName.isNotBlank()) { "Catalog database output must have a nonblank file name" }

            Files.createDirectories(parent)
            requireNoSidecars(destination)
            tempPath = Files.createTempFile(parent, "$fileName.", ".tmp")

            createDatabase(tempPath, schema, catalog)
            requireNoSidecars(tempPath)
            publishAtomically(tempPath, destination)
            tempPath = null
        } catch (error: Exception) {
            tempPath?.let { failedPath ->
                try {
                    deleteDatabaseFiles(failedPath)
                } catch (cleanupError: Exception) {
                    error.addSuppressed(cleanupError)
                }
            }
            throw CatalogDatabaseWriteException(outputPath, error)
        }
    }

    private fun publishAtomically(tempPath: Path, destination: Path) {
        try {
            Files.move(tempPath, destination, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (error: AtomicMoveNotSupportedException) {
            throw IllegalStateException("Atomic publication is not supported for $destination", error)
        } catch (error: IOException) {
            throw IllegalStateException("Atomic publication failed for $destination", error)
        }
    }

    private fun createDatabase(tempPath: Path, schema: RoomSchema, catalog: ParsedCatalog) {
        DriverManager.getConnection("jdbc:sqlite:${tempPath.toAbsolutePath()}").use { connection ->
            configureConnection(connection)
            connection.autoCommit = false
            try {
                createSchema(connection, schema)
                insertCatalog(connection, catalog)
                verifyDatabase(connection, schema, catalog)
                connection.commit()
            } catch (error: Exception) {
                try {
                    connection.rollback()
                } catch (rollbackError: Exception) {
                    error.addSuppressed(rollbackError)
                }
                throw error
            }
        }
    }

    private fun configureConnection(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA journal_mode=DELETE").use { result ->
                check(result.next() && result.getString(1).equals("delete", ignoreCase = true)) {
                    "SQLite did not accept journal_mode=DELETE"
                }
            }
            statement.execute("PRAGMA foreign_keys=ON")
            statement.executeQuery("PRAGMA foreign_keys").use { result ->
                check(result.next() && result.getInt(1) == 1) {
                    "SQLite foreign key enforcement is disabled"
                }
            }
        }
    }

    private fun createSchema(connection: Connection, schema: RoomSchema) {
        connection.createStatement().use { statement ->
            // SQL is trusted because it comes from the validated committed Room export.
            schema.entities.forEach { entity ->
                statement.execute(entity.createSql.forTable(entity.tableName))
            }
            schema.entities.forEach { entity ->
                entity.indices.forEach { index ->
                    statement.execute(index.createSql.forTable(entity.tableName))
                }
            }
            schema.setupQueries.forEach { query -> statement.execute(query) }
            statement.execute("PRAGMA user_version=${schema.version}")
        }
    }

    private fun insertCatalog(connection: Connection, catalog: ParsedCatalog) {
        connection.prepareStatement("INSERT INTO categories (id, name) VALUES (?, ?)").use { statement ->
            catalog.categories.sortedBy { it.id }.forEach { category ->
                statement.setInt(1, category.id)
                statement.setString(2, category.name)
                statement.executeUpdate()
            }
        }
        connection.prepareStatement(
            "INSERT INTO products (id, name, description, imageAssetPath, priceInCents, active) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
        ).use { statement ->
            catalog.products.sortedBy { it.id }.forEach { product ->
                statement.setInt(1, product.id)
                statement.setString(2, product.name)
                statement.setString(3, product.description)
                statement.setString(4, product.imageAssetPath)
                statement.setLong(5, product.priceInCents)
                statement.setInt(6, product.active.toSqlInteger())
                statement.executeUpdate()
            }
        }
        connection.prepareStatement(
            "INSERT INTO product_categories (productId, categoryId) VALUES (?, ?)"
        ).use { statement ->
            catalog.productCategories.sortedWith(compareBy({ it.productId }, { it.categoryId })).forEach { relation ->
                statement.setInt(1, relation.productId)
                statement.setInt(2, relation.categoryId)
                statement.executeUpdate()
            }
        }
        connection.prepareStatement(
            "INSERT INTO product_variants (id, variantCode, color, size, productId, stock, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ).use { statement ->
            catalog.productVariants.sortedBy { it.id }.forEach { variant ->
                statement.setInt(1, variant.id)
                statement.setString(2, variant.variantCode)
                statement.setString(3, variant.color)
                statement.setString(4, variant.size)
                statement.setInt(5, variant.productId)
                statement.setInt(6, variant.stock)
                statement.setInt(7, variant.active.toSqlInteger())
                statement.executeUpdate()
            }
        }
    }

    private fun verifyDatabase(connection: Connection, schema: RoomSchema, catalog: ParsedCatalog) {
        check(connection.queryLong("SELECT COUNT(*) FROM categories") == catalog.categories.size.toLong())
        check(connection.queryLong("SELECT COUNT(*) FROM products") == catalog.products.size.toLong())
        check(
            connection.queryLong("SELECT COUNT(*) FROM product_categories") == catalog.productCategories.size.toLong()
        )
        check(
            connection.queryLong("SELECT COUNT(*) FROM product_variants") == catalog.productVariants.size.toLong()
        )
        check(
            connection.queryString("SELECT identity_hash FROM room_master_table WHERE id = 42") == schema.identityHash
        ) { "Room identity hash does not match the schema" }

        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA foreign_key_check").use { result ->
                check(!result.next()) { "Catalog database contains foreign key violations" }
            }
            statement.executeQuery("PRAGMA integrity_check").use { result ->
                check(result.next() && result.getString(1) == "ok" && !result.next()) {
                    "SQLite integrity_check did not return exactly one 'ok' row"
                }
            }
        }
    }

    private fun Connection.queryLong(sql: String): Long = createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            check(result.next()) { "Query returned no rows: $sql" }
            result.getLong(1)
        }
    }

    private fun Connection.queryString(sql: String): String = createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            check(result.next()) { "Query returned no rows: $sql" }
            result.getString(1)
        }
    }

    private fun String.forTable(tableName: String): String = replace("\${TABLE_NAME}", tableName)

    private fun Boolean.toSqlInteger(): Int = if (this) 1 else 0

    private fun requireNoSidecars(databasePath: Path) {
        val sidecars = sidecarPaths(databasePath).filter { Files.exists(it) }
        check(sidecars.isEmpty()) { "SQLite sidecar files remain: ${sidecars.joinToString()}" }
    }

    private fun deleteDatabaseFiles(databasePath: Path) {
        sidecarPaths(databasePath).forEach { Files.deleteIfExists(it) }
        Files.deleteIfExists(databasePath)
    }

    private fun sidecarPaths(databasePath: Path): List<Path> = listOf(
        Path.of("$databasePath-wal"),
        Path.of("$databasePath-shm"),
        Path.of("$databasePath-journal")
    )
}
