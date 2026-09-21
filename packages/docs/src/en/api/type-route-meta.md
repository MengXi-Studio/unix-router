# RouteMeta

Route metadata (the type of `RouteConfig.meta`, a UTS `type`). Three commonly used built-in fields:

```ts
type RouteMeta = {
	title?: string        // page title
	isTab?: boolean       // whether it is a tabBar page (decides switchTab navigation)
	requireAuth?: boolean // whether login is required (guards can block based on it)
}
```

## Usage Example

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/home/home', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: 'Profile', requireAuth: true } }
]
```

Reading the target page's meta in a guard to make decisions:

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) }
	}
	return true
})
```

## Custom meta Fields: Modify the Type Definition Directly

::: warning An important difference from vue-router
In vue-router, custom meta fields rely on **interface declaration merging** via `declare module { interface RouteMeta {...} }`. But **UTS does not support interface declaration merging**, so that approach does not work on native platforms (Kotlin/Swift). Custom fields must be added by **directly modifying the `RouteMeta` type definition**.
:::

Append fields directly in this library's source (or in your own forked / copied `route.uts` type file):

```ts
// where the RouteMeta type is defined (types/route.uts)
export type RouteMeta = {
	title?: string
	isTab?: boolean
	requireAuth?: boolean
	// ↓ append your custom fields as needed
	icon?: string
	tag?: string
}
```

After appending, you get full type hints in configs and guards:

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/new/new', name: 'new', meta: { title: 'New Arrivals', tag: 'new' } }
]
```

> When reading optional fields, use explicit checks like `=== true` / `=== undefined` (UTS is strictly typed, with no implicit truthy conversion). For more, see the [Route Meta guide](../guide/meta).

## Related APIs

- [RouteConfig](./type-route-config)
- [Route Meta guide](../guide/meta)
