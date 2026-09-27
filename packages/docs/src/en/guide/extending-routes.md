# Extending Routes

`routes.gen.uts` is generated at build time and is not meant to be hand-edited, but "generated" does not mean "not customizable". This page covers the two extension mechanisms: **preserving manual edits** across regeneration (`preserveRouteChanges`), and the **extension declaration file** for the `pages.json` mode (`routes.ext.uts`).

## The routes.gen.uts Example

```ts
/** Generated automatically by @meng-xi/unix-router/vite-plugin */
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{
		path: 'pages-sub/setting/setting',
		name: 'setting',
		meta: { title: '设置' },
		beforeEnter: (to, from) => { /* macro function injected verbatim */ }
	},
	{ path: 'pages/mine/mine', name: 'mine', meta: { title: '我的', isTab: true } },
	// ...
]
```

### preserveRouteChanges

On regeneration the plugin diffs the existing `routes.gen.uts`: fields you appended to existing entries and entire hand-written custom routes (e.g. virtual placeholder routes) are **all preserved**; only the fields derived from page declarations are refreshed. A single file can be regenerated safely.

## The Extension Declaration File (routes.ext.uts)

`routesGen` generates the route table from the hand-written `pages.json` as its source of truth, but `name` / extended `meta` / `beforeEnter` cannot be expressed in `pages.json`. For this, `routesGen` provides an extension declaration file (default `routes.ext.uts`), parsed at build time and merged into the generated route table:

```ts
// routes.ext.uts —— must export an array literal
export const routeExtensions = [
	{
		path: 'pages/goods/detail', // match key: path or name (either one)
		meta: { requireAuth: true }, // appended meta extension fields
		beforeEnter: (to, from) => {
			// injected verbatim into the generated file, must be self-contained
			// (same mechanism as macro injection)
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	},
	{ name: 'home', meta: { keepAlive: true } }
]
```

**Merge rules**:

| Scenario | Behavior |
| --- | --- |
| Match a route by `path` (takes priority) or `name` | Unmatched entries are logged and ignored |
| Explicit `name` declared | Overrides the auto-generated name; skipped with a warning if it conflicts with another route's name |
| `meta.title` / `meta.isTab` | Ignored with a warning when already derived from `pages.json` (style / tabBar); only fills in when missing |
| Other `meta` fields | Deduplicated by key then appended (the declared value wins) |
| `beforeEnter` | A later declaration overrides the earlier one (warn on override) |
| Fields other than `path` / `name` / `meta` / `beforeEnter` | The entry is dropped with a warning |

`routeGen` / `pagesGen` do not read the extension declaration file — their name / meta / beforeEnter are declared near the page via macros / blocks (see [File Conventions](./file-conventions)).

## Common Pitfalls

- **Page edits not taking effect under `routesGen`**: `routesGen` treats `pages.json` as the source of truth and does not scan page directories — register new pages in `pages.json` first and the route table regenerates; name / meta / beforeEnter go through the extension declaration file.
- **Page edits not taking effect (`routeGen` / `pagesGen`)**: make sure the plugin's `watch` is on (default); if you edit `pages.json` directly through HBuilderX, it will be overwritten by the next regeneration — declare pages in the page files instead.

## Next Steps

- [File-Based Routing](./file-based-routing) — plugin choice, setup, and the options reference
- [File Conventions](./file-conventions) — the defineUniPage macro / &lt;route-config&gt; block syntax
- [Route Guards](./guards) — where `beforeEnter` sits in the guard chain
