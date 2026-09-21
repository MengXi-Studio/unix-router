# useRoute()

`useRoute(): RouteLocation` returns the current [RouteLocation](./type-route-location). It must be called inside a component's `setup` (or `script setup`).

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
```

## Return Value

It returns the **global reactive object**; access its fields directly — **there is no `.value`**.

::: warning Difference from vue-router
In vue-router, `route` is a ref-style object where the template can use `route.path` but the script needs `computed` and similar helpers to access the raw value. This library's `useRoute()` returns the global reactive `RouteLocation` itself; both script and template **access it directly as `route.path`**, with no `.value` and no unwrapping needed.
:::

## Fields

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string` | Page path (with a leading slash, e.g. `/pages/index/index`) |
| `name` | `string \| null` | Named route name; `null` when unnamed |
| `meta` | `RouteMeta` | Route meta (`title` / `isTab` / `requireAuth`) |
| `query` | `Map<string, string>` | Query parameters; read with `.get(key)` |
| `params` | `Map<string, string>` | Route params (requires `ParamsPlugin`) |
| `fullPath` | `string` | Full path (`path` + serialized query) |
| `hash` | `string` | Always `''` (uni-app x does not support hash; field kept) |
| `matched` | `RouteConfig[]` | Matched route records (a single-record array under the flat model) |

## Reading Route Info

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()

// Access fields directly (no .value)
console.log(route.path)            // /pages/detail/detail
console.log(route.fullPath)        // /pages/detail/detail?id=1
console.log(route.query.get('id')) // '1'
console.log(route.name)            // 'detail' (null when unnamed)
</script>

<template>
	<view class="page">
		<text>Current page: {{ route.path }}</text>
	</view>
</template>
```

::: tip Both query and params are Maps
In uni-app x, query travels in the URL as a string; both `route.query` / `route.params` are `Map<string, string>`, read with `.get(...)`, checked with `.has(...)`.
:::

## Update Timing

- **On navigation completion**: updated automatically after the uni API call succeeds and the target page is confirmed via the page stack top (written after stripping plugin-internal keys).
- **On `syncRoute()`**: behaviors that bypass the router, such as physical back and tab switching, are rebuilt from the page stack by `syncRoute()` (triggered automatically by the `onShow` mixin registered by `app.use(router)` on H5; on native platforms it is recommended to call it manually in each page's `onShow`).

## onShow Timing Hint

A page's `onShow` may run **before the route state syncs**. If you need to deterministically read this page's parameters inside `onShow`, prefer reading the native `options` in the page's `onLoad(options)` (i.e. the URL query), or manually call `router.syncRoute()` once before reading `route`.

## Related APIs

- [RouteLocation](./type-route-location)
- [useRouter()](./use-router)
- [Router Instance - currentRoute](./router-instance#currentroute)
