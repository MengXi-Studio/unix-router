# File-Based Routing

The page model of unix-router is static: one route = one page registered in `pages.json`. Traditionally you write path / title / tabBar / isTab **twice** — once in `pages.json` and once in the route table — which easily drifts out of sync.

**File-based routing** moves the source of truth into each page file: declare a small config near your page code, and a build-time plugin generates `pages.json` (including tabBar) and the `routes.gen.uts` route table automatically — **add / remove / rename a page, and both configs stay in sync from a single declaration**.

```ts
// vite.config.ts —— the only integration point
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

export default {
	plugins: [uni(), routeGen({ ... })]
}
```

> This is a **build-time dev tool** (vite / webpack plugin). It plays no role at runtime and adds zero bundle size; the macro is stripped during compilation.

## The defineUniPage Macro

Declare it at the top of each page's `<script setup>` (no import needed; stripped at compile time with same-line-count comment placeholders that keep line numbers stable):

```ts
<!-- pages/mine/mine.uvue -->
<script setup>
	// Page declaration macro: stripped at compile time by @meng-xi/unix-router/vite-plugin
	defineUniPage({
		title: 'Mine',
		name: 'mine',
		isTab: true,
		tab: { order: 2, text: 'Mine', iconPath: 'static/tabbar/mine.png', selectedIconPath: 'static/tabbar/mine-active.png' }
	})
	// ...page logic
</script>
```

| Field | Type | Description |
| --- | --- | --- |
| `title` | `string?` | Page title → `navigationBarTitleText` in `pages.json` and route `meta.title` |
| `name` | `string?` | Named route; auto-generated via camelCase normalization when omitted |
| `isTab` | `boolean?` | Whether it is a tabBar page → `meta.isTab` + `pages.json` `tabBar.list` |
| `tab` | `{ order?, text?, iconPath?, selectedIconPath? }` | tabBar details; `order` determines tab position |
| `meta` | `Record<string, any>?` | Extended route meta fields (e.g. `requireAuth`), must match the `RouteMeta` definition |
| `redirect` | `string?` | Route redirect target (page path) |
| `beforeEnter` | `(to, from) => any?` | Per-route before guard, **must be self-contained** (the generated file cannot reference page scope) |

**Rules**: at most once per page, at the top level of the script. Function fields such as `beforeEnter` are injected into the generated file verbatim (JSON cannot express functions; UTS expressions can), and type safety is enforced by the UTS compile chain:

```ts
<!-- pages-sub/setting/setting.uvue —— subpackage page + per-route guard -->
<script setup>
	defineUniPage({
		title: 'Settings',
		name: 'setting',
		beforeEnter: (to: any, from: any): any => {
			// Note: must be self-contained — cannot reference variables inside the page
			// (the generated file and the page file are two separate scopes)
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	})
</script>
```

## The &lt;route-config&gt; Custom Block

If you prefer not to use the macro, or the declaration is long, use an SFC custom block (same effect, lower priority than the macro):

```vue
<route-config lang="jsonc">
{
	// jsonc: comments supported
	"title": "Goods Detail",
	"name": "goods-detail",
	"meta": { "requireAuth": true }
}
</route-config>

<route-config lang="uts">
{
	title: 'Checkout',
	meta: { requireAuth: true },
	// lang="uts" supports function fields
	beforeEnter: (to, from) => true
}
</route-config>
```

The block content is intercepted as a virtual module at compile time and never enters the page JS — transparent to the compiler.

## Priority

When the same field is declared in multiple places, it is resolved along the priority chain:

```
defineUniPage macro  >  <route-config> block  >  plugin inference (titleFallback / tabBar config as fallback)
```

Merging is field-level: if the macro declares `title` and the block declares `meta`, both take effect.

## Automatic Name Normalization

| Scenario | Result |
| --- | --- |
| `name` declared via macro/block | The declared value is used |
| Omitted, last segment `mine` is unique | Last-segment camelCase: `pages/mine/mine` → `mine` |
| Last-segment collision (e.g. `pages/a/index` and `pages/b/index`) | Full-path camelCase fallback: `aIndex` / `bIndex` |
| Still conflicting in extreme cases | Handled by `errorStrategy`: `strict` (default) aborts the build with a listing / `warn` logs and skips |

