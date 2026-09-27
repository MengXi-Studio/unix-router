# Router Instance

The `Router` instance returned by `createRouter()` provides navigation, guards, and state queries. This page lists all of its members.

## Properties

### `currentRoute`

- Signature: `get currentRoute(): RouteLocation` (**read-only**)
- Description: the **router-internal** current route location (a shallow ref held by the router, read-only). Updated when a forward navigation completes (after stack-top confirmation) and when `back()` / `syncRoute()` re-align the internal state. It is maintained independently of [useRoute()](./use-route) — the latter is a separate global reactive object written by forward navigations only.

```ts
console.log(router.currentRoute.path) // /pages/index/index
```

## Navigation Methods

All navigation methods return `Promise<NavigationResult>` (`NavigationResult` is the target [RouteLocation](./type-route-location); resolved on success, rejected on failure). Concurrent navigations are queued automatically; a failed navigation always rejects (see [Error Handling](../guide/error-handling)).

### `push(location)`

- Signature: `push(location: RouteLocationRaw): Promise<NavigationResult>`
- Description: navigates to a new page, mapping to `uni.navigateTo`; when the target is a `meta.isTab` page it automatically uses `uni.switchTab` (no query carried). Pushing to an address identical to the current route rejects with `DUPLICATED`.

```ts
const to = await router.push({ name: 'detail', query: new Map<string, string>([['id', '1']]) })
```

### `replace(location)`

- Signature: `replace(location: RouteLocationRaw): Promise<NavigationResult>`
- Description: replaces the current page, mapping to `uni.redirectTo`; when the target is a tabBar page it automatically uses `uni.switchTab`. No duplicate-navigation detection.

### `relaunch(location)`

- Signature: `relaunch(location: RouteLocationRaw): Promise<NavigationResult>`
- Description: closes all pages and opens the target page, mapping to `uni.reLaunch`; when the target is a tabBar page it automatically uses `uni.switchTab`.

### `back(delta?)`

- Signature: `back(delta?: number | null): Promise<NavigationResult>`
- Description: goes back one or multiple pages, mapping to `uni.navigateBack`. `delta` defaults to `1` (passing `null` is also treated as `1`):
  - a non-positive integer → rejects `ABORTED`;
  - an insufficient page stack (stack length < 2 or `delta >= stack length`) → rejects `CANCELLED`.
- Before returning it only runs the `beforeEach` + `beforeResolve` guard chain; it does not pass through the plugins' enrich / afterResolve.

```ts
await router.push({ name: 'about' })
await router.back()      // back one page
await router.back(2)     // back two pages
```

## Guard Registration

The three guard registration methods all **return an unregister function**. Return-value semantics: `null | true` allows; `false` → `ABORTED`; `Error` → `CANCELLED`; a string or object location → redirect; `{ location, mode? }` → `NavigationRedirect`.

| Method | Signature | Description |
| --- | --- | --- |
| `beforeEach(guard)` | `(guard: NavigationGuard) => () => void` | Global before guard, the first to run after the navigation is queued |
| `beforeResolve(guard)` | `(guard: NavigationGuard) => () => void` | Global resolve guard, the last gate before the uni API is actually invoked; in-component guards are implemented by filtering on it |
| `afterEach(guard)` | `(guard: PostNavigationGuard) => () => void` | Global after guard, signature `(to, from, failure: Error \| null) => void`, fired after the navigation completes or fails |

```ts
const off = router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login' }
	}
	return true
})
// remove when needed
off()
```

## State and Utility Methods

### `getRoutes()`

- Signature: `getRoutes(): RouteConfig[]`
- Description: returns all registered route configs (a shallow copy).

### `hasRoute(name)`

- Signature: `hasRoute(name: string): boolean`
- Description: checks whether a route with the given name exists.

### `resolve(location)`

- Signature: `resolve(location: RouteLocationRaw): RouteLocation`
- Description: resolves a route location into a full `RouteLocation`, **without navigating**. For a named location: under `strict` (default) an unregistered name throws `RouterError ROUTE_NOT_FOUND`; under `strict: false` it only warns and falls back to handling the name as a path. An invalid location (neither `name` nor `path`) always throws `ROUTE_NOT_FOUND`.

```ts
const to = router.resolve({ name: 'detail' })
console.log(to.path) // /pages/detail/detail
```

### `isReady()`

- Signature: `isReady(): Promise<void>`
- Description: waits for the router to finish initializing (`app.use(router)` marks it ready; constrained by the timeout when `readyTimeout` is configured).

### `onError(handler)`

- Signature: `onError(handler: (error: Error, to: RouteLocation, from: RouteLocation) => void): () => void`
- Description: registers a navigation error callback, fired when a navigation fails (including guard aborts, API failures, and resolve failures); **returns an unregister function**.

```ts
const offError = router.onError((error, to, from) => {
	console.error('Navigation failed:', error.message)
})
offError() // cancel
```

### `onRouteChange(listener)`

- Signature: `onRouteChange(listener: (to: RouteLocation, from: RouteLocation) => void): () => void`
- Description: registers a route-change listener, fired when a navigation completes or state syncs; **returns an unregister function**.

```ts
router.onRouteChange((to, from) => {
	console.log('Route changed:', from.path, '→', to.path)
})
```

### `syncRoute()`

- Signature: `syncRoute(): void`
- Description: syncs the page stack (`getCurrentPages`) state into the **router-internal** `currentRoute` (the `useRoute()` reactive object is not written). On H5, `app.use(router)` registers an `onShow` mixin that syncs automatically; **on native platforms it is recommended to call it yourself in each page's `onShow`**.

### `guardRoute(location?, options?)`

- Signature: `guardRoute(location?: RouteLocationRaw, options?: GuardRouteOptions): Promise<RouteLocation>`
- Description: re-runs the **global `beforeEach` only** for a given route (cold-start scenarios such as H5 direct URLs / deeplinks) — it does not run `beforeEnter` / `beforeResolve` / `afterEach` again — **without performing actual navigation**. If the guard allows, it resolves with the target location; if it aborts, it fires `options.onAbort(failure)` and rejects; if it redirects, a **real navigation** is performed in the redirect mode (default `relaunch`).

```ts
router.isReady().then(() => {
	const launchPath = options?.path != null ? `/${options.path}` : undefined
	router.guardRoute(launchPath, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' }) // the page has loaded and cannot be blocked; jump to a safe page
		}
	}).catch(() => {})
})
```

### `install(app)`

- Signature: `install(app: any): void`
- Description: installs the router into the Vue app instance, usually invoked by `app.use(router)`. **On H5 only**, it registers `provide` (for `useRouter` setup injection), mounts the `$router` / `$route` global properties, and registers the `onShow` global mixin (automatic `syncRoute()`); **on native platforms** it registers the global active router (for the non-setup context fallback) and triggers the plugins' app-level hook.

## Related APIs

- [createRouter()](./create-router)
- [useRouter()](./use-router)
- [NavigationGuard](./type-navigation-guard)
