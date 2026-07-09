# Architecture Decision Records — MODU

## 1. StateFlow over LiveData

MODU was already using Kotlin coroutines and Flow in the data and domain layers, so StateFlow made sense for the UI too. LiveData is Android-specific — it works fine, but it brings its own test headaches with `InstantTaskExecutorRule` and doesn't play well with the rest of the coroutine ecosystem. StateFlow lets me test ViewModels with standard `kotlinx-coroutines-test` and keeps everything in one mental model.

The downside is no automatic lifecycle handling. You have to collect in `repeatOnLifecycle` or `collectAsStateWithLifecycle` yourself. It's a small price for not dragging Android framework dependencies into the presentation layer.

- **Why:** Kotlin-native, easier testing, consistent with the rest of the codebase.
- **Trade-off:** No built-in lifecycle awareness — you handle subscription timing manually.
- **What I'd do differently:** Nothing, honestly. This one was straightforward.

## 2. Paging 3 over manual pagination

The backend had a `page`/`size` API and I needed to load products as the user scrolls. I could've written my own page tracking, loading states, and retry logic — but that's a lot of boilerplate that doesn't add value. Paging 3 gives you a `PagingSource` that encapsulates all the key/page mechanics, and the domain layer receives clean `PagingData` without knowing how pagination works underneath.

The trade-off is that the domain layer now depends on `PagingData` from AndroidX. Not ideal from a purity standpoint, but the alternative was reimplementing pagination infrastructure from scratch. The data layer owns the complexity, the UI just consumes.

- **Why:** `PagingSource` keeps pagination details in the data layer. Less boilerplate.
- **Trade-off:** Domain layer depends on AndroidX `PagingData` types.
- **What I'd do differently:** Maybe revisit if the backend pagination contract changes significantly — but so far it's held up.

## 3. Offline-first cart sync

The API contracts and object naming changed constantly during development — I'd implement a flow against one contract and wake up to renamed fields and restructured responses. That churn was painful for integration, but it wasn't the reason for going offline-first. The real motivation was forward-looking: when real auth and multi-device support arrive, users should be able to browse and add to cart without a connection, even if sync isn't available yet.

Room persists cart items locally. Pending changes (add, update, remove) are tracked and synced when connectivity resumes. On reconnect, the system merges remote and local state and surfaces price or stock alerts from the backend. It was my first time building sync logic like this. In hindsight, `CartRepositoryImpl` is 366 lines — there's real complexity there, especially around merge conflicts and stale data. WorkManager would've been cleaner for background retries, but time was tight and I needed something working.

- **Why:** Forward-looking design — offline-first means the app stays useful when multi-device auth arrives, not just as a workaround for backend churn.
- **Trade-off:** Significant sync complexity (merging, stale data, alerts). Manual retry instead of WorkManager.
- **What I'd do differently:** Use WorkManager for background retries next time. Move cart creation to the server side. Handle the merge logic in smaller, testable pieces.

## 4. Injectable ErrorHandler

Error mapping was scattered — I was catching and converting IO/HTTP errors at each call site, and the behavior was inconsistent. The team convention was an injectable interface, so I followed that. `DataErrorHandlerImpl` centralizes the mapping: IO errors, HTTP codes, backend errors all go through one place and come out as domain-level `AppError` types.

It adds an abstraction layer — every error path goes through the interface instead of being handled locally. That might feel like overkill for a smaller project, but it paid off when I needed consistent error behavior across repositories and ViewModels. Easy to mock in tests too.

- **Why:** Consistent error handling. Follows team convention. Easy to test with Hilt injection.
- **Trade-off:** Extra abstraction layer for every error path.
- **What I'd do differently:** Nothing — this was a clean win.

## 5. Planned Compose migration

The project started with XML/ViewBinding because that was the project convention at the time. It wasn't a long-term preference — just what was mandated. Compose is the current Android standard and fits better with state-driven UI. The migration won't be very difficult because the projecto implementation of the clean architecture already separates UI from logic — ViewModels expose reactive state via StateFlow, and Compose consumes StateFlow natively. No ViewModels need to be touched; the presentation layer is the only thing that changes. The plan is to build the Compose layer in parallel with the existing XML layer, then remove XML entirely once Compose is feature-complete.

I haven't started this yet. The existing UI works and has no critical bugs, so there's no rush. The goal is a clean Compose-only codebase, not a hybrid one — no ComposeView interop, no incremental screen-by-screen migration. New screens will default to Compose, and once every screen is covered, the XML layer gets deleted in one clean cut.

- **Why:** Compose is the current standard. Better for reusable components and state-driven UI.
- **Trade-off:** Two parallel UI systems during the transition period. Rewriting all screens takes time.
- **What I'd do differently:** Nothing — parallel build then clean replace avoids the maintenance nightmare of hybrid UI layers.