Generates `route-name.gen.d.ts` (a WEB-side `RouteNameMap` module augmentation), giving literal autocomplete for name-based navigation (see [The Named Route Type RouteName](./route-config#the-named-route-type-routename)).

## Generated Artifacts

| File | Description |
| --- | --- |
| `pages.json` | Auto-generated page entries + tabBar + subPackages; **hand-written non-page fields (globalStyle, uniIdRouter, etc.) are merged and preserved** |
| `routes.gen.uts` | The route table `RouteConfig[]`; in `router.uts`: `import { routes } from './routes.gen.uts'` |
| `route-name.gen.d.ts` | Literal types for `RouteNameMap` (WEB-side editor hints, optional) |
| `define-uni-page.d.ts` | Macro type declaration (editor hints, optional) |

Commit the generated files **to git**: the HBuilderX CLI compile chain does not guarantee the plugin runs first; watch/HMR are development-time conveniences only.

### routes.gen.uts Example

```ts
/** Generated by @meng-xi/unix-router/vite-plugin */
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{
		path: 'pages-sub/setting/setting',
		name: 'setting',
		meta: { title: 'Settings' },
		beforeEnter: (to, from) => { /* macro function injected verbatim */ }
	},
	{ path: 'pages/mine/mine', name: 'mine', meta: { title: 'Mine', isTab: true } },
	// ...
]
```

### preserveRouteChanges

On regeneration the plugin diffs the existing `routes.gen.uts`: fields you added to existing entries and entire hand-written custom routes (e.g. virtual placeholder routes) are **all preserved**; only fields derived from page declarations are refreshed. A single file can be regenerated safely.

## Configuration

### uni-app x CLI Projects

The project's `vite.config.ts` already has `uni()`; just append:

```ts
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

export default defineConfig({
	plugins: [
		uni(),
		routeGen({
			pages: {
				pagesDir: 'pages',
				subPackages: [{ root: 'pages-sub', dir: 'pages-sub' }],
				entryPage: 'pages/index/index',
				tabBar: { color: '#7A7E83', selectedColor: '#007AFF', backgroundColor: '#FFFFFF' }
			},
			router: { dts: true }
		})
	]
})
```

### HBuilderX Projects

**In HBuilderX, a project-level `vite.config.ts` entirely replaces the built-in config** (the built-in `uni()` no longer applies), so you must import `uni()` yourself; `@dcloudio/vite-plugin-uni` is not in the project dependencies, so resolve it from the HBuilderX install:

```ts
// vite.config.ts (HBuilderX)
import { createRequire } from 'node:module'
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

const hbxRequire = createRequire('<HBuilderX install dir>/plugins/uniapp-cli-vite/package.json')
const uniMod = hbxRequire('@dcloudio/vite-plugin-uni')

export default {
	plugins: [uniMod.default ?? uniMod, routeGen({ ... })]
}
```

### Options Reference

**Top level**:

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `pagesJsonPath` | `string?` | `'pages.json'` | pages.json path (relative to project root) |
| `watch` | `boolean?` | `true` | Auto-rerun on page directory changes (200 ms debounced, serialized) |
| `verbose` | `boolean?` | `false` | Print generation logs |
| `errorStrategy` | `'strict' \| 'warn'?` | `'strict'` | Strategy for parse conflicts / terminal name conflicts |
| `pages` | `object?` | — | Phase one (page generation) options |
| `router` | `object?` | — | Phase two (route generation) options |

**`pages`**:

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `pagesDir` | `string?` | `'pages'` | Main-package page source directory |
| `subPackages` | `{ root, dir }[]?` | `[]` | Subpackages: `root` is the page path prefix, `dir` the source directory |
| `entryPage` | `string?` | — | Entry page, moved to the head of the `pages` array |
| `titleFallback` | `string?` | — | Fallback title when the macro/block does not declare one |
| `tabBar` | `object?` | — | tabBar appearance fields (color / selectedColor / backgroundColor / borderStyle) |
| `includeExtensions` | `string[]?` | `['.uvue']` | Page extensions to scan |
| `excludePatterns` | `(string \| RegExp)[]?` | `[]` | Exclusion rules |
| `dts` | `string \| false?` | — | Macro type declaration output path, `false` to disable |

**`router`**:

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `outputPath` | `string?` | `'routes.gen.uts'` | Route table file path |
| `exportName` | `string?` | `'routes'` | Exported variable name of the route array |
| `nameStrategy` | `'camelCase' \| 'fullPath'?` | `'camelCase'` | Name normalization strategy (last segment / full path) |
| `importFrom` | `string?` | `'@meng-xi/unix-router'` | Import source of the `RouteConfig` type; for uni_modules usage point to the utssdk entry |
| `dts` | `string \| false?` | — | `RouteNameMap` declaration file path, `false` to disable |
| `preserveRouteChanges` | `boolean?` | `true` | Preserve your modifications to the route file on regeneration |

## Relationship with @meng-xi/vite-plugin (generateUni)

The `defineUniPage` macro / `<route-config>` block syntax stays consistent with MengXi Studio's `generateUni`, so the two plugins are interchangeable. Differences:

- `generateUni` targets projects not using unix-router; this plugin serves unix-router users with the same workflow and ships inside the router package (no extra install)
- This plugin extends unix-router-specific fields: `name`, extended `meta`, `beforeEnter`

## Common Pitfalls

- **Forgetting `uni()` in HBuilderX**: the dev server starts but pages are blank / `/main` returns 404. A project-level `vite.config.ts` replaces the built-in config entirely — import `uni()` yourself (see above).
- **`beforeEnter` referencing page variables**: the macro function is injected verbatim into `routes.gen.uts`, a separate scope from the page; referencing page variables breaks compilation. Guard logic must be self-contained (storage / global state).
- **Macro declared twice**: at most once per page; `strict` aborts the build.
- **Edits not taking effect**: make sure `watch` is on (default); if you edit `pages.json` directly through HBuilderX, it will be overwritten by the next regeneration — declare pages in the page files instead.

## Next Steps

- [Route Configuration](./route-config) — fields and conventions of a hand-written route table (the generation target of this plugin)
- [Route Meta](./meta) — applications of extended fields like `meta.requireAuth`
- [Route Guards](./guards) — where `beforeEnter` sits in the guard chain
