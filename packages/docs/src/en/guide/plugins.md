# Plugin System

unix-router uses a **core + plugin** architecture (in the style of uni-router / Swiper.js):

- The **core** only provides uni-app x native capabilities such as route matching, navigation execution, guard chains, and state sync;
- **Extended capabilities** (page parameters, uni native navigation interception, etc.) are provided via plugins and registered on demand.

## Design Principles

1. **Lean core**: the core contains no business extensions, keeping it stable and lightweight.
2. **Plugin expansion**: non-core capabilities are all registered as plugins in `createRouter({ plugins: [...] })`.
3. **Zero intrusion**: a feature is unavailable until its plugin is registered; using an unregistered feature throws a `PLUGIN_REQUIRED` error that clearly guides you.
4. **Composable**: plugins are installed in array order and register hooks through `PluginContext`, injecting into each stage of the navigation flow.

## Quick Start

```ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	strict: true,
	plugins: [ParamsPlugin, InterceptorPlugin], // enable: page params + uni navigation interception
	interceptUniApi: true // InterceptorPlugin switch, defaults to false
})
```

> Only register the plugins you need. Using a corresponding feature without registering its plugin throws `PLUGIN_REQUIRED`.

## Built-in Plugins

| Plugin | name | Capability | Companion option |
| --- | --- | --- | --- |
| `ParamsPlugin` | `params` | Page parameter passing (`params`, in-memory / persisted) | `paramsPersistent` |
| `InterceptorPlugin` | `interceptor` | Intercepts the uni native navigation APIs, sinking guards down to the uni API layer | `interceptUniApi` |

### ParamsPlugin: page params

uni-app x uses a static page model, so URLs are not suitable for carrying complex objects. ParamsPlugin uses an "**in-memory store + internal `__params__` key**" scheme: the source page stores `params` into the manager and generates a key, and the target page retrieves them by that key.

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [ParamsPlugin] })

// Pass string params (through the internal key channel)
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})

// Read on the target page (route.params is rebuilt by ParamsPlugin during state sync)
const route = useRoute()
console.log(route.params.get('id')) // '1024'
```

**Persisting to storage**: params live in memory by default; enable persistence to keep them across refresh / re-entry:

```ts
const router = createRouter({
	routes,
	plugins: [ParamsPlugin],
	paramsPersistent: true // persists all params to uni storage by default
})
```

::: warning ParamsPlugin must be registered
Calling a navigation with `params` without registering `ParamsPlugin` throws `PLUGIN_REQUIRED`:

```
Using params requires registering ParamsPlugin: createRouter({ plugins: [ParamsPlugin] })
```
:::

### InterceptorPlugin: uni navigation interception

Calling `uni.navigateTo` directly **bypasses the route guards**. When interception is enabled, external direct calls are also handed over to `router.*` so the full guard chain runs — guards are "sunk down" to the uni API layer.

```ts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [InterceptorPlugin],
	interceptUniApi: true
})

// Now both are equivalent and guards always take effect:
await router.push({ name: 'about' })        // goes through router, guards take effect
uni.navigateTo({ url: '/pages/about/about' }) // intercepted → handed to router, guards take effect
```

**Minimum HBuilderX versions required per platform** (official addInterceptor compatibility table):

| Platform | Minimum HBuilderX version |
| --- | --- |
| Web | 4.0 |
| WeChat Mini Program | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

::: warning Does not affect the router's own calls
The interceptor only applies to **external direct calls**; the `uni` calls issued internally by `router.push/replace/relaunch/back` are not re-intercepted (distinguished via an internal marker).
:::

Intercepted APIs: `navigateTo / redirectTo / switchTab / reLaunch / navigateBack`. It also exposes `markRouterCall` / `installInterceptors` / `removeInterceptors` for low-level use.

## Plugin Context

Each plugin registers hooks through `context` in `install(context, options)`, and the router invokes them at each stage of the navigation flow:

| hook | Timing | Purpose |
| --- | --- | --- |
| `onEnrichLocation` | before `resolve` | Enrich the raw route location (e.g. inject an internal key) |
| `onAfterResolve` | after resolve, before guards | Extract plugin data from the enriched location |
| `onPrepareNavigation` | before the uni API call | Modify the navigation URL query and options |
| `onCompleteNavigation` | after the uni API call succeeds | Extend the navigation result |
| `onNavigationAbort` | when a navigation aborts / fails | Clean up plugin resources |
| `onRouteSync` | during state sync | Rebuild plugin data from the URL query |
| `onAppInstall` | on `router.install()` | Register app-level cleanup logic |

`context` also exposes read-only members: `currentRoute`, `resolve()`, `router`, `paramsManager`, `hasPlugin()`.

## Custom Plugin

Implement the `RouterPlugin` interface (`name` + `install`):

```ts
import type { RouterPlugin, PluginContext, RouterOptions } from '@meng-xi/unix-router'

/** Analytics plugin: reports the route on every completed navigation */
const AnalyticsPlugin: RouterPlugin = {
	name: 'analytics',

	install(context: PluginContext, options: RouterOptions) {
		// 1. Before navigation starts: inject an internal key into the target (optional)
		context.onEnrichLocation(location => location)

		// 2. After resolve: record the target
		context.onAfterResolve((enrichedLocation, pluginData) => {
			pluginData.set('timestamp', Date.now())
		})

		// 3. Before the uni API call: rewrite the query (optional)
		context.onPrepareNavigation(ctx => {
			// ctx.query.set('_t', String(Date.now()))
		})

		// 4. Report after a successful navigation
		context.onCompleteNavigation(ctx => {
			const ts = ctx.pluginData.get('timestamp')
			reportAnalytics(ctx.to.path, ts as number)
		})

		// 5. Clean up on navigation abort
		context.onNavigationAbort(pluginData => {
			pluginData.clear()
		})
	}
}

// register
const router = createRouter({ routes, plugins: [AnalyticsPlugin] })
```

::: tip pluginData
`pluginData` is a `Map`, written in the `onAfterResolve` stage and read in `onPrepareNavigation` / `onCompleteNavigation` / `onNavigationAbort`, letting a plugin pass data across stages.
:::

## Execution Order

When `createRouter` is invoked, plugins are installed in `plugins` array order; during a navigation the hooks run stage by stage:

```
push → enrichLocation → matcher.resolve → afterResolve
     → beforeEach → beforeEnter → beforeResolve
     → prepareNavigation → uni API → (success) setRoute + completeNavigation + afterEach
     → (failure / abort) navigationAbort
```

## Next Steps

- [Navigation Guards](./guards) — where guards sit in the plugin flow
- [Platform Compatibility](./compatibility) — addInterceptor version requirements per platform
- [Recipes](./recipes) — auth, permissions, analytics and other complete solutions
- [API Reference](../api/create-router) — the `plugins` / `interceptUniApi` / `paramsPersistent` options