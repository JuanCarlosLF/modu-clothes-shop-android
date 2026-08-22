# Historical REST Integration

MODU was originally developed in collaboration with a backend team. That backend is no longer available, so the project now includes a fully local variant. This document explains how the historical REST integration was designed and how the Android client used it. It describes behavior verified in source and unit tests, not a currently operational service contract. Backend semantics, persistence, validation, and end-to-end compatibility cannot be verified today. The `remote` flavor provides the network dependencies, and Retrofit uses the endpoint configured through `BuildConfig.BASE_URL`.

## Client Flows

- **Catalog:** the client requested `GET products` with zero-based `page`, a `size`, and optional title, price-order, maximum-price, and category filters. Paging 3 sends `params.loadSize` as `size`. A response wrapped product summaries in `data` and pagination information in `meta`; `meta.hasNext == true` advanced to `page + 1`, otherwise paging stopped. The metadata DTO also carried nullable `page` and `size`, but client key calculation relied on the requested page and `hasNext`.
- **Product detail:** `GET products/{id}` returned a non-null `DetailDto` with nullable fields for identity, name, description, image, price, categories, and variants. `GET categories` returned the category list used by catalog filtering. These fields reflect the data consumed by the Android client; they do not document the backend's complete data model.
- **Cart:** the API exposed operations to read the cart, create it, update it as a whole, add an item, delete an item, clear the cart, and perform checkout. At a high level, add requests carried variant ID and quantity; update requests carried shipping cost and cart items. Cart responses contained totals and item identifiers, quantities, stock, and prices, either directly or inside a summary, with optional price, stock, and availability alerts.
- **Local persistence and synchronization:** additions were cached locally first, and local quantity updates marked pending sync. New items received decreasing negative IDs. Deletion used the local pending path for negative-ID or already-pending items and fell back locally on no internet; otherwise the client called remote delete directly for a synchronized positive-ID item. An online clear called the remote endpoint; on no internet, the fallback cleared local state and marked pending. Reconciliation first sent positive-ID items in one update, then added negative-ID items individually; the resulting server cart and alerts replaced local state. A generated-cart flag controlled server creation. With no pending work, the client fetched the remote cart and saved it locally; a mapped not-found error reset the flag and cleared local state.
- **Checkout:** the request contained the paid flag, special instructions, shipping cost, and cart items to order. The response could report `orderPlaced`, order information, or an updated cart with alerts. The client cleared local cart state and reset the generated flag only when `orderPlaced == true`; otherwise it required a non-null `cartResponse`, saved it, and returned `ALERTS_TRIGGERED`. The client operation threw when it encountered a null `cartResponse` in that branch, and the exception was error-mapped. No payment processing or production order behavior is established by this client code.

## Device Scope And Errors

The shared OkHttp interceptor added the stored Android ID directly to the `Authorization` header when non-null and omitted the header otherwise. Both product and cart APIs used that client.

`DataErrorHandlerImpl` maps `IOException` to `NO_INTERNET`. For `HttpException`, it first attempts to parse the structured error body into a typed `AppError`. If parsing fails or the body maps to `UNKNOWN`, HTTP 401, 404, and 5xx responses fall back to `UNAUTHORIZED`, `NOT_FOUND`, and `INTERNAL_ERROR`, respectively; other such statuses remain `UNKNOWN`. Other exception types also become `UNKNOWN`.

## Representative Evidence

- [ProductApi.kt](../app/src/main/java/com/example/modu/data/dataSource/remote/product/api/ProductApi.kt): catalog, detail, and category endpoint definitions.
- [ProductPagingSource.kt](../app/src/main/java/com/example/modu/data/dataSource/remote/product/ProductPagingSource.kt): zero-based paging, requested load size, and `meta.hasNext` handling.
- [CartApi.kt](../app/src/main/java/com/example/modu/data/dataSource/remote/cart/api/CartApi.kt): historical cart and checkout operations.
- [CartRepositoryImpl.kt](../app/src/main/java/com/example/modu/data/repository/cart/CartRepositoryImpl.kt): local persistence, pending synchronization, reconciliation, alerts, and checkout handling.
- [DeviceIdInterceptor.kt](../app/src/main/java/com/example/modu/data/dataSource/remote/interceptor/DeviceIdInterceptor.kt): conditional `Authorization` header behavior.
- [DataErrorHandlerImpl.kt](../app/src/main/java/com/example/modu/data/dataSource/remote/exception/DataErrorHandlerImpl.kt): IO, HTTP, backend-body, and fallback error mapping.
