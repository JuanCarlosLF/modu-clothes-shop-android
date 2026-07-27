package com.example.modu.catalog.generator

import com.example.modu.catalog.generator.exception.CatalogInputReadException
import com.example.modu.catalog.generator.exception.CatalogSeedParseException
import com.example.modu.catalog.generator.model.ParsedCatalog
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class CatalogSeedParser {

    private val json = Json

    fun parse(sourcePath: Path): ParsedCatalog = try {
        json.decodeFromString<ParsedCatalog>(Files.readString(sourcePath, UTF_8))
    } catch (error: SerializationException) {
        throw CatalogSeedParseException(sourcePath = sourcePath, cause = error)
    } catch (error: IOException) {
        throw CatalogInputReadException(inputPath = sourcePath, cause = error)
    } catch (error: SecurityException) {
        throw CatalogInputReadException(inputPath = sourcePath, cause = error)
    }
}
