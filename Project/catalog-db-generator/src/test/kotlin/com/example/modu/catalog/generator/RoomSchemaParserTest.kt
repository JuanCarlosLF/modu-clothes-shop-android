package com.example.modu.catalog.generator

import com.example.modu.catalog.generator.exception.CatalogSchemaParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path

internal class RoomSchemaParserTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val parser = RoomSchemaParser()

    @Test
    fun parse_givenRoomExport_whenCalled_thenReadsConsumedContract() {
        val schema = parser.parse(roomSchemaPath())

        assertEquals(1, schema.version)
        assertEquals("7bcd11b731409bb41466ba661b8223eb", schema.identityHash)
        assertEquals(
            setOf("products", "categories", "product_variants", "product_categories", "cart", "cart_items"),
            schema.entities.map { it.tableName }.toSet()
        )
        assertEquals(6, schema.entities.flatMap { it.indices }.size)
        assertTrue(schema.entities.all { it.createSql.contains("`${'$'}{TABLE_NAME}`") })
        assertEquals(2, schema.setupQueries.size)
    }

    @Test
    fun parse_givenUnknownFields_whenCalled_thenAllowsRoomExportEvolution() {
        val evolved = Files.readString(roomSchemaPath())
            .replaceFirst("{", "{\n  \"futureExportMetadata\": {\"revision\": 2},")
            .replaceFirst(
                "\"tableName\": \"products\",",
                "\"tableName\": \"products\",\n        \"futureEntityMetadata\": true,"
            )

        val schema = parser.parse(writeFixture("evolved.json", evolved))

        assertEquals(1, schema.version)
        assertEquals(6, schema.entities.size)
    }

    @Test
    fun parse_givenMissingConsumedField_whenCalled_thenThrowsSchemaParseException() {
        val malformed = Files.readString(roomSchemaPath()).replace(
            Regex("""(?m)^\s*"identityHash"\s*:\s*"[^"]+",\R?"""),
            ""
        )

        assertThrows(CatalogSchemaParseException::class.java) {
            parser.parse(writeFixture("missing-identity.json", malformed))
        }
    }

    @Test
    fun parse_givenUnsupportedExportFeatures_whenCalled_thenThrowsSchemaParseException() {
        val source = Files.readString(roomSchemaPath())
        val malformedExports = listOf(
            source.replaceFirst("\"formatVersion\": 1", "\"formatVersion\": 2"),
            source.replaceFirst(
                "\"setupQueries\": [",
                "\"views\": [{\"viewName\": \"names\", " +
                    "\"createSql\": \"CREATE VIEW `${'$'}{VIEW_NAME}` AS SELECT name FROM products\"}],\n" +
                    "    \"setupQueries\": ["
            )
        )

        malformedExports.forEachIndexed { index, malformed ->
            assertThrows(CatalogSchemaParseException::class.java) {
                parser.parse(writeFixture("unsupported-$index.json", malformed))
            }
        }
    }

    @Test
    fun parse_givenInvalidDatabaseContract_whenCalled_thenThrowsSchemaParseException() {
        val source = Files.readString(roomSchemaPath())
        val malformedExports = listOf(
            source.replaceFirst("\"version\": 1,", "\"version\": 0,"),
            source.replace(
                Regex("""(?s)"entities"\s*:\s*\[.*?]\s*,\s*"setupQueries"""),
                "\"entities\": [],\n    \"setupQueries\""
            ),
            source.replaceFirst("\${TABLE_NAME}", "products"),
            source.replaceFirst("\"tableName\": \"categories\"", "\"tableName\": \"PRODUCTS\"")
        )

        malformedExports.forEachIndexed { index, malformed ->
            assertThrows(CatalogSchemaParseException::class.java) {
                parser.parse(writeFixture("invalid-$index.json", malformed))
            }
        }
    }

    private fun writeFixture(name: String, content: String): Path =
        Files.writeString(temporaryFolder.newFile(name).toPath(), content)

    private fun roomSchemaPath(): Path {
        val relativePath = Path.of(
            "app",
            "schemas",
            "com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase",
            "1.json"
        )
        var candidate: Path? = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
        while (candidate != null) {
            val schemaPath = candidate.resolve(relativePath)
            if (Files.isRegularFile(schemaPath)) return schemaPath
            candidate = candidate.parent
        }
        throw AssertionError("Could not find committed Room schema '$relativePath'")
    }
}
