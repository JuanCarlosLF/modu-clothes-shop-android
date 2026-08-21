package com.example.modu.catalog.generator

import com.example.modu.catalog.generator.exception.CatalogSchemaParseException
import com.example.modu.catalog.generator.model.RoomSchema
import com.example.modu.catalog.generator.model.RoomSchemaExport
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path

internal class RoomSchemaParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(schemaPath: Path): RoomSchema = try {
        val export = json.decodeFromString<RoomSchemaExport>(Files.readString(schemaPath, UTF_8))
        export.validate()
        export.database
    } catch (error: Exception) {
        throw CatalogSchemaParseException(schemaPath, error)
    }

    private fun RoomSchemaExport.validate() {
        require(formatVersion == SUPPORTED_FORMAT_VERSION) {
            "formatVersion must be $SUPPORTED_FORMAT_VERSION"
        }
        require(database.views.isEmpty()) {
            "database.views must be empty because catalog database generation does not support Room views"
        }
        database.validate()
    }

    private fun RoomSchema.validate() {
        require(version > 0) { "database.version must be positive" }
        require(identityHash.isNotBlank()) { "database.identityHash must not be blank" }
        require(entities.isNotEmpty()) { "database.entities must not be empty" }

        val tableNames = linkedSetOf<String>()
        entities.forEachIndexed { entityIndex, entity ->
            require(entity.tableName.isNotBlank()) {
                "database.entities[$entityIndex].tableName must not be blank"
            }
            require(tableNames.add(entity.tableName.lowercase())) {
                "Duplicate Room table name: ${entity.tableName}"
            }
            require(entity.createSql.isNotBlank() && entity.createSql.contains(TABLE_NAME_PLACEHOLDER)) {
                "database.entities[$entityIndex].createSql must contain $TABLE_NAME_PLACEHOLDER"
            }
            entity.indices.forEachIndexed { index, roomIndex ->
                require(roomIndex.createSql.isNotBlank() && roomIndex.createSql.contains(TABLE_NAME_PLACEHOLDER)) {
                    "database.entities[$entityIndex].indices[$index].createSql must contain $TABLE_NAME_PLACEHOLDER"
                }
            }
        }
        setupQueries.forEachIndexed { index, query ->
            require(query.isNotBlank()) { "database.setupQueries[$index] must not be blank" }
        }
    }

    private companion object {
        const val SUPPORTED_FORMAT_VERSION = 1
        const val TABLE_NAME_PLACEHOLDER = "\${TABLE_NAME}"
    }
}
