# Router Instance

The `Router` instance returned by `createRouter()` provides navigation, guards, and state querying. This page lists all its members.

## Properties

### `currentRoute`

- Type: `RouteLocation`
- Description: The current route location. It is a **reactive object triggered in templates**, and `useRoute()` is derived from it.

```ts
console.log(router.currentRoute.path) // /pages/index/index
```

## Navigation Methods

### `push(location)`

- Returns: `Promise<NavigationResult>`
- Description: Navigates to a new page (corresponds to `uni.navigateTo`; TabBar pages automatically use `uni.switchTab` instead).

### `replace(location)`

- Returns: `Promise<NavigationResult>`
- Description: Replaces the current page (corresponds to `uni.redirectTo`).

### `relaunch(location)`

- Returns: `Promise<NavigationResult>`
- Description: Closes all pages and opens the target (corresponds to `uni.reLaunch`).

### `back(delta?)`

- Returns: `Promise<NavigationResult>`
- Description: Goes back one or more pages (corresponds to `uni.navigateBack`); `delta` must be a positive integer.

```ts
await router.push({ name: 'about' })
await router.back()      // go back one page
await router.back(2)     // go back two pages
```

## Guard Registration

| Method | Returns | Description |
| --- | --- | --- |
| `beforeEach(guard)` | `() => void` | Global before guard; returns an unregister function |
| `beforeResolve(guard)` | `() => void` | Global resolve guard |
| `afterEach(guard)` | `() => void` | Global after guard |
| `onError(handler)` | `() => void` | Router error handling callback |

```ts
const off = router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !isLoggedIn()) return { name: 'login' }
})
// remove it when needed
off()
```

## State and Utility Methods

### `resolve(location)`

- Returns: `RouteLocation`
- Description: Resolves a route location into a full `RouteLocation`, **without performing a navigation**.

### `hasRoute(name)`

- Returns: `boolean`
- Description: Checks whether a route with the given name exists.

### `getRoutes()`

- Returns: `RouteConfig[]`
- Description: Gets all registered route configs.

### `isReady()`

- Returns: `Promise<void>`
- Description: Waits for the router to finish initializing.

### `syncRoute()`

- Returns: `void`
- Description: Syncs the route state with the actual page stack (based on `getCurrentPages()`). On install, it is automatically called on the page's `onShow` through a global mixin.

### `guardRoute(location?, options?)`

- Returns: `Promise<RouteLocation>`
- Description: Re-runs the guard chain for the given route (cold-start scenario), **without performing an actual navigation**.

```ts
router.isReady().then(() => {
	const launchPath = options?.path ? `/${options.path}` : undefined
	router.guardRoute(launchPath, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' })
		}
	})
})
```

### `install(app)`

- Returns: `void`
- Description: Installs the router into the Vue app instance (**provides** `router` and `route` + mounts `$router` / `$route` + registers a global mixin). Usually invoked by `app.use(router)`.

### `onRouteChange(listener)`

- Returns: `() => void`
- Description: Registers a route-change listener (triggered on a complete navigation or a state sync); returns an unregister function.

```ts
router.onRouteChange((to, from) => {
	console.log('route change:', from.path, '→', to.path)
})
```

## Related APIs

- [createRouter()](./create-router)
- [useRouter()](./use-router)
- [NavigationGuard](./type-navigation-guard)