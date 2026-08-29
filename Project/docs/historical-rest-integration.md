# Historical REST Integration

Source for MODU's original REST-backed implementation is retained for the `remote` flavor. This document records client behavior established from the Android source and tests; it is not a promise of an active service. The flavor supplied the network dependencies, and Retrofit used the endpoint configured through `BuildConfig.BASE_URL`.

## Client Flows

- **Catalog:** The client requested `GET products` with a zero-based `page`, a `size`, and optional title, price-order, maximum-price, and category filters. Paging 3 sent `params.loadSize` as `size`. A response wrapped product summaries in `data` and pagination information in `meta`; `meta.hasNext == true` advanced to `page + 1`, otherwise paging stopped. The metadata DTO also carried nullable `page` and `size`, but key calculation relied on the requested page and `hasNext`.
- **Product detail:** `GET products/{id}` returned a non-null `DetailDto` with nullable fields for identity, name, description, image, price, categories, and variants. `GET categories` returned the category list used by catalog filtering. These fields describe the data consumed by the Android client rather than a complete server data model.
- **Cart:** The API defined operations to read the cart, create it, update it as a whole, add an item, delete an item, clear the cart, and perform checkout. Add requests carried variant ID and quantity; update requests carried shipping cost and cart items. Cart responses contained totals and item identifiers, quantities, stock, and prices, either directly or inside a summary, with optional price, stock, and availability alerts.
- **Local persistence and synchronization:** Additions were cached locally first, and local quantity updates were marked for synchronization. New items received decreasing negative IDs. Deletion used the local pending path for negative-ID or already-pending items and fell back to local deletion on no internet; otherwise the client called remote delete for a synchronized positive-ID item. An online clear called the remote endpoint, while the no-internet fallback cleared local state and marked pending work. Reconciliation first sent positive-ID items in one update and then added negative-ID items individually; the resulting remote cart and alerts replaced local state. A generated-cart flag controlled remote cart creation. With no pending work, the client fetched the remote cart and saved it locally; a mapped not-found error reset the flag and cleared local state.
- **Checkout:** The request contained the paid flag, special instructions, shipping cost, and cart items to order. The response could report `orderPlaced`, order information, or an updated cart with alerts. The client cleared local cart state and reset the generated flag only when `orderPlaced == true`; otherwise it required a non-null `cartResponse`, saved it, and returned `ALERTS_TRIGGERED`. A null `cartResponse` in that branch caused an exception that was passed through error mapping. The client did not implement payment processing.

## Device Scope and Errors

The shared OkHttp interceptor added the stored `ANDROID_ID` directly to the `Authorization` header when available and omitted the header otherwise. Both product and cart APIs used that client. The identifier provided device scope, not user authentication.

`DataErrorHandlerImpl` mapped `IOException` to `NO_INTERNET`. For `HttpException`, it first attempted to parse the structured error body into a typed `AppError`. If parsing failed or produced `UNKNOWN`, HTTP 401, 404, and 5xx responses fell back to `UNAUTHORIZED`, `NOT_FOUND`, and `INTERNAL_ERROR`; other statuses remained `UNKNOWN`. Other exception types also became `UNKNOWN`.

## Representative Evidence

- [ProductApi.kt](../app/src/main/java/com/juancarloslf/modu/data/dataSource/remote/product/api/ProductApi.kt): catalog, detail, and category endpoint definitions.
- [ProductPagingSource.kt](../app/src/main/java/com/juancarloslf/modu/data/dataSource/remote/product/ProductPagingSource.kt): zero-based paging, requested load size, and `meta.hasNext` handling.
- [CartApi.kt](../app/src/main/java/com/juancarloslf/modu/data/dataSource/remote/cart/api/CartApi.kt): cart and checkout operations.
- [CartRepositoryImpl.kt](../app/src/main/java/com/juancarloslf/modu/data/repository/cart/CartRepositoryImpl.kt): local persistence, pending synchronization, reconciliation, alerts, and checkout handling.
- [DeviceIdInterceptor.kt](../app/src/main/java/com/juancarloslf/modu/data/dataSource/remote/interceptor/DeviceIdInterceptor.kt): conditional `Authorization` header behavior.
- [DataErrorHandlerImpl.kt](../app/src/main/java/com/juancarloslf/modu/data/dataSource/remote/exception/DataErrorHandlerImpl.kt): IO, HTTP, structured-body, and fallback error mapping.
