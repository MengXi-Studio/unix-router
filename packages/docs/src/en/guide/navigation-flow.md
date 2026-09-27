# Navigation Flow Internals

Understand what happens during a single unix-router navigation, from trigger to completion. This chapter builds on the stage breakdown from the [Plugin System](./plugins) and gives the complete internal timeline.

## Full Forward Navigation Timeline (push / replace / relaunch)

Using `router.push(location)` as an example; `replace` / `relaunch` only swap in a different uni API:

1. **Concurrent queueing**: if a navigation is already in progress (`pendingNavigation`), wait for it to finish first (a failure also counts as finished), guaranteeing that only one navigation executes at any moment.
2. **PLUGIN_REQUIRED pre-check**: using `params` without `ParamsPlugin` registered, or using `events` without `EventsPlugin` registered, fails immediately with `PLUGIN_REQUIRED` — no later stage is ever entered.
3. **enrichLocation hooks**: before resolve, plugins **enrich the raw route location** (e.g. `ParamsPlugin` stores params into the manager and injects the `__params__` internal key; `EventsPlugin` injects the `__evt__` channel key).
4. **`matcher.resolve`**: resolves the enriched location into a full `RouteLocation` (i.e. `to`). In strict mode (the default), an unmatched named route throws `RouterError ROUTE_NOT_FOUND`.
5. **afterResolve hooks**: **extract plugin data** from the enriched location into `pluginData` (e.g. retrieving params by the `__params__` key).
6. **DUPLICATED detection**: only `push` performs it — when the target's path + query + params + hash are all identical to the current route, `DUPLICATED` is thrown (abort hooks run first to clean up). If any field differs, it counts as a new navigation; `replace` / `relaunch` skip this check and may re-enter the current page.
7. **`beforeEach`**: the global before guard.
8. **`beforeEnter`**: the per-route guard (if the target route defines one).
9. **`beforeResolve`**: the global resolve guard.
10. **Redirect handling**: when any of the guards above returns a redirect, `handleGuardResult` handles it recursively — for the redirect target it **rebuilds enrichLocation / afterResolve** (plugin data can be reused), then walks the guard chain from the start. The redirect mode defaults to the original navigation mode; the depth cap is `MAX_REDIRECT_DEPTH` (10), beyond which the result is `CANCELLED`.
11. **prepareNavigation hooks**: before the uni API call, plugins modify the navigation URL's query and options (`NavigationPrepareContext.query` / `options` are mutable).
12. **beforeNavigation hooks**: run right before the uni API is actually invoked, **may be async** and run **serially** (e.g. on H5, AnimationPlugin waits here for the exit animation to finish).
13. **Invoke the uni API**: dispatched by `meta.isTab` and the navigation mode — `push` → `navigateTo` / `switchTab`; `replace` → `redirectTo` / `switchTab`; `relaunch` → `reLaunch` / `switchTab` (tabBar pages carry no query).
14. **Poll the page stack top for confirmation**: the page stack (`getCurrentPages`) is the single source of truth; the router polls until the target page becomes the stack top. If it is not confirmed within the timeout (500ms) → `NAVIGATION_API_ERROR`.
15. **Strip internal keys**: removes `__params__` / `__evt__` and other plugin-internal keys from `to.query` and recomputes `fullPath`, never exposing them to users.
16. **Update state**: `setCurrentRoute` + `setGlobalCurrentRoute`; `useRoute()` / `currentRoute` update reactively (`onRouteChange` listeners are notified here).
17. **completeNavigation hooks**: plugins extend the navigation result (`ctx.result` is mutable).
18. **`afterEach(to, from, null)`**: the after guard runs.
19. **Return the target**: the Promise resolves with the target `RouteLocation` (`NavigationResult`, i.e. `to` after internal keys were stripped).

### Failure Path

If **any stage from step 3 through 14 fails** (a guard aborts / cancels, the uni API errors, stack-top confirmation times out, redirect depth exceeded):

```
onNavigationAbort (plugin cleanup, exceptions swallowed)
  → afterEach(to, from, failure)
  → onError(failure, to, from)
  → Promise reject (a failed navigation always rejects; it never resolves)
```

## The back Flow

`router.back(delta)` is shorter and **does not pass through the plugins' enrich / afterResolve** (pluginData is empty):

