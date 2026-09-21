# Plugin System

unix-router adopts a **core + plugins** architecture (in the style of uni-router / Swiper.js):

- The **core** does only four things: route matching, navigation execution, the guard chain, and state sync;
- **All other extended capabilities** (page params, page-to-page communication, navigation animations, uni API interception, etc.) are fully pluginized and registered on demand (**opt-in**).

Without a plugin registered, the corresponding capability simply does not exist: the core package stays lean and stable; using an unregistered capability throws a `PLUGIN_REQUIRED` error that explicitly guides you instead of failing silently.

## Built-in Plugins at a Glance

| Plugin | name | Capability | Companion option | Details |
| --- | --- | --- | --- | --- |
| `ParamsPlugin` | `params` | Page parameter passing (in-memory / persisted) | `paramsPersistent` | [Parameter Passing](./params) |
| `EventsPlugin` | `events` | Page-to-page communication (`events` listener map + EventChannel callbacks) | — | [Page-to-Page Communication](./events) |
| `AnimationPlugin` | `animation` | Navigation window animation (native pass-through / H5 WAAPI) | `animation` | [Navigation Animation](./animation) |
| `InterceptorPlugin` | `interceptor` | Intercepts the uni native navigation APIs, sinking guards down to the uni API layer | `interceptUniApi` | [uni API Interception](./interceptor) |

## Registering Plugins

A plugin is an instance of the `RouterPlugin` abstract class; you **must instantiate it** when registering:

```ts
import { createRouter, ParamsPlugin, EventsPlugin, AnimationPlugin, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new ParamsPlugin(), new EventsPlugin(), new AnimationPlugin(), new InterceptorPlugin()],
	interceptUniApi: true // requires InterceptorPlugin, defaults to false
})
```

::: warning The old syntax is deprecated
`plugins: [ParamsPlugin]` (passing the class directly) is the legacy syntax and has been deprecated. Plugins are implemented as abstract classes; always pass an instance `new XxxPlugin()`.
:::

Plugins are installed in array order. At install time they register hooks through the `PluginContext` and are injected into each stage of the navigation flow. When a companion option is set but the corresponding plugin is not registered (e.g. `interceptUniApi: true` without `InterceptorPlugin`), the option is ignored with a warning.

## The RouterPlugin Abstract Class

To write a custom plugin, extend `RouterPlugin` and implement the `name` field plus the abstract `install(context, options)` method:

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type { PluginContext, RouterOptions } from '@meng-xi/unix-router'

class MyPlugin extends RouterPlugin {
	name = 'my-plugin'

