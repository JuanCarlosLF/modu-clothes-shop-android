# MODU — Android Clothing Store

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Min%20SDK-28-green)](https://developer.android.com/about/versions/pie)
[![CI](https://github.com/JuanCarlosLF/modu-clothes-shop-android/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/JuanCarlosLF/modu-clothes-shop-android/actions/workflows/ci.yml)

MODU is a native Android clothing-store experience for discovering products, choosing the right variants, and managing a shopping cart through checkout. Browse a paginated catalog, narrow results with search and filters, inspect product details, and keep cart selections between sessions.

## Screenshots

<table role="presentation">
  <tr>
    <td><img src="screenshots/home-default.png" width="200" alt="Product catalog"/></td>
    <td><img src="screenshots/product-detail-1.png" width="200" alt="Product detail"/></td>
    <td><img src="screenshots/filter-default.png" width="200" alt="Catalog filters"/></td>
  </tr>
  <tr>
    <td><img src="screenshots/home-with-filters-applied.png" width="200" alt="Filtered catalog"/></td>
    <td><img src="screenshots/product-detail-2.png" width="200" alt="Product variant selection"/></td>
    <td><img src="screenshots/cart-default.png" width="200" alt="Shopping cart"/></td>
  </tr>
</table>

## Explore MODU

- Browse a paginated clothing catalog.
- Search products, apply filters, and sort results by price.
- Open detailed product pages and discover related products.
- Select product variants and quantities with stock validation.
- Add products to a persistent cart, update quantities, remove or restore items, and clear the cart.
- Complete a simulated checkout flow.

## Engineering Highlights

- Kotlin, coroutines, and Flow drive asynchronous work and reactive screen state.
- A layered architecture separates XML/ViewBinding presentation, domain use cases and contracts, and data implementations.
- Hilt composes the dependency graph and selects data implementations by product flavor.
- Room provides local persistence, including the bundled catalog and cart data used by the `demo` flavor.
- Paging 3 loads the catalog incrementally through repository and UI layers.
- JVM, instrumented, and catalog generator tests cover application behavior and deterministic catalog generation.

## Run Locally

### Prerequisites

- Android Studio with an Android SDK
- JDK 21 (Android Studio's bundled JBR is suitable)

Clone the repository:

```bash
git clone https://github.com/JuanCarlosLF/modu-clothes-shop-android.git
cd modu-clothes-shop-android
```

Open the `Project` directory in Android Studio. If needed, let Android Studio create `Project/local.properties` with the local Android SDK path. Select the `demoDebug` build variant, then run the `app` configuration on an emulator or Android device.

The `demo` flavor includes a versioned Room catalog and local image assets, providing a reproducible ready-to-run experience without additional services, credentials, API keys, runtime catalog or asset downloads, or runtime imports.

Signed portfolio builds are available from [GitHub Releases](https://github.com/JuanCarlosLF/modu-clothes-shop-android/releases). Release pull requests are validated before merge, and the resulting `demoRelease` APK is rebuilt, signature-checked, and published from the merged `main` commit.

## Verification

Run these commands from `Project`.

**Windows**

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDemoDebug assembleDemoRelease assembleRemoteDebug assembleRemoteRelease
.\gradlew.bat connectedDemoDebugAndroidTest
```

**Unix**

```bash
./gradlew test
./gradlew assembleDemoDebug assembleDemoRelease assembleRemoteDebug assembleRemoteRelease
./gradlew connectedDemoDebugAndroidTest
```

The connected test task requires an available emulator or device.

## Project Context

The `remote` flavor preserves MODU's historical REST integration for reference, while the ready-to-run `demo` flavor uses bundled local data. In the demo, checkout is simulated locally: it clears the cart and reports success without processing payments or creating production orders. Stock and price alert handling belongs to the historical remote flow.

## Technical Documentation

- [Current architecture](Project/docs/architecture.md)
- [Architecture decision records](Project/docs/adr/ADRs.md)
- [Demo catalog generation](Project/docs/catalog-generation.md)
- [Historical REST integration](Project/docs/historical-rest-integration.md)
