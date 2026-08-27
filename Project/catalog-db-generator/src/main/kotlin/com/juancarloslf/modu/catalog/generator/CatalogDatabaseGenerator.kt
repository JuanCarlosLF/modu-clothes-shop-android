package com.juancarloslf.modu.catalog.generator

import com.juancarloslf.modu.catalog.generator.validation.CatalogSeedValidator
import java.nio.file.Path

internal data class CatalogGenerationPaths(
    val schema: Path,
    val catalog: Path,
    val assets: Path,
    val output: Path
) {
    companion object {
        fun fromProjectRoot(projectRoot: Path): CatalogGenerationPaths = CatalogGenerationPaths(
            schema = projectRoot.resolve(
                "app/schemas/com.juancarloslf.modu.data.dataSource.local.demo.database.ModuDemoDatabase/1.json"
            ),
            catalog = projectRoot.resolve(
                "catalog-db-generator/src/main/resources/catalog/catalog-seed.json"
            ),
            assets = projectRoot.resolve("app/src/demo/assets"),
            output = projectRoot.resolve("app/src/demo/assets/database/modu_demo_database.db")
        )
    }
}

internal class CatalogDatabaseGenerator {

    fun generate(paths: CatalogGenerationPaths) {
        val catalog = CatalogSeedParser().parse(paths.catalog)
        CatalogSeedValidator().validate(catalog, paths.assets)
        val schema = RoomSchemaParser().parse(paths.schema)
        CatalogDatabaseWriter().write(schema, catalog, paths.output)
    }
}

public fun main() {
    val projectRoot = Path.of("").toAbsolutePath().normalize()
    CatalogDatabaseGenerator().generate(CatalogGenerationPaths.fromProjectRoot(projectRoot))
}
