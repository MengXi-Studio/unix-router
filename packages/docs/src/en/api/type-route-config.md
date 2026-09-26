# RouteConfig

A single route configuration record. Since uni-app x adopts the static `pages.json` page model, **the page path is the route path**, and `path` must match the registration in `pages.json`.

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: 'About' } },
	{
		path: 'pages/admin/admin',
		name: 'admin',
		meta: { title: 'Admin', requireAuth: true },
		beforeEnter: (to, from) => {
			return isAdmin() ? true : { name: 'home' }
		}
	}
]
```

## Fields

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `path` | `string` | — | **Required**. Page path, must match `pages.json` (e.g. `pages/index/index`), without a leading slash; normalized to `/pages/index/index` after parsing |
| `name` | `string` | — | Named route name, for navigation by name; a duplicate `name` logs a warning and the later one overrides the earlier one |
| `meta` | `RouteMeta` | — | Route metadata (`title` / `isTab` / `requireAuth`) |
| `redirect` | `RouteLocationRaw` | — | Reserved field (the redirect target). Not consumed by the current matcher / router; declared in the type for forward compatibility |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]` | — | Route-exclusive before guard: a function or an array, runs only when entering this route (before the global `beforeResolve`) |

## Related APIs

- [createRouter()](./create-router)
- [RouteMeta](./type-route-meta)
- [NavigationGuard](./type-navigation-guard)
