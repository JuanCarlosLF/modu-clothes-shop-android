package com.example.modu.catalog.generator

import com.example.modu.catalog.generator.exception.CatalogSeedValidationException
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class CatalogDatabaseGeneratorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val generator = CatalogDatabaseGenerator()

    @Test
    fun fromProjectRoot_whenCalled_thenResolvesExactFixedPaths() {
        val root = Path.of("project-root")

        assertEquals(
            CatalogGenerationPaths(
                schema = root.resolve("app/schemas/com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase/1.json"),
                catalog = root.resolve("catalog-db-generator/src/main/resources/catalog/catalog-seed.json"),
                assets = root.resolve("app/src/demo/assets"),
                output = root.resolve("app/src/demo/assets/database/modu_demo_database.db")
            ),
            CatalogGenerationPaths.fromProjectRoot(root)
        )
    }

    @Test
    fun generate_givenValidInputs_whenCalled_thenCreatesDatabase() {
        val fixture = fixture("valid")

        generator.generate(fixture)

        DriverManager.getConnection("jdbc:sqlite:${fixture.output.toAbsolutePath()}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM products").use { result ->
                    check(result.next())
                    assertEquals(1, result.getInt(1))
                }
                statement.executeQuery("PRAGMA integrity_check").use { result ->
                    check(result.next())
                    assertEquals("ok", result.getString(1))
                }
            }
        }
    }

    @Test
    fun generate_givenMissingImageAndExistingOutput_whenCalled_thenPreservesOutput() {
        val fixture = fixture("invalid", createImage = false)
        val original = "existing output".toByteArray()
        Files.write(fixture.output, original)

        assertThrows(CatalogSeedValidationException::class.java) { generator.generate(fixture) }

        assertArrayEquals(original, Files.readAllBytes(fixture.output))
    }

    @Test
    fun generate_givenProductionInputs_whenCalled_thenCreatesExpectedCatalog() {
        val production = CatalogGenerationPaths.fromProjectRoot(projectRoot())
        val generated = temporaryFolder.newFolder("production").toPath().resolve("catalog.db")

        generator.generate(production.copy(output = generated))

        DriverManager.getConnection("jdbc:sqlite:${generated.toAbsolutePath()}").use { connection ->
            connection.createStatement().use { statement ->
                mapOf(
                    "products" to 61,
                    "categories" to 6,
                    "product_categories" to 80,
                    "product_variants" to 1_140
                ).forEach { (table, expected) ->
                    statement.executeQuery("SELECT COUNT(*) FROM $table").use { result ->
                        check(result.next())
                        assertEquals(expected, result.getInt(1))
                    }
                }
                statement.executeQuery("PRAGMA integrity_check").use { result ->
                    check(result.next())
                    assertEquals("ok", result.getString(1))
                }
            }
        }
    }

    private fun fixture(name: String, createImage: Boolean = true): CatalogGenerationPaths {
        val root = temporaryFolder.newFolder(name).toPath()
        val assets = Files.createDirectories(root.resolve("assets"))
        val image = assets.resolve("catalog/images/jacket.jpg")
        if (createImage) {
            Files.createDirectories(image.parent)
            Files.createFile(image)
        }
        val catalog = root.resolve("catalog.json")
        Files.writeString(catalog, validCatalogJson())
        return CatalogGenerationPaths(
            schema = roomSchemaPath(),
            catalog = catalog,
            assets = assets,
            output = root.resolve("catalog.db")
        )
    }

    private fun validCatalogJson() = """
        {
          "formatVersion": 1,
          "products": [{"id": 1, "name": "Jacket", "description": "Description", "imageAssetPath": "catalog/images/jacket.jpg", "priceInCents": 1299, "active": true}],
          "categories": [{"id": 1, "name": "Jackets"}],
          "productCategories": [{"productId": 1, "categoryId": 1}],
          "productVariants": [{"id": 1, "productId": 1, "variantCode": "jacket-s", "size": "S", "color": "BLACK", "stock": 3, "active": true}]
        }
    """.trimIndent()

    private fun projectRoot(): Path = roomSchemaPath().parent.parent.parent.parent

    private fun roomSchemaPath(): Path {
        val relative = Path.of(
            "app", "schemas",
            "com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase", "1.json"
        )
        var candidate: Path? = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
        while (candidate != null) {
            val path = candidate.resolve(relative)
            if (Files.isRegularFile(path)) return path
            candidate = candidate.parent
        }
        throw AssertionError("Could not find committed Room schema '$relative'")
    }
}
