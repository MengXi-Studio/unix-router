# Route Configuration

unix-router is built on the **static page model** of uni-app x: one route = one page registered in `pages.json`, and the route path is the page path.

## One-to-One Mapping Between Routes and Pages

```
pages.json                            router.config.ts
─────────────────────                 ──────────────────
pages/                                routes:
  index/index.uvue   ◀── maps ───▶   { path: 'pages/index/index', name: 'home', ... }
  about/about.uvue   ◀── maps ───▶   { path: 'pages/about/about', name: 'about', ... }
```

**Key constraint**: `RouteConfig.path` must be **exactly identical** to the page path registered in `pages.json`; otherwise the target page will not be compiled into the bundle and navigation will result in a white screen. This is also why unix-router does **not** support dynamic routes (a newly added route cannot compile a page into the bundle at runtime).

## RouteConfig Fields

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string` | **Required**. Page path, identical to the registration in `pages.json`, **without a leading slash**, e.g. `pages/index/index` |
| `name` | `string?` | Named route, for navigation by name (recommended) |
| `meta` | `RouteMeta?` | Route metadata (see [Route Meta](./meta)) |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | Route-exclusive before guard (runs only when entering this route) |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	// Minimal: path only
	{ path: 'pages/index/index' },

	// Common: path + name + meta (isTab decides whether switchTab is used)
	{ path: 'pages/home/home', name: 'home', meta: { title: 'Home', isTab: true } },

	// Route-exclusive guard, function form: runs only when this route is visited
	{
		path: 'pages/profile/profile',
		name: 'profile',
		meta: { title: 'Profile', requireAuth: true },
		beforeEnter: (to, from) => (isLoggedIn() ? true : { name: 'login' })
	},

	// Route-exclusive guard, array form: runs in order; any rejection stops the chain
	{
		path: 'pages/admin/admin',
		name: 'admin',
		beforeEnter: [checkLogin, checkAdmin]
	}
]
```

## `path` or `name`: Which to Navigate By

- **`name`**: decouples the path from callers. When a page path changes, you only update one place in `router.config.ts`; navigating by `name` uniformly in business code is recommended.
- **`path`**: suitable for ad-hoc jumps and small projects; the string form can inline a query, e.g. `router.push('/pages/detail/detail?id=1')`.

## Path Normalization

The `path` in the config carries no leading slash (consistent with `pages.json`); internally the router normalizes all paths to the canonical form with a leading slash and no trailing slash:

- `pages/index/index` → `/pages/index/index`
- `/pages/about/about/` → `/pages/about/about`

Navigation inputs (the string or object passed to `push`) work with or without a leading slash — the parsed result is identical.

## Strict Mode `strict`

- `strict: true` (default): navigating by an **unregistered `name`** → throws a `RouterError` of `ROUTE_NOT_FOUND`, which helps catch typos early.
- `strict: false`: does not throw; logs a warning and degrades to path-based handling (`/` + name).

```ts
// with strict: true
router.resolve({ name: 'homee' }) // throws RouterError(ROUTE_NOT_FOUND)
```

## Duplicate `name` / `path` Detection

Configs with the same name or the same path: **a warning is logged and the later one overrides the earlier one**:

```
检测到重复路由名称 "home"，后者将覆盖前者。
检测到重复路由路径 "/pages/index/index"，后者将覆盖前者。
```

Keep `name` unique; when pages "jump to the wrong place", first check for duplicate `name` / `path` entries.

## The Named Route Type RouteName

`RouteName` is the exported type of named route names:

- **WEB**: `keyof RouteNameMap & string`. `RouteNameMap` is a built-in empty interface that can be extended via module augmentation, providing literal hints for route names:

```ts
// Only effective for TS / editor hints
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: 'home'
		about: 'about'
	}
}
```

- **Native platforms**: UTS does not support `keyof` union types, so `RouteName` degrades to `string` (paired with `strict` mode as a safety net against typos).

A typical usage is to annotate parameter types in your business wrappers:

```ts
import type { RouteName } from '@meng-xi/unix-router'

function go(where: RouteName) {
	router.push({ name: where })
}
// On H5, `where` gets literal completion; on native it is equivalent to string
```

## Common Pitfalls

- **Wrong / unregistered path**: white screen. Cross-check `pages.json`.
- **`name` typo**: throws when `strict: true`; when `false` it silently degrades to path navigation and is easy to miss.
- **Duplicate names**: the later one overrides; if pages jump to the wrong place, first check for duplicate `name`s.

## Unsupported Capabilities

Not supported under the static page model (see [Differences from vue-router](./differences)):

- Dynamic routes `addRoute` / `removeRoute`
- Nested routes `children`
- Named views / `RouterView`
- `scrollBehavior`

## Next Steps

- [Navigation](./navigation) — the four navigation methods and how to pass data
- [Route Meta](./meta) — built-in meta fields and customization
- [RouteConfig API](../api/type-route-config) — the complete type definition
