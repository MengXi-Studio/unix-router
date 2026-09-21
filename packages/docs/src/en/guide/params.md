# Passing Parameters

There are two ways to pass data between pages: **query** (URL query parameters, built into the router) and **params** (passed via the associated store of `ParamsPlugin`). Pick the right one first, then write code.

## query vs params: Which to Choose

| Dimension | query | params |
| --- | --- | --- |
| URL visibility | Visible (appended to `fullPath`) | Invisible to users (the internal key is stripped) |
| Survives refresh / bookmark / share | ✅ Still there | ❌ Bound to this navigation only |
| Value type | `Map<string, string>` | `Map<string, string>` (content must be JSON-serializable) |
| Plugin required | No | Yes (`ParamsPlugin`) |
| Typical use cases | Short values: list filters, ids, source tags | Detail-page data bundles, structured parameters, data you don't want in the address bar |

## query: URL Query Parameters

```ts
// sender: inline in a string, or carried on an object
await router.push('/pages/detail/detail?id=1024&from=home')
// or
await router.push({
	name: 'detail',
	query: new Map<string, string>([['id', '1024'], ['from', 'home']])
})
```

query is appended to the URL; the target page reads it via `route.query`:

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
route.query.get('id')     // '1024'
route.query.has('from')   // true
```

## params: Associated-Store Passing (ParamsPlugin)

### Registering the Plugin

params depend on `ParamsPlugin`, registered as an **instance** when creating the router:

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()]
})
```

### Sending and Reading

```ts
// source page
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024'], ['info', 'hello']])
})
```

Read back in the target page `pages/detail/detail.uvue`:

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
const id = route.params.get('id')     // '1024'
const info = route.params.get('info') // 'hello'
</script>

<template>
	<view class="page">
		<text class="title">{{ id }}</text>
		<text class="desc">{{ info }}</text>
	</view>
</template>
```

### Example: Passing an Object Array from a List Page to a Detail Page

params carry only `Map<string, string>`. Structured data such as objects / arrays must first be serialized with `JSON.stringify` and restored on the target page with `JSON.parse<T>`:

```vue
<!-- list page pages/list/list.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

type GoodsItem = {
	id: number
	name: string
}

const router = useRouter()

const openDetail = () => {
	const goods: GoodsItem[] = [{ id: 1, name: 'Mechanical Keyboard' }, { id: 2, name: 'Wireless Mouse' }]
	const params = new Map<string, string>()
	// serialize the structured data into a JSON string (params content must be JSON-serializable)
	params.set('goods', JSON.stringify(goods))
	params.set('from', 'list')
	router.push({
		path: 'pages/goods/goods',
		params: params
	})
}
</script>

<template>
	<view class="page">
		<button @click="openDetail"><text>Open the goods page with an object array</text></button>
	</view>
</template>
```

```vue
<!-- goods page pages/goods/goods.uvue -->
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

type GoodsItem = {
	id: number
	name: string
}

const route = useRoute()

// Map values may be null; check before parsing
const raw = route.params.get('goods')
let goodsList: GoodsItem[] = []
if (raw !== null) {
	const parsed = JSON.parse<GoodsItem[]>(raw)
	if (parsed !== null) {
		goodsList = parsed
	}
}
const firstName = goodsList.length > 0 ? goodsList[0].name : 'No data'
</script>

<template>
	<view class="page">
		<text class="title">First item: {{ firstName }}</text>
	</view>
</template>
```

### How It Works

- params are carried as `Map<string, string>` and the content must be **JSON-serializable** (non-serializable values are dropped at store time with a warning)
- On navigation, the params are stored in the ParamsManager and an associated key is generated; the navigation URL is built with the internal query key `__params__`
- When the target page syncs route state (`syncRoute`), it reads the params back by key and **strips `__params__`** — users never see it in the URL or in `route.query`

### Persistence with paramsPersistent

Params are stored in memory by default. With `paramsPersistent: true` they are written to uni storage (keys carry a dedicated prefix); if the storage write fails, it automatically falls back to memory with a warning:

```ts
const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()],
	paramsPersistent: true
})
```

### Example: Reading params Across Cold Starts with paramsPersistent

With `paramsPersistent: true`, params are written to uni storage (key prefix `unixr_params_`): after the app is killed by the system and cold-starts, if the restored top-of-stack page URL still carries the internal `__params__` key, route state sync automatically reads the params back from storage — in memory-only mode they cannot be recovered after a restart.

```ts
// router/index.uts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()],
	paramsPersistent: true
})
```

Custom plugins or standalone scenarios can use `createParamsManager(true)` directly; in that case the associated key is kept by the caller (e.g. written to storage):

```ts
import { createParamsManager } from '@meng-xi/unix-router'

// create a persistent manager (independent of the router instance)
const manager = createParamsManager(true)

// store the content and get the associated key
const params = new Map<string, string>()
params.set('draft', 'unsubmitted form draft')
const key = manager.set(params)

