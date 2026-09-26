# Navigation

unix-router provides four navigation methods — `push / replace / relaunch / back` — which map to the uni native navigation APIs underneath and automatically detect tabBar pages. Each navigation returns `Promise<NavigationResult>`: on success it **resolves with the target route location**, on failure it **rejects with a `NavigationFailure`**.

## Navigation Methods

| Method | Corresponding uni API | Description |
| --- | --- | --- |
| `push` | `navigateTo` / `switchTab` | Pushes onto the stack; can go back |
| `replace` | `redirectTo` / `switchTab` | Replaces the current page, no new stack entry |
| `relaunch` | `reLaunch` / `switchTab` | Closes all pages and opens the target |
| `back` | `navigateBack` | Goes back one or several pages |

## Location Forms (RouteLocationRaw)

Both a string and an object are supported:

```ts
// 1. String path (can inline ?query)
await router.push('/pages/detail/detail?id=1024')

// 2. Path object (path takes precedence over name)
await router.push({ path: 'pages/detail/detail', query: new Map<string, string>([['id', '1024']]) })

// 3. Named object (recommended: a path change only requires updating the config)
await router.push({ name: 'detail', query: new Map<string, string>([['id', '1024']]) })
```

::: tip Both query and params are Maps
uni-app x carries query / params as `Map<string, string>`; read them with `.get(key)` / `.has(key)`.
:::

## Passing Data: query or params?

- **query**: visible in the URL, survives refresh and bookmarking; suitable for short values like ids or source tags. Available directly with any navigation method.
- **params**: never enters the URL; passed across pages via the associated store of `ParamsPlugin`, suitable for structured data or content you do not want exposed in the address bar.

For a full comparison and usage of both, see [Passing Parameters](./params).

## tabBar Pages

When the target route has `meta.isTab === true`, `push` / `replace` / `relaunch` automatically switch to `uni.switchTab`:

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true } }
]

await router.push({ name: 'mine' }) // automatically uses switchTab
```

::: warning switchTab carries no query
uni's `switchTab` discards the entire query string, so **neither `query` nor `params` reaches the target tab page** (plugin params travel via an internal query key, which is dropped as well). To pass data to a tabBar page, use global state or storage instead (see [Recipes](./recipes#tabbar-apps)).
:::

## Going Back

```ts
await router.back()   // delta defaults to 1, back one page
await router.back(2)  // back two levels
```

Behavior details:

- `delta` defaults to `1`; it must be a **positive integer**, otherwise no navigation is started and the promise rejects with `ABORTED` immediately
- Insufficient page stack (fewer than two pages, or `delta` exceeds the stack depth) → rejects with `CANCELLED`
- The back target is resolved by the **page stack** (the `delta`-th page counting from the top), then matched back to a route record by path
- The guard phase before going back only runs the global `beforeEach` + `beforeResolve` (the route-exclusive `beforeEnter` does not participate); `afterEach` still fires after a successful back

## Duplicate Navigation Detection

Only `push` detects duplicates: when the target location is fully identical to the current route's `path` / `query` / `params` / `hash`, it rejects with `DUPLICATED`. `replace` / `relaunch` mean "force arrival" by semantics and perform no detection.

```ts
try {
	await router.push({ name: 'about' })
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) return // ignore duplicate navigation
	throw e
}
```

## Concurrent Navigation Queueing

When the previous navigation has not finished, a new navigation **automatically waits for it to complete before executing** (serialized queueing internally) — no manual locking or debouncing needed.

## Handling Navigation Failures

A failed navigation rejects the promise; branch on the error code:

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

const goDetail = async () => {
	try {
		await router.push({ name: 'detail' })
	} catch (e) {
		if (isNavigationFailure(e, RouterErrorCode.ABORTED)) return     // blocked by a guard, normal business flow
		if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) return  // duplicate navigation, ignore
		// the rest: ROUTE_NOT_FOUND / NAVIGATION_API_ERROR / PLUGIN_REQUIRED ...
		uni.showToast({ title: 'Navigation failed', icon: 'none' })
	}
}
```

You can also use `router.onError` as a global fallback; see [Error Handling](./error-handling).

## Per-Navigation Animation

After registering `AnimationPlugin`, the global default animation applies; a single navigation can override it on the location object:

```ts
await router.push({
	name: 'detail',
	animationType: 'slide-in-right',
	animationDuration: 300
})
```

See [Navigation Animation](./animation) for details.

## Programmatic vs Declarative Navigation

Besides calling `router.push`, you can also:

```ts
// programmatic (any context)
router.replace('/pages/login/login')

// declarative (in components)
import { RouterLink } from '@meng-xi/unix-router'
// <RouterLink to="pages/about/about">About</RouterLink>
```

The props of `RouterLink` are only `to` / `replace` / `relaunch`; see the [RouterLink API](../api/router-link).

## Cold-Start Direct Entry

Cold start / direct-URL entry into a page in `uni-app x` **does not go through the guard chain** (the page is loaded directly by `pages.json`). To re-run guards, use `guardRoute`:

```ts
// App.vue onLaunch
router.isReady().then(() => {
	router.guardRoute(`/${options?.path ?? ''}`, {
		onAbort: (failure) => router.relaunch({ name: 'login' })
	})
})
```

## Next Steps

- [Passing Parameters](./params) — choosing between query / params and ParamsPlugin
- [Composables](./composables) — useRouter / useRoute / useLink
- [Router Instance](../api/router-instance) — full signatures of the navigation methods
