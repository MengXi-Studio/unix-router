# Route Configuration

unix-router is based on the uni-app x **static page model**: one route = one page registered in `pages.json`, and the route path is the page path.

## One-to-One Mapping of Routes and Pages

```
pages.json                            router.config.ts
─────────────────────                 ──────────────────
pages/                                routes:
  index/index.uvue   ◀── maps ───▶   { path: 'pages/index/index', name: 'home', ... }
  about/about.uvue   ◀── maps ───▶   { path: 'pages/about/about', name: 'about', ... }
```

**Key constraint**: `RouteConfig.path` must **exactly match** the page path registered in `pages.json`, otherwise the target page will not be compiled into the bundle and navigation will white-screen. This is also why unix-router does **not** support dynamic routes (a route cannot compile a page into the bundle at runtime).

## RouteConfig Fields

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string` | Page path; must match `pages.json`, e.g. `pages/index/index` |
| `name` | `string?` | Named route, for navigation by name (recommended) |
| `meta` | `RouteMeta?` | Route metadata (see [Route Meta](./meta)) |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | Route-local guard (runs only when entering this route) |
| `redirect` | `RouteLocationRaw?` | Redirect target |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	// Minimal: only path
	{ path: 'pages/index/index' },

	// Common: path + name + meta (isTab decides switchTab)
	{ path: 'pages/home/home', name: 'home', meta: { title: 'Home', isTab: true } },

	// With route-local guard beforeEnter (only for this route)
	{
		path: 'pages/profile/profile',
		name: 'profile',
		meta: { title: 'Profile', requireAuth: true },
		beforeEnter: (to, from) => (isLoggedIn() ? true : { name: 'login' })
	},

	// Redirect: visiting the old path jumps to the new path
	{ path: 'pages/old/home', redirect: 'pages/home/home' }
]
```

## `path` or `name`: Which to Navigate By

- **`name`**: decouples the path from callers. If a page path changes, update only `router.config.ts`; recommended for business code.
- **`path`**: good for ad-hoc jumps and small projects; the string form can inline query, e.g. `router.push('/pages/detail/detail?id=1')`.

## Path Normalization

Paths are normalized automatically (leading `/` added, trailing `/` removed):

- `pages/index/index` → `/pages/index/index`
- `'/pages/about/about/'` → `/pages/about/about`

## Strict Mode via `strict`

- `strict: true` (default): navigating with an **unregistered `name`** → throws `ROUTE_NOT_FOUND`, catching typos early.
- `strict: false`: does not throw; falls back to path (`/` + name) with a warning.

```ts
// with strict: true
router.resolve({ name: 'homee' }) // throws RouterError(ROUTE_NOT_FOUND)
```

## Duplicate Names / Paths

Configs with the same name or path **override earlier ones** and log a warning. Prefer unique `name`s in your config.

## On "Named Route Type Hints"

The repo ships an empty `RouteNameMap` interface. To get type hints for `name`, extend it via module augmentation:

```ts
// Only affects TS / editor hints
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: void
		about: void
	}
}
```

> ⚠️ **UTS limitation**: uni-app x native (Kotlin/Swift) does **not** support interface declaration merging, so this augmentation won't apply on App native compile — it only helps H5/editor (TypeScript) autocomplete. On native, use string `name` plus the `strict` mode as a safety net.

## Common Pitfalls

- **Wrong / unregistered path**: white screen. Cross-check `pages.json`.
- **`name` typo**: with `strict: true` it throws; with `false` it silently degrades and is easy to miss.
- **Duplicate names**: later one wins; if jumping to the wrong page, first check for duplicate `name`s.

## Unsupported Capabilities

The static page model does not support (see [Differences from vue-router](./differences)):

- Dynamic routes `addRoute` / `removeRoute`
- Nested routes `children`
- Named views / `RouterView`
- `scrollBehavior`