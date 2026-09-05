# How the Navigation Flow Works

Understand what happens in a unix-router navigation from trigger to completion.

## Flowchart

```mermaid
flowchart TD
	A[router.push / replace / relaunch] --> B{Previous navigation?}
	B -- yes --> WAIT[Wait for completion]
	WAIT --> C
	B -- no --> C[matcher.resolve resolves target]
	C --> D{Duplicate navigation? push to a location identical in path+query+params+hash}
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
3. **Duplicate detection**: `push` to a location whose `path+query+params+hash` is identical to the current one → `NAVIGATION_DUPLICATED`. Any differing field (e.g. new params) counts as a new navigation, allowing re-entry into the current page.
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

## Where Your Code Runs

| Your code | Step |
| --- | --- |
| `matcher.resolve` resolution | determines `to` (your `routes` config) |
| `beforeEach` | global before (after queueing, resolution, duplicate check) |
| `beforeEnter` | route-local before (after `beforeEach`) |
| `beforeResolve` | the last gate before the real navigation |
| `uni.*` native navigation | internal scheduling (dispatched by `meta.isTab`) |
| `afterEach` / `onRouteChange` | after the navigation is committed |
| `syncRoute` | page `onShow`, rebuilds `currentRoute` from the page stack |

## Internal Scheduling at a Glance

A simple `push` roughly undergoes (pseudo-code):

```
push(location)
  → if there is a pendingNavigation, wait for it (concurrent queueing)
  → matcher.resolve(location)                 // produce to
  → push to an address identical in path+query+params+hash?  → throw DUPLICATED
  → runBeforeEach(to, from)                   // global before
  → runBeforeEnter(config, to, from)          // route-local
  → runBeforeResolve(to, from)                // global resolve
  → navigateTo / switchTab / ...              // the real navigation
  → update currentRoute + runAfter(to, from)  // commit + after
```

Key points:

- Before any real `uni.*` navigation, `to` has passed all guards.
- Any guard returning a non-"continue" result returns early (`abort` / `redirect`) and never reaches the native navigation.
- `afterEach` fires after `currentRoute` is updated; `onRouteChange` listeners are notified here too.