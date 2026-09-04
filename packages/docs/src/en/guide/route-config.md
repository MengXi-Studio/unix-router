# Route Config

unix-router is based on uni-app x's **static page model**: each route corresponds to a page registered in `pages.json`, and the route path is the page path.

## RouteConfig

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string` | The page path, must match the registration in `pages.json`, e.g. `pages/index/index` |
| `name` | `string?` | Named route, used to navigate by name |
| `meta` | `RouteMeta?` | Route metadata (title / isTab / requireAuth, etc.) |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | Route-local before guard |
| `redirect` | `RouteLocationRaw?` | Redirect target |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{
		path: 'pages/index/index',
		name: 'home',
		meta: { title: 'Home', isTab: true }
	},
	{
		path: 'pages/guards/guards',
		name: 'guards',
		meta: { title: 'Guards', requireAuth: true },
		beforeEnter: (to, from) => {
			return isLoggedIn() ? true : { name: 'login' }
		}
	},
	// redirect: navigating to this path jumps straight to the target
	{ path: 'pages/old/old', redirect: 'pages/index/index' }
]
```

## Path Normalization

Paths are automatically normalized: a leading slash is added and trailing slashes are removed.

- `pages/index/index` → `/pages/index/index`
- `'/pages/about/about/'` → `/pages/about/about`

## Strict Mode

With `createRouter({ strict: true })`, resolving an unknown **named route** throws a `ROUTE_NOT_FOUND` error; in non-strict mode it warns and falls back to path-based handling.

## Named Route Type Hints (Optional)

By augmenting the module's `RouteNameMap`, you can get type hints for `name`:

```ts
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: void
		about: void
	}
}
```

## Unsupported Capabilities

Because of the static page model, the following capabilities are **not supported** (see [Differences from vue-router](./differences) for details):

- Dynamic routes `addRoute` / `removeRoute`
- Nested routes `children`
- Named views / `RouterView`
- `scrollBehavior`