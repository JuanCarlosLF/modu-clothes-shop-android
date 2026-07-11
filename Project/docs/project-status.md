# MODU Project Status

This document separates verified behavior in the current Android repository from the release work that remains. It is the source of truth for project scope and limitations; the root README is intentionally a shorter technical overview.

## Verified In The Android Client

- Catalog UI with Paging 3, search, filters, and a staggered grid adapter.
- Product detail with variant selection, quantity validation, related products, and loading placeholders.
- Cart persistence in Room with add, quantity update, delete with undo, and clear operations.
- Client-side remote cart synchronization logic, including reconciliation of pending changes and locally created items.
- Simulated checkout request handling with stock and price alerts.
- Hilt dependency injection for network, database, data, and domain components.
- Central `ErrorHandler` foundation that maps IO and HTTP failures to domain `AppError` values.
- Gradle wrapper JAR restored and versioned (Gradle 9.1.0).
- 56 JVM unit tests across seven test files were run successfully locally on 2026-07-11.

## Current Limitations

- The FastAPI service is a local mock exposed through an ephemeral ngrok tunnel. It is not a stable deployment and its persistent catalog, cart, and checkout behavior is not verified end to end.
- The catalog and product detail require a network response. MODU is not an offline-first application; only the cart has local persistence and reconciliation logic.
- The cart is scoped to a device identifier. It has no accounts, authentication, or multi-device synchronization.
- `DeviceIdInterceptor` sends `ANDROID_ID` through the `Authorization` header. This is device routing, not authentication; the final backend contract should use a semantically explicit device header or application-generated identifier.
- Error mapping is not yet applied consistently. `CartRepositoryImpl.fetchAndCacheMissingProducts()` discards mapped errors, `ProductRepositoryImpl.getRelatedProducts()` bypasses the handler, and `DetailViewModel.loadRelatedProducts()` exposes a raw exception message.
- Image fallback behavior is inconsistent outside the home catalog.
- There is no CI workflow, release signing configuration, APK, version tag, or GitHub Release.
- `com.example.modu` remains a placeholder namespace.

## Release Scope

The intended portfolio release is a demonstrable Android client backed by a stable FastAPI deployment:

- Persistent demo data for catalog, categories, filters, pagination, product detail, and cart endpoints.
- Device-scoped cart endpoints for read, replace, patch, item creation, and deletion.
- Simulated checkout that validates availability, stock, and price without processing payment.
- Consistent typed error propagation and loading, empty, error, no-network, and image-fallback UI states.
- A stable backend URL configured without source edits.
- Automated Android and FastAPI tests, CI, an installable APK, and documented release evidence.

## Explicitly Out Of Scope

- User accounts, authentication, password recovery, and cross-device synchronization.
- Payments, production orders, and an administration dashboard.
- Full offline catalog caching.
- A Jetpack Compose migration or broad Gradle modularization.
- Play Store publication.

## Evidence To Add Before Release

- Run a clean-clone build and all JVM tests.
- Verify catalog, detail, cart, offline recovery, and simulated checkout against the deployed backend.
- Add CI evidence and release artifacts.
- Reconcile screenshots, README, ADRs, and this status document with the observed release behavior.
