# File Conventions

The build-time plugin treats every page file as the source of truth: declare a small config near your page code, and the plugin generates the `pages.json` entries and the route table from it. This page is the reference for all declaration syntax; see [File-Based Routing](./file-based-routing) for integration.

## The defineUniPage Macro

Declare it at the top of each page's `<script setup>` (no import needed; stripped at compile time with same-line-count comment placeholders that keep line numbers stable):

```vue
<!-- pages/mine/mine.uvue -->
<script setup>
	// Page declaration macro: stripped at compile time by @meng-xi/unix-router/vite-plugin
	defineUniPage({
		title: '我的',
		name: 'mine',
		isTab: true,
		tab: { order: 2, text: '我的', iconPath: 'static/tabbar/mine.png', selectedIconPath: 'static/tabbar/mine-active.png' }
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

A subpackage page with a per-route guard:

```vue
<!-- pages-sub/setting/setting.uvue —— subpackage page + per-route guard -->
<script setup>
	defineUniPage({
		title: '设置',
		name: 'setting',
		beforeEnter: (to: any, from: any): any => {
			// Note: must be self-contained — cannot reference variables inside the page
			// (the generated file and the page file are two separate scopes)
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	})
</script>
```

**Rules**: at most once per page, at the top level of the script. Function fields such as `beforeEnter` are injected into the generated file verbatim (JSON cannot express functions; UTS expressions can), and type safety is enforced by the UTS compile chain.

## The &lt;route-config&gt; Custom Block

If you prefer not to use the macro, or the declaration is long, use an SFC custom block (same effect as the macro, lower priority):

```vue
<route-config lang="jsonc">
{
	// jsonc: comments supported
	"title": "商品详情",
	"name": "goods-detail",
	"meta": { "requireAuth": true }
}
</route-config>

<route-config lang="uts">
{
	title: '确认订单',
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

## Name Normalization

| Scenario | Result |
| --- | --- |
| `name` declared via macro/block | The declared value is used |
| Omitted, last segment `mine` is unique | Last-segment camelCase: `pages/mine/mine` → `mine` |
| Last-segment collision (e.g. `pages/a/index` and `pages/b/index`) | Full-path camelCase fallback: `aIndex` / `bIndex` |
| Still conflicting in extreme cases | Handled by `errorStrategy`: `strict` (default) aborts the build with a listing / `warn` logs and skips |

Generates `route-name.gen.d.ts` (a WEB-side `RouteNameMap` module augmentation), giving literal autocomplete for name-based navigation (see [The Named Route Type RouteName](./route-config#the-named-route-type-routename)).

## Generated Files

| File | Description |
| --- | --- |
| `pages.json` | Auto-generated page entries + tabBar + subPackages; **hand-written non-page fields (globalStyle, uniIdRouter, etc.) are merged and preserved** |
| `routes.gen.uts` | The route table `RouteConfig[]`; in `router.uts`: `import { routes } from './routes.gen.uts'` |
| `route-name.gen.d.ts` | Literal types for `RouteNameMap` (WEB-side editor hints, optional) |
| `define-uni-page.d.ts` | Macro type declaration (editor hints, optional) |

Commit the generated files **to git**: the HBuilderX CLI compile chain does not guarantee the plugin runs first; watch/HMR are development-time conveniences only.

::: warning Common Pitfalls
- **Macro declared twice**: at most once per page; under `strict` the build aborts immediately.
- **`beforeEnter` referencing page variables**: the macro function is injected verbatim into `routes.gen.uts`, a separate scope from the page; referencing page variables breaks compilation. Guard logic must be self-contained (storage / global state).
:::

## Next Steps

- [File-Based Routing](./file-based-routing) — plugin choice and setup
- [Extending Routes](./extending-routes) — modifying the generated files / `routes.ext.uts` extension declarations
- [Route Configuration](./route-config) — fields and conventions of a hand-written route table (the generation target of this plugin)
