# Architecture Decision Records — MODU

MODU evolved from its original REST-backed implementation, retained for the `remote` flavor, into a ready-to-run local `demo`. The network-oriented decisions below remain relevant because they explain source that is still part of the repository.

## 1. StateFlow over LiveData

The data and domain layers already used Kotlin coroutines and Flow, so ViewModels expose state with `StateFlow` rather than introducing LiveData and a second observation model. This keeps coroutine behavior consistent across layers and lets JVM tests use `kotlinx-coroutines-test` and `runTest` without Android-specific LiveData rules.

Unlike LiveData observation, collecting a `StateFlow` is not automatically tied to a Fragment view lifecycle. Fragments therefore collect state, events, and Paging streams through `viewLifecycleOwner` with `repeatOnLifecycle`, which starts and stops collection with the view's lifecycle.

## 2. Paging 3 over manual pagination

Paging 3 owns page keys, load sizes, refresh behavior, and UI load states instead of requiring custom pagination infrastructure. The original REST path uses a `PagingSource` to request server pages, while the current demo path uses a Room `PagingSource`; both expose the same stream to the shared presentation layer.

`PagingData` crosses the domain repository and use-case contracts as an AndroidX type. This couples those contracts to Paging, but keeps pagination and load-state behavior consistent from each data implementation through `PagingDataAdapter`.

## 3. Room persistence and device-scoped remote sync

The original REST cart was persisted in Room so it survived app restarts and remained observable while remote operations were in progress. The repository cached changes locally, marked pending synchronization in preferences, assigned negative IDs to items created offline, and reconciled those items with the device-scoped remote cart when network calls resumed.

The remote client sent `ANDROID_ID` in the `Authorization` header. That identifier scoped cart state to a device but did not authenticate a user, so the design did not provide account identity or cross-device cart ownership.

Retries stayed inside the active application flow. If requirements called for synchronization after process death or scheduling under connectivity and battery constraints, WorkManager would be the appropriate evolution.

## 4. Injectable ErrorHandler

Remote error mapping is centralized behind an injectable `ErrorHandler`. `DataErrorHandlerImpl` converts `IOException` to `NO_INTERNET`, parses structured HTTP error bodies into typed `AppError` values, applies status-based fallbacks for unauthorized, not-found, and server errors, and maps other exceptions to `UNKNOWN`.

This gives repositories and paging sources one mapping policy without forcing presentation code to understand Retrofit exceptions. The handler returns the mapped exception; each caller remains responsible for propagating it or deliberately handling it in its own flow.

## 5. Retain XML and ViewBinding

MODU's screens and navigation were already implemented with XML layouts, Fragments, RecyclerView, and ViewBinding. Rewriting them in Compose would have changed the presentation implementation without adding user-visible behavior, while requiring the existing state, event, navigation, and adapter flows to be revalidated.

The application therefore retains XML and ViewBinding, preserving established screen behavior and keeping the work focused on the catalog, cart, persistence, and flavor composition.

## 6. Flavor-specific dependency injection

The `demo` and `remote` flavors share presentation, domain contracts, and most data classes while Hilt selects the appropriate repository implementations. `demo` binds repositories backed by the packaged Room database and local cart storage, giving it runtime autonomy with no network permission.

`remote` binds the REST-backed repositories and adds its network graph, `BuildConfig.BASE_URL`, and `android.permission.INTERNET`. Network provisioning is therefore exclusive to that flavor even though common source retains Retrofit DTOs and repository code.

## 7. Versioned prepackaged Room catalog database

The demo catalog is distributed as a versioned SQLite database and opened by Room with `createFromAsset`, making the complete catalog available without runtime parsing or first-launch seeding. A dedicated JVM generator reads Room's exported schema as authoritative DDL, validates the normalized seed and image assets, writes rows in a transaction, verifies Room metadata and SQLite integrity, and publishes the database atomically.

Runtime JSON seeding was rejected because it would add parser, validation, insertion, and initialization state to the app. Instrumentation-based generation would require a device or emulator and extraction from app-private storage, while handwritten SQL would duplicate the Room schema and could drift from it.

Because the database is committed, it can become stale when the schema, seed, or images change. The fixed Gradle generation task declares those inputs and the database output; JVM tests regenerate from production inputs and compare the result with the committed artifact, and instrumentation tests verify that Room opens the packaged database.

## Related Documentation

- [Architecture](../architecture.md)
- [Demo catalog generation](../catalog-generation.md)
- [Historical REST integration](../historical-rest-integration.md)