	install(context: PluginContext, options: RouterOptions): void {
		// register hooks via the context, read plugin options from options
	}
}
```

- `name: string`: the plugin's unique identifier. `hasPlugin(name)` uses it to check registration status (the `params` / `events` `PLUGIN_REQUIRED` pre-checks also match by name).
- `install(context, options)`: called once at install time, inside `createRouter` before any navigation happens.

## PluginContext: 8 Hooks

Each hook is invoked at a fixed point in the navigation flow. Examples for every hook:

| hook | Signature | Trigger point |
| --- | --- | --- |
| `onEnrichLocation` | `(location: RouteLocationRaw) => RouteLocationRaw` | **Before** `matcher.resolve()`, enriches the raw route location (e.g. injecting internal keys). Runs in a chain — the previous hook's return value is the next one's input |
| `onAfterResolve` | `(enrichedLocation: RouteLocationRaw, pluginData: PluginData) => void` | After resolve, before the guard chain. Extracts plugin data from the enriched location into `pluginData` |
| `onPrepareNavigation` | `(ctx: NavigationPrepareContext) => void` | **Before** the uni API call. May modify the navigation URL's `ctx.query` and `ctx.options` (e.g. appending internal keys, rewriting animation params) |
| `onBeforeNavigation` | `(ctx: NavigationPrepareContext) => Promise<void> \| void` | Just before the uni API is actually invoked. **May be async** (e.g. on H5, back waits for the exit animation to finish); multiple hooks run **serially** |
| `onCompleteNavigation` | `(ctx: NavigationCompleteContext) => void` | After the uni API call **succeeds** and the page stack is confirmed. May extend the navigation result `ctx.result` |
| `onNavigationAbort` | `(pluginData: PluginData) => void` | Runs cleanup when a navigation aborts or fails (exceptions are swallowed and never interrupt the failure flow) |
| `onRouteSync` | `(query: Map<string, string>, params: Map<string, string>) => void` | During route state sync. Extracts plugin data from the URL query (e.g. rebuilding params from the `__params__` key); internal keys should be removed here |
| `onAppInstall` | `(app: any) => void` | Fired when `router.install()` is called (`app.use(router)`); may register app-level cleanup logic |

::: tip PluginData
`PluginData` is simply `Map<string, any>`, shared across stages within a single navigation: `onAfterResolve` writes → `onPrepareNavigation` / `onBeforeNavigation` / `onCompleteNavigation` read → `onNavigationAbort` cleans up. A redirect reuses the same `pluginData`.
:::

## Context Members

Besides the hook registration methods, `PluginContext` exposes:

| Member | Type | Description |
| --- | --- | --- |
| `currentRoute` | `RouteLocation` | The current route location (read-only getter, fetched in real time via an injected reader) |
| `resolve` | `(location: RouteLocationRaw) => RouteLocation` | Resolves a route location into a full `RouteLocation` (equivalent to `router.resolve`) |
| `router` | `any` | Reference to the router instance. Declared as `any` to avoid native-side interface degradation across files; cast with `as Router` inside plugins as needed |
| `paramsManager` | `any` | The core's shared `ParamsManager` instance (used by `ParamsPlugin`; cast with `as ParamsManager` as needed) |
| `hasPlugin` | `(name: string) => boolean` | Checks whether a given plugin is registered |

## Complete Custom Plugin Example

Taking a "navigation analytics" plugin as an example, demonstrating class inheritance, multi-hook cooperation, and passing values across stages via `pluginData`:

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type {
	PluginContext,
	PluginData,
	RouterOptions,
	NavigationPrepareContext,
	NavigationCompleteContext
} from '@meng-xi/unix-router'

/** Analytics plugin: stamps each navigation with a start timestamp and reports on success */
class AnalyticsPlugin extends RouterPlugin {
	name = 'analytics'

	override install(context: PluginContext, options: RouterOptions): void {
		// 1. Record the start time before resolve (mounting it into pluginData in afterResolve is safer)
		context.onEnrichLocation((location) => {
			// If you need to append an internal key to the navigation URL, return the enriched location here
			return location
		})

		// 2. After resolve: extract data, record the navigation start time
		context.onAfterResolve((enrichedLocation, pluginData) => {
			pluginData.set('startedAt', Date.now())
		})

		// 3. Before the uni API call: rewrite the navigation query (optional; example appends a timestamp marker)
		context.onPrepareNavigation((ctx: NavigationPrepareContext) => {
			ctx.query.set('_t', String(Date.now()))
		})

		// 4. Report after a successful navigation
		context.onCompleteNavigation((ctx: NavigationCompleteContext) => {
			const startedAt = ctx.pluginData.get('startedAt')
			reportAnalytics(ctx.to.path, startedAt as number)
		})

		// 5. Clean up on abort / failure
		context.onNavigationAbort((pluginData: PluginData) => {
			pluginData.clear()
		})
	}
}

// Register: pass an instance
const router = createRouter({ routes, plugins: [new AnalyticsPlugin()] })
```

## Execution Order

At `createRouter` time, plugins are installed in the `plugins` array order. During a forward navigation, hooks execute by stage:

```
push / replace / relaunch
  → onEnrichLocation (enrich the raw location)
  → matcher.resolve (resolve the target)
  → onAfterResolve (extract plugin data into pluginData)
  → beforeEach → beforeEnter → beforeResolve (guard chain)
  → onPrepareNavigation (rewrite query / options)
  → onBeforeNavigation (may be async, serial)
  → uni API (navigateTo / redirectTo / reLaunch / switchTab)
  → page stack top confirmed → onCompleteNavigation → afterEach
  → (any stage fails) onNavigationAbort → afterEach(failure) → onError
```

## Platform Notes (UTS Strong Typing)

::: warning On non-vapor platforms (Kotlin / Swift), plugins must be implemented as classes
On platforms compiled to Kotlin / Swift, **object literals containing methods are inferred as `UTSJSONObject`** and cannot work as plugins. Therefore:

- Plugins must be implemented by **extending `RouterPlugin` with a class**; hook registration is done through class methods;
- Pass an **instance** when registering: `plugins: [new MyPlugin()]` — never the class itself or an object literal;
- Likewise, `PluginContext` is a class, not a type.
:::

## Next Steps

- [Navigation Flow](./navigation-flow) — the exact position of each hook in the full navigation timeline
- [RouterOptions](../api/type-router-options) — the `plugins` option and each plugin's companion options
- [Parameter Passing](./params) / [Page-to-Page Communication](./events) / [Navigation Animation](./animation) / [uni API Interception](./interceptor) — complete usage of the four built-in plugins
