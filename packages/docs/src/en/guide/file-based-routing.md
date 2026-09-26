# File-Based Routing

The page model of unix-router is static: one route = one page registered in `pages.json`. Traditionally you write path / title / tabBar / isTab **twice** — once in `pages.json` and once in the route table — and the duplicated maintenance easily drifts out of sync.

**File-based routing** moves the source of truth into each page file: declare a small config near your page code, and a build-time plugin generates `pages.json` (including tabBar) and the `routes.gen.uts` route table automatically — **add / remove / rename a page, and both configs stay in sync from a single declaration**.

```ts
// vite.config.ts —— the only integration point
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

export default {
	plugins: [uni(), routeGen({ ... })]
}
```

> This is a **build-time dev tool** (vite / webpack plugin). It plays no role at runtime and adds zero bundle size; the macro is stripped during compilation.

## Which Plugin to Choose

`@meng-xi/unix-router/vite-plugin` exports three independent plugins, each covering a different slice of the pipeline. They can be registered alone or in combination:

| Plugin | Data flow | Use case |
| --- | --- | --- |
| `routeGen` | Page files → `pages.json` + route table | **Default recommendation**. Declare near your pages, the whole chain stays in sync automatically |
| `pagesGen` | Page files → `pages.json` only | You only want automatic page registration; hand-write the route table |
| `routesGen` | `pages.json` → route table only | `pages.json` is hand-written (or maintained by another tool); name / meta / beforeEnter are injected via an extension declaration file |

- The three plugins are independent: `pagesGen` never touches route files, `routesGen` never touches `pages.json`
- `pagesGen` + `routesGen` combined can replace `routeGen` with the two phases decoupled — in that mode the page-level name / meta / beforeEnter declarations move to `routes.ext.uts` (see [Extending Routes](./extending-routes))

Usage is identical (an unplugin factory + vite adapter); only the option split differs: `pagesGen` takes the common options + `pages`, `routesGen` takes the common options + `router` (see [Options Reference](#options-reference)).

## Setup

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

**In HBuilderX, a project-level `vite.config.ts` entirely replaces the built-in config** (the built-in `uni()` no longer applies), so you must import `uni()` yourself; `@dcloudio/vite-plugin-uni` is not in the project dependencies, so resolve it from the HBuilderX install directory:

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

::: tip Forgetting uni()
In HBuilderX, if pages are blank after the dev server starts / `/main` returns 404, chances are the project-level `vite.config.ts` replaced the built-in config without importing `uni()` itself.
:::

Once registered, **start the dev server**: the plugin scans the page directories and produces the `pages.json` entries and the route table. For the in-page declaration syntax, see [File Conventions](./file-conventions).

## Migrating an existing project

Already have a hand-written `pages.json` and route table in the project? Pick one of the two paths below depending on your situation; both migrate incrementally.

### Path A: routeGen fully automatic — unify the duplicated configs (recommended)

Current state (path / title written once in `pages.json` and once in the route table):

```ts
// router/routes.ts —— hand-written route table
export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于' } }
]
```

```jsonc
// pages.json —— hand-written page entries (title duplicates meta.title above)
{
	"pages": [
		{ "path": "pages/index/index", "style": { "navigationBarTitleText": "首页" } },
		{ "path": "pages/about/about", "style": { "navigationBarTitleText": "关于" } }
	]
}
```

Migration steps:

1. Register `routeGen` as in [Setup](#setup);
2. Move each page's title / name / isTab / meta into the page macro:

```vue
<!-- pages/index/index.uvue -->
<script setup>
	defineUniPage({ title: '首页', name: 'home', isTab: true })
</script>
```

3. Replace the hand-written route table with the generated artifact:

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes.gen.uts' // generated automatically at build time

export const router = createRouter({ routes })
```

4. Delete the hand-written page entries from `pages.json` (non-page fields such as `globalStyle` are **kept** — the plugin merges them instead of dropping them when generating), then restart the dev server.

- Delete the hand-written array in `router/routes.ts` and switch to `import { routes } from './routes.gen.uts'`
- Move each page's `title / name / isTab / meta` into the `defineUniPage` at the top of the page (field mapping in [File Conventions](./file-conventions))

### Path B: routesGen incremental — keep pages.json hand-written

When `pages.json` is maintained by another tool, or you don't want to touch page files yet, hand only the **route table** to the plugin:

1. Replace `routeGen` with `routesGen` as in [Setup](#setup);
2. Keep `pages.json` as is; the route table is generated from it at build time;
3. Put `name` / extended `meta` / `beforeEnter` in the extension declaration file `routes.ext.uts` (see [Extending Routes](./extending-routes)).

To switch to Path A later, the `pagesGen` + `routesGen` combo transitions smoothly (the two phases are decoupled).

## Starting from scratch

The minimal flow for wiring up a new project from zero:

**1. Install**

```bash
npm install @meng-xi/unix-router
```

**2. Register the plugin** (see [Setup](#setup); a CLI project is shown)

**3. Declare near the page**

```vue
<!-- pages/index/index.uvue -->
<script setup lang="uts">
	// Entry page: entryPage is declared as pages/index/index in the plugin options
	defineUniPage({ title: '首页', name: 'home', isTab: true })
</script>

<template>
	<view class="page">
		<text class="title">首页</text>
	</view>
</template>
```

**4. Create the router**: the route table is no longer hand-written; import the generated artifact

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes.gen.uts' // generated automatically at build time

export const router = createRouter({ routes })
```

**5. Install into the app**

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

**6. Start the dev server**: the plugin scans the `pages` directory and produces the `pages.json` page entries, the `routes.gen.uts` route table, and the type declarations ([Generated Files](./file-conventions#generated-files)). Commit the generated files **to git** — the HBuilderX CLI compile chain does not guarantee the plugin runs first.

## Modifying routes

When you need to make manual adjustments to the generated result:

- `routeGen` / `pagesGen` mode: edit the `defineUniPage` declaration in the page directly; fields appended to existing entries and entire hand-added custom routes are automatically preserved on regeneration thanks to `preserveRouteChanges` (on by default);
- `routesGen` mode: inject name / meta / beforeEnter through the `routes.ext.uts` extension declaration file.

Both are detailed in [Extending Routes](./extending-routes). Note: **hand-editing fields in the generated files that are "derived from page declarations" is pointless** — the next regeneration refreshes them; change the declaration source instead.

## Options Reference

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

**`router` (`routesGen` extras)**:

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `extensions` | `string \| false?` | `'routes.ext.uts'` | Extension declaration file path (relative to project root), `false` to disable extension merging |

> Option split across the three plugins: the **common options** (`pagesJsonPath` / `watch` / `verbose` / `errorStrategy`) exist on all three; the `pages` section belongs to `routeGen` / `pagesGen`, and the `router` section to `routeGen` / `routesGen` (`extensions` is `routesGen`-only).

## Relationship with @meng-xi/vite-plugin (generateUni)

The `defineUniPage` macro / `<route-config>` block syntax stays consistent with MengXi Studio's `generateUni`, so the two plugins are interchangeable. Differences:

- `generateUni` targets projects not using unix-router; this plugin serves unix-router users with the same workflow and ships inside the router package (no extra install)
- This plugin extends unix-router-specific fields: `name`, extended `meta`, `beforeEnter`

## Next Steps

- [File Conventions](./file-conventions) — the defineUniPage macro / &lt;route-config&gt; block / name normalization
- [Extending Routes](./extending-routes) — preserveRouteChanges / routes.ext.uts extension declarations
- [Route Configuration](./route-config) — fields and conventions of a hand-written route table (the generation target of this plugin)