// persist the key yourself; from then on (including after a cold start) it can be read back by key at any time; call remove to clean up when done
uni.setStorageSync('draft_key', key)
const restored = manager.peek(key)
```

::: warning Using params without the plugin makes navigation fail
If you use `params` without registering `ParamsPlugin`, the navigation immediately rejects with `PLUGIN_REQUIRED` (error code 256). When you see this error, check that `createRouter`'s `plugins` includes `new ParamsPlugin()`.
:::

### Advanced: ParamsManager

The plugin also exports `createParamsManager(defaultPersistent)` and `ParamsManager` (`set(params, persistent?) → key` / `peek(key)` / `remove(key)`), so custom plugins can reuse the same "store + key association" protocol; see [Plugin System](./plugins) for details.

## query Parsing Utilities

query values are always strings, and manual conversion is error-prone under UTS strict typing. Three atomic reader utilities are built in:

```ts
import { queryInt, queryNumber, queryBool } from '@meng-xi/unix-router'

const route = useRoute()

const page = queryInt(route.query, 'page', 1)      // integer; missing/empty/NaN → 1 (defaultValue defaults to 0)
const price = queryNumber(route.query, 'price')    // number; missing/empty/NaN → 0
const enabled = queryBool(route.query, 'enabled')  // boolean; '' / '0' / 'false' → false
```

| Function | Signature | Behavior |
| --- | --- | --- |
| `queryInt` | `(query, key, defaultValue = 0)` | Returns the default on parse failure (missing key, empty string, NaN) |
| `queryNumber` | `(query, key, defaultValue = 0)` | Same as above, floating point |
| `queryBool` | `(query, key, defaultValue = false)` | `''` / `'0'` / `'false'` → `false`; any other non-empty value → `true` |

## Notes

- **Use params, not the URL, for large data**: URL length is limited on every platform. For long content (a whole form, large JSON), pass it via the ParamsPlugin associated store instead of stuffing it into query.
- **Read timing**: a page's `onShow` may fire before the route state sync, at which point `route.params` / `route.query` have not been rebuilt yet. Prefer reading the launch query directly in the page's `onLoad(options)`, or call `router.syncRoute()` before reading in `onShow` (see [Composables](./composables#route-state-sync)).
- **params are one-shot**: they exist for "this navigation"; do not use them as persistent state. For cross-session state, use storage or global state.

## API Reference

### ParamsPlugin

A `RouterPlugin` subclass — the page-params passing plugin.

| Member | Signature | Description |
| --- | --- | --- |
| `name` | constant `'params'` | Plugin name; after registration it can be detected via `hasPlugin('params')` |
| `constructor()` | no parameters | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | Creates the ParamsManager according to `options.paramsPersistent` and registers four hooks: enrichLocation / afterResolve / prepareNavigation / routeSync |

Registration (must be instantiated; on non-JS targets plugins must be implemented as classes — an object literal containing methods gets inferred as UTSJSONObject):

```ts
plugins: [new ParamsPlugin()]
```

### createParamsManager

```ts
function createParamsManager(defaultPersistent: boolean): ParamsManager
```

| Parameter | Type | Description |
| --- | --- | --- |
| `defaultPersistent` | `boolean` | Global default persistence strategy: `true` writes to uni storage, `false` keeps params in memory |

### ParamsManager

```ts
class ParamsManager {
	constructor(defaultPersistent: boolean)
	setDefaultPersistent(persistent: boolean): void
	set(params: ParamObject, persistent?: boolean | null): string
	peek(key: string): ParamObject | null
	remove(key: string): void
}
```

where `ParamObject = Map<string, string>`.

| Member | Parameters | Return value | Description |
| --- | --- | --- | --- |
| `constructor` | `defaultPersistent: boolean` | — | Sets the default persistence strategy |
| `setDefaultPersistent` | `persistent: boolean` | `void` | Changes the global default persistence strategy |
| `set` | `params: ParamObject`; `persistent?: boolean \| null` (defaults to `null`, following the global strategy) | `string` | Stores the params and generates an associated key prefixed with `pk_`; content must be JSON-serializable, otherwise the value is dropped with a warning |
| `peek` | `key: string` | `ParamObject \| null` | Reads back by key (memory first, storage as fallback); does not clean up, so the navigation resolve phase can reuse it |
| `remove` | `key: string` | `void` | Deletes the key from both memory and storage |

### RouterOptions.paramsPersistent

```ts
paramsPersistent?: boolean // default false
```

When enabled, params handled by ParamsPlugin are written to uni storage; if `true` is set without ParamsPlugin registered, a warning is logged at install time and the option is ignored.

### Error Behavior

| Condition | Behavior |
| --- | --- |
| Navigation carries `params` without ParamsPlugin registered | The navigation rejects with a `NavigationFailure`, error code `PLUGIN_REQUIRED` (`256`), with the message "using params requires registering ParamsPlugin" |
| `paramsPersistent: true` without ParamsPlugin registered | A warning is logged at install time and the option is ignored |
| params contain non-JSON-serializable values | Those values are dropped at store time with a warning |

### Platform Notes

- The internal query key is `__params__` (constant `PARAMS_QUERY_KEY`); it is stripped during route state sync and never appears in the URL or in `route.query`;
- Persistent storage keys carry the prefix `unixr_params_` (constant `PARAMS_STORAGE_PREFIX`); if a storage write fails, it falls back to memory automatically with a warning;
- `peek` read order: the in-memory Map first; on a miss, storage is read.

## Next Steps

- [Composables](./composables) — read timing of useRoute and state sync
- [Inter-Page Communication](./events) — the two-way events channel
- [Plugin System](./plugins) — ParamsPlugin internals and custom plugins
