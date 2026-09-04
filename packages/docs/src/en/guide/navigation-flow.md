# How the Navigation Flow Works

Understand what happens in a unix-router navigation from trigger to completion.

## Flowchart

```mermaid
flowchart TD
	A[router.push / replace / relaunch] --> B{Previous navigation?}
	B -- yes --> WAIT[Wait for completion]
	WAIT --> C
	B -- no --> C[matcher.resolve resolves target]
	C --> D{Duplicate navigation? push to same location}
	D -- yes --> DUP[Throw NAVIGATION_DUPLICATED]
	D -- no --> E[runBeforeEach global before guards]
	E --> F{Result?}
	F -- abort/cancel --> ABORT[Trigger onError + afterEach(fail)]
	F -- redirect --> REDIRECT{Depth exceeded?}
	REDIRECT -- yes --> CANCEL[Cancel navigation]
	REDIRECT -- no --> C
	F -- allowed --> G[runBeforeEnter route-local guard]
	G --> H[runBeforeResolve global resolve guards]
	H --> I[Call uni.* navigation API]
	I --> J{Success?}
	J -- failure --> APIERR[Rollback + trigger protection]
	J -- success --> K[Update currentRoute + runAfterEach]
```

## Execution Order (Forward Navigation push / replace / relaunch)

1. **Concurrent queuing**: waits for the previous navigation to complete.
2. **Resolve the target**: `matcher.resolve(location)` produces a `RouteLocation`.
3. **Duplicate detection**: `push` to the same location as the current one → `NAVIGATION_DUPLICATED`.
4. **Global before** `beforeEach`
5. **Route-local** `beforeEnter` (if configured)
6. **Global resolve** `beforeResolve`
7. **Native navigation**: dispatches `navigateTo/redirectTo/reLaunch/switchTab` based on `meta.isTab`
8. **Post after completion** `afterEach`, and update `currentRoute`

Any guard returning `false`/`Error`/a redirect will abort or reroute the navigation. The navigation is cancelled when the redirect depth exceeds `MAX_REDIRECT_DEPTH`.

## The back Flow

`router.back(delta)` is shorter:

1. Wait for the previous navigation to complete
2. Compute the target page from the page stack
3. `beforeEach` → `beforeResolve`
4. `uni.navigateBack(delta)`
5. Sync `currentRoute` and trigger `afterEach`

## State Sync syncRoute

uni-app x's physical back button, tab switches, and so on do **not pass through the router**. On `install`, a global mixin is injected that automatically calls `router.syncRoute()` on the page's `onShow`, rebuilding `currentRoute` from the page stack so that `useRoute()` always reflects the real page.

## Cold Start guardRoute

When a user enters a page directly (H5 URL / scene value / deeplink), the page is already loaded and the guards haven't run. `guardRoute()` re-runs the guard chain for the current route: if allowed it returns; if redirected it navigates; if aborted it triggers `onAbort`, which can redirect to a safe page.