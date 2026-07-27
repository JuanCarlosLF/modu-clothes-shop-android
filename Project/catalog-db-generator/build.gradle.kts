plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}

tasks.register<JavaExec>("generateDemoCatalogDatabase") {
    group = "catalog"
    description = "Generates the demo catalog database from the schema, seed, and catalog images."
    mainClass.set("com.example.modu.catalog.generator.CatalogDatabaseGeneratorKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir(rootProject.projectDir)

    inputs.file(rootProject.file("app/schemas/com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase/1.json"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootProject.file("catalog-db-generator/src/main/resources/catalog/catalog-seed.json"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir(rootProject.file("app/src/demo/assets/catalog/images"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(rootProject.file("app/src/demo/assets/database/modu_demo_database.db"))
}
