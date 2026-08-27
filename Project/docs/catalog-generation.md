# Demo Catalog Generation

The `demo` flavor must expose its catalog immediately without network access or runtime seed machinery. Room therefore opens the versioned database at `app/src/demo/assets/database/modu_demo_database.db` with `createFromAsset`.

The generation pipeline has two distinct stages:

```text
catalog-source.json -> Python preparation -> normalized seed + images
Room schema + normalized seed + images -> JVM generator -> SQLite database
```

The Python stage is only needed when the catalog source changes. Regenerating the database from the existing normalized JSON does not require Python.

## Regenerate The Database

Run the fixed task from `Project/`:

```powershell
.\gradlew.bat :catalog-db-generator:generateDemoCatalogDatabase
```

The task takes no path arguments. It reads:

- `app/schemas/com.juancarloslf.modu.data.dataSource.local.demo.database.ModuDemoDatabase/1.json` as the authoritative Room DDL.
- `catalog-db-generator/src/main/resources/catalog/catalog-seed.json` as the normalized catalog rows.
- `app/src/demo/assets/catalog/images/` as the image asset tree validated by the seed.

It writes `app/src/demo/assets/database/modu_demo_database.db` transactionally and verifies row counts, Room metadata, foreign keys, SQLite integrity, and the absence of sidecar files before publication.

Run the generator tests after changing the schema, seed, images, or generator:

```powershell
.\gradlew.bat :catalog-db-generator:test
```

## Update The Catalog Source

The preparation script requires Python 3.10+ and the pinned dependencies:

```powershell
python -m pip install -r scripts/catalog/requirements.txt
```

Prepare the normalized seed and app images from `Project/`:

```powershell
python scripts/catalog/prepare_catalog.py prepare `
  --source scripts/catalog/catalog-source.json `
  --assets app/src/demo/assets/catalog/images `
  --seed catalog-db-generator/src/main/resources/catalog/catalog-seed.json `
  --reports scripts/catalog/reports
```

Review the normalized seed, prepared images, download report, and contact sheet. Then run the Gradle database generation task and its tests.

Run the Python regression suite when changing preparation behavior:

```powershell
python scripts/catalog/test_prepare_catalog.py
```

## Versioned Outputs

Commit changes to the catalog source, normalized seed, prepared app images, and generated database together. Do not commit `scripts/catalog/reports/`, Python caches, temporary downloads, SQLite sidecars, or `catalog-db-generator/build/`.

The architectural rationale and rejected alternatives are recorded in [ADR 7](adr/ADRs.md#7-versioned-prepackaged-room-catalog-database).
