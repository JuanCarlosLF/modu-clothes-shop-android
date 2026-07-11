# Architecture Decision Records — MODU

## 1. StateFlow over LiveData

- **Status:** Accepted
- **Date:** 2026-07-11
- **Scope:** Presentation state and event delivery

The data and domain layers already used Kotlin coroutines and Flow. Extending that to the presentation layer with StateFlow kept the codebase in a single concurrency model instead of splitting between coroutines and LiveData observables.

LiveData is Android-specific, and `InstantTaskExecutorRule` is commonly used to make its updates synchronous in JVM tests. StateFlow works with standard `kotlinx-coroutines-test` and `runTest`, which means ViewModel tests use the same test infrastructure as repository and use case tests.

- **Decision:** Use StateFlow for ViewModel state exposure instead of LiveData.
- **Risk:** StateFlow collection is not automatically tied to a Fragment view lifecycle. Missing lifecycle-aware collection can keep work active outside the intended UI state, retain references longer than necessary, or deliver updates when the view is unavailable.
- **Mitigation:** All fragments in this project use `repeatOnLifecycle`. A code review or lint rule could enforce this, but neither is currently configured.

## 2. Paging 3 over manual pagination

- **Status:** Accepted
- **Date:** 2026-07-11
- **Scope:** Catalog pagination across Data, Domain, and Presentation

The current client contract uses `page`/`size` query parameters. Paging 3 provides `PagingSource`, which encapsulates page tracking and key management. `PagingDataAdapter` exposes `LoadState`, and the UI decides how to render loading and errors. End-to-end behavior against the completed backend remains target release work.

Manual pagination would require building and maintaining equivalent infrastructure, which is not the differentiating work of this project.

- **Decision:** Use Paging 3's `PagingSource` in the data layer and `PagingData` in the domain/UI layers.
- **Risk:** The domain layer depends on `PagingData` from `androidx.paging`, which contradicts the goal of a framework-independent domain layer. The project acknowledges this coupling rather than hiding it.
- **Mitigation:** Page-key and load-state mechanics are concentrated in `ProductPagingSource`, while the domain layer receives `PagingData` as an opaque stream. A backend pagination-contract change may also require updates to API definitions, DTOs, repository mapping, and tests. A future refactor could introduce a domain-level pagination model to remove the AndroidX dependency, at the cost of reimplementing load-state management.

## 3. Cart: Room persistence + device-scoped remote sync

- **Status:** Accepted
- **Date:** 2026-07-11
- **Scope:** Local cart persistence and client-side remote synchronization

The cart needed to survive app restarts, and the client was designed for CRUD endpoints scoped to a device identifier. Room provides local persistence with compile-time SQL validation. The client-side sync layer tracks pending changes via `CartPreferences` and contains reconciliation logic for local state, including items created offline with negative IDs. The current backend is a local FastAPI mock exposed by ephemeral ngrok; persistence and the complete cart contract are not confirmed.

- **Decision:** Persist cart in Room. Sync with remote backend using device ID as scope. Track pending changes to reconcile after connectivity loss.
- **Risk:** `CartRepositoryImpl` combines substantial merge, stale-data, alert, and mixed online/offline reconciliation responsibilities. Sync uses manual retry instead of WorkManager. Relevant logic has unit coverage, but checkout and sync have not been verified end to end against a complete backend.
- **Limitation:** `ANDROID_ID` provides device scoping, not authentication. It is currently sent in the `Authorization` header, whose authentication semantics do not match its use. If the final backend contract changes, an explicit `X-Device-Id` header with an application-generated identifier is preferable; that change is not implemented.
- **What would change in a larger project:** WorkManager for background retries, server-side cart creation, smaller testable sync primitives instead of one large reconciliation method.

## 4. Injectable ErrorHandler

- **Status:** Accepted; implementation incomplete
- **Date:** 2026-07-11
- **Scope:** Remote/data error mapping and propagation to UI state

Error mapping was handled inconsistently across call sites — some caught and converted IO/HTTP errors, others let exceptions propagate raw. Following the existing team convention, error mapping was centralized into an injectable `ErrorHandler` interface with a single implementation.

`DataErrorHandlerImpl` converts `IOException` → `NO_INTERNET`, `HttpException` → typed `AppError` (with structured JSON body parsing for backend error contracts), and unknown exceptions → `UNKNOWN`.

- **Decision:** Centralize IO/HTTP → `AppError` mapping in an injectable interface.
- **Risk:** The foundation is incomplete in current execution paths. `CartRepositoryImpl.fetchAndCacheMissingProducts()` calls `handle()` and discards the returned `AppError`; `ProductRepositoryImpl.getRelatedProducts()` bypasses the handler; and `DetailViewModel.loadRelatedProducts()` catches `Exception` and exposes `error.message`. Error propagation therefore cannot be described as complete or operational end to end.
- **Completion criterion:** Every remote call path either propagates `AppError` to a persistent UI state field or explicitly documents why the error is intentionally suppressed. Paging 3 errors are intercepted and mapped before reaching `LoadState`.

## 5. Compose migration excluded from scope

- **Status:** Accepted
- **Date:** 2026-07-11
- **Scope:** MODU release UI technology

The project started with XML and ViewBinding. Jetpack Compose is Google's recommended modern toolkit for new Android UI, while the Views toolkit remains supported. Migrating MODU's existing XML/ViewBinding screens would require presentation-layer rewrites without changing the user-visible behavior.

- **Decision:** Exclude Compose migration from MODU. Keep XML/ViewBinding for the final release.
- **Risk:** Keeping the supported Views toolkit means MODU does not demonstrate Compose, which some reviewers may expect from a modern Android project.
- **Mitigation:** The Presentation, Domain, and Data separation limits how much UI migration would affect business and data logic. ViewModels already expose StateFlow, but any future migration would still require validating UI state and event contracts rather than assuming no ViewModel changes.
