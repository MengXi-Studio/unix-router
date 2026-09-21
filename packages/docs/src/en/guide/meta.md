# Route Meta

`meta` is strongly typed metadata (a UTS `type`) attached to the route config, readable by guards, pages, and components. It ships with three built-in fields covering the most common scenarios: **page title**, **TabBar detection**, and **login interception**.

## Built-in Fields

```ts
type RouteMeta = {
	title?: string        // page title (usable for the nav bar title)
	isTab?: boolean       // whether it is a tabBar page (decides switchTab navigation)
	requireAuth?: boolean // whether login is required (usually paired with a guard for login interception)
}
```

Route config example:

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } }
```

## Reading meta

In pages / composables, read it reactively via `useRoute()`:

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.meta.title
route.meta.isTab
```

In guards, read the **target page's** meta to make navigation decisions:

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true) {
		// …
	}
	return true
})
```

## title: Page Title

### Set it uniformly in afterEach (recommended)

Register once, effective globally — every completed navigation updates the nav bar title according to the target page's `meta.title`:

```ts
router.afterEach((to, from, failure) => {
	if (failure !== null) {
		return // don't update the title when navigation fails
	}
	const title = to.meta.title ?? ''
	if (title.length > 0) {
		uni.setNavigationBarTitle({ title })
	}
})
```

### Use it per page as needed

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

onShow(() => {
	uni.setNavigationBarTitle({ title: route.meta.title ?? '' })
})
```

## isTab: TabBar Page Detection

When the target route has `meta.isTab === true`, the router automatically switches to `uni.switchTab` navigation (applies to `push` / `replace` / `relaunch`). Note that `switchTab` **does not carry query** — pass parameters to tab pages via global state or [inter-page communication](./events).

::: warning Don't forget to mark TabBar pages
Every page listed in the `pages.json` tabBar list must be configured with `isTab: true` in its route config, otherwise it will be opened via `navigateTo` and fail.
:::

## requireAuth: Login Interception

Pair it with a global guard to implement login interception (see the full flow in [Navigation Guards](./guards)):

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	return true
})
```

## Custom meta Fields

**UTS does not support interface declaration merging**. The vue-router module augmentation pattern (`declare module` + `interface RouteMeta`) is unavailable in uni-app x. Custom fields must be added **directly to the `RouteMeta` type definition**.

This library is distributed as UTS source code, so simply edit the type definition file (npm installs to `node_modules/@meng-xi/unix-router`, uni_modules installs to `uni_modules/ux-router`):

```ts
// RouteMeta type definition
export type RouteMeta = {
	title?: string
	isTab?: boolean
	requireAuth?: boolean
	// —— custom fields appended below ——
	icon?: string          // page icon
	requireAdmin?: boolean // admin page
}
```

After appending, the fields are usable directly in route configs and guards (with full type hints):

```ts
{ path: 'pages/admin/admin', name: 'admin', meta: { title: 'Admin', requireAdmin: true } }
```

::: tip Key difference from vue-router
vue-router extends meta through declaration merging: `declare module 'vue-router' { interface RouteMeta { … } }`. UTS uses a nominal type system with **no declaration merging** — you can only modify the original type.
:::

## Common Pitfalls

- **Forgetting `meta.isTab`**: a TabBar page opened via `navigateTo` will fail. Always set `isTab: true` for TabBar pages.
- **Truthiness checks on optional fields**: under UTS's strict typing, use explicit checks like `=== true` / `!= null`, e.g. `if (to.meta.requireAuth === true)`.
- **Reading meta at the wrong time**: route state is synced by the router at page `onShow` (handled automatically via `app.use(router)`); don't read the target page's meta any earlier.

## Next Steps

- [Navigation Guards](./guards) — the full flow of `requireAuth` login interception
- [Navigation](./navigation) — how `isTab` automatically switches to `switchTab`
- [RouteMeta Type](../api/type-route-meta) — type definition reference
