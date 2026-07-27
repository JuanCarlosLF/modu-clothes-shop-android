package com.example.modu.catalog.generator.exception

import com.example.modu.catalog.generator.validation.CatalogSeedValidationReport
import java.nio.file.Path

internal sealed class CatalogGenerationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

internal class CatalogInputReadException(
    val inputPath: Path,
    cause: Throwable
) : CatalogGenerationException(
    message = "Unable to read catalog input: $inputPath",
    cause = cause
)

internal class CatalogSeedParseException(
    val sourcePath: Path,
    cause: Throwable
) : CatalogGenerationException(
    message = "Unable to parse catalog seed: $sourcePath",
    cause = cause
)

internal class CatalogSchemaParseException(
    val schemaPath: Path,
    cause: Throwable
) : CatalogGenerationException(
    message = "Unable to parse Room schema: $schemaPath",
    cause = cause
)

internal class CatalogSeedValidationException(
    val report: CatalogSeedValidationReport
) : CatalogGenerationException(message = report.format())