1. Concurrent queueing: wait for any in-progress navigation to finish.
2. Validate `delta`: defaults to `1` (passing `null` is also treated as 1); a non-positive integer → `ABORTED`; an insufficient page stack (stack length < 2 or `delta >= stack length`) → `CANCELLED`.
3. Compute the target page from the page stack and resolve it into `to`.
4. `beforeEach` → `beforeResolve` (guards may redirect; a redirect is handled as a real navigation).
5. `prepareNavigation` → `beforeNavigation` hooks (`mode: 'back'`, e.g. after playing the exit animation).
6. `uni.navigateBack(delta)` → `setCurrentRoute(to)` (router-internal state only — the `useRoute()` reactive object is not written) → `afterEach(to, from, null)` → resolve `to`.

## State Sync syncRoute

uni-app x's physical back button, edge swipe, tab switching, and similar behaviors **do not pass through the router**, so the route state can drift from the page stack. `syncRoute()` rebuilds the state from the page stack:

1. Resolve the page stack's top page (`history.resolveCurrent()`), producing path / query / params / meta, etc.
2. Run the **routeSync hooks**: plugins extract their data from the URL query (e.g. rebuilding params from the `__params__` key) and strip internal keys.
3. Recompute `fullPath`, construct a new `RouteLocation`, and update the **router-internal** `currentRoute` via `setCurrentRoute` — the `useRoute()` reactive object is not written (only forward navigations write it).

Sync is triggered when: `createRouter` initializes (page stack non-empty), the `onShow` mixin registered by `router.install()` (H5 only), and manual invocation (on native platforms it is recommended to call it yourself in each page's `onShow`).

## Cold Start guardRoute

When a user lands directly on a page (H5 direct URL, deeplink, QR scan), the page has already loaded but the guard chain never ran. `guardRoute(location?, { onAbort? })` **only re-runs the guard chain; it does not perform actual navigation**:

1. Resolve the target: if `location` is passed, resolve it; otherwise use the current `currentRoute`.
2. Run `beforeEach`:
   - **Allowed** → returns the target `RouteLocation`; the flow ends.
   - **Aborted** → fires `onError` + `onAbort(failure)` and rejects the Promise.
   - **Redirected** → performs a **real navigation** in the redirect mode (default `relaunch`, because on a cold start the page stack contains only the current page, making `push` / `replace` semantics unclear); after redirecting, the full navigation flow runs (guard chain included).

::: tip Typical usage together with guardRoute

```ts
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			// the page has already loaded and cannot be blocked; jump to a safe page
			router.relaunch({ name: 'home' })
		}
	}).catch(() => {})
})
```

:::

## Design Note: The Page Stack Is the Single Source of Truth

- **The uni API's success callback is not treated as proof of success**: a uni navigation API being accepted does not mean the target page has been pushed onto the stack. After invoking the uni API, the router **polls the page stack** and only records success once the target page is confirmed as the stack top; otherwise it fails with `NAVIGATION_API_ERROR`.
- **The router-internal `currentRoute` is always derived from the page stack**: what gets written after a successful forward navigation is the stack-top-confirmed target (both the internal state and the `useRoute()` reactive object); system behaviors like physical back and tab switching are re-aligned by `syncRoute()` in `onShow`. Note the two states are maintained independently — `useRoute()` is written by forward navigations alone, and `back()` / `syncRoute()` update the router-internal `currentRoute` only.

## Where Your Code Executes

| Your code | Which step |
| --- | --- |
| `routes` config | `matcher.resolve` decides `to` |
| Plugin `onEnrichLocation` / `onAfterResolve` | before the guard chain |
| `beforeEach` | after queueing, pre-check, resolve, and duplicate detection |
| `beforeEnter` | per-route before guard (after `beforeEach`) |
| `beforeResolve` | the last gate before the real navigation |
| Plugin `onPrepareNavigation` / `onBeforeNavigation` | before the uni API call (the latter may be async) |
| `uni.*` native navigation | internal dispatch (by `meta.isTab` and mode) |
| Plugin `onCompleteNavigation` / `afterEach` / `onRouteChange` | after stack-top confirmation + state update |
| Plugin `onNavigationAbort` | when any stage fails or aborts |
| `syncRoute` | `onShow` / initialization, rebuilding state from the page stack |

## Next Steps

- [Plugin System](./plugins) — the signature and registration of each stage's hooks
- [Navigation Guards](./guards) — guard return values and redirect semantics
- [Error Handling](./error-handling) — error codes and handling for the failure path
