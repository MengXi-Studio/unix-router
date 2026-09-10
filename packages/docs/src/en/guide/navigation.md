# Navigation

unix-router provides four navigation methods — `push / replace / relaunch / back` — mapped to the uni native navigation APIs, with automatic tabBar page detection.

## Navigation Methods

| Method | Corresponding uni API | Notes |
| --- | --- | --- |
| `push` | `navigateTo` / `switchTab` | Pushes onto the stack, can go back |
| `replace` | `redirectTo` / `switchTab` | Replaces the current page, no new stack entry |
| `relaunch` | `reLaunch` / `switchTab` | Closes all pages and opens the target |
| `back` | `navigateBack` | Goes back one / several pages |

`router.push` and friends all return a `Promise`: resolves on success and rejects on failure (detected with `isNavigationFailure`).

## Location Form (RouteLocationRaw)

Both a string and an object are supported:

```ts
// 1. String path
await router.push('/pages/about/about')

// 2. Path object (path takes precedence over name)
await router.push({ path: 'pages/about/about', query: new Map([['a', '1']]) })

// 3. Named object (name is recommended for compile-time consistency)
await router.push({ name: 'about', query: new Map([['a', '1']]) })
```

::: tip query and params are both Maps
uni-app x carries query/params as `Map<string, string>`; read them with `.get(key)` / `.has(key)`.
:::

## Passing query

query shows up in the URL and survives refresh:

```ts
await router.push({
	name: 'detail',
	query: new Map([['id', '1024']])
})

// target page
const route = useRoute()
console.log(route.query.get('id')) // '1024'
```

## Passing object params (ParamsPlugin)

Complex objects are not suitable for stuffing into a URL. After registering `ParamsPlugin`, `params` are passed through the `__params__` in-memory key channel:

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [ParamsPlugin] })

// source page
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024'], ['name', 'Zhang Wei']])
})

// read on the target page
const route = useRoute()
console.log(route.params.get('id'))   // '1024'
console.log(route.params.get('name')) // 'Zhang Wei'
```

> To keep params across refresh, enable `paramsPersistent: true` (stored to storage). Using `params` without registering `ParamsPlugin` throws `PLUGIN_REQUIRED`. See [Plugin System](./plugins).

## tabBar Pages

When the target route has `meta.isTab === true`, the router automatically uses `uni.switchTab`:

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true } }
]

await router.push({ name: 'mine' }) // automatically uses switchTab
```

> `switchTab` does not support query; on a tabBar page pass params via `onShow` + global state instead (see [Recipes](./recipes#tabbar-page-data)).

## Going Back

```ts
await router.back()   // back one page
await router.back(2)  // back two pages
```

- `delta` must be a positive integer
- Returning `CANCELLED` when the page stack is insufficient
- On App/H5, going back goes through the guard chain; on Mini Program the host back must be handled afterwards via `onRouteChange` (see [Platform Compatibility](./compatibility))

## Duplicate Navigation and Concurrency

- **Duplicate navigation**: pushing to a location identical to the current one throws `DUPLICATED`; use `replace` or catch and ignore it
- **Concurrency queueing**: while a previous navigation is unfinished, a new one waits for it to finish

```ts
try {
	await router.push({ name: 'about' })
} catch (err) {
	if (router.code === 16) return // RouterErrorCode.DUPLICATED, ignore
	// other navigation failures: handle them
}
```

## Programmatic vs Declarative Navigation

Besides calling `router.push`, you can also:

```ts
// programmatic (any context)
router.replace('/pages/login/login')

// declarative (components)
import { RouterLink } from '@meng-xi/unix-router'
// <RouterLink to="pages/about/about">About</RouterLink>
```

## Cold-Start Direct Entry

Cold-start / direct-URL entries into a page in `uni-app x` **do not go through the guard chain** (the page is loaded straight from `pages.json`). To re-run guards, use `guardRoute`:

```ts
// App.vue onLaunch
router.isReady().then(() => {
	router.guardRoute(`/${options?.path ?? ''}`, {
		onAbort: (failure) => router.relaunch({ name: 'login' })
	})
})
```

## Next Steps

- [Route Guards](./guards) — permission control during navigation
- [Plugin System](./plugins) — ParamsPlugin / InterceptorPlugin
- [Composables](./composables) — useRoute / useLink