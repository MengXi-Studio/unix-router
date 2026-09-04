# RouteConfig

A single route configuration record. Because uni-app x uses the static `pages.json` page model, **the page path is the route path**, and `path` must match the registration in `pages.json`.

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: 'About', requireAuth: true } },
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
| `path` | `string` | — | **Required**. The page path, must match `pages.json` (e.g. `pages/index/index`) |
| `name` | `string` | — | The named route name, for navigating by name |
| `redirect` | `RouteLocationRaw` | — | The redirect target (when not provided, navigations to this path are redirected directly) |
| `meta` | `RouteMeta` | — | Route metadata |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]` | — | Route-local before guard |

## Related APIs

- [createRouter()](./create-router)
- [RouteMeta](./type-route-meta)
- [NavigationGuard](./type-navigation-guard)