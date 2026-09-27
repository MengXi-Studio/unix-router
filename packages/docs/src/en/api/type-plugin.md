# RouterPlugin / PluginContext

Type reference for the plugin system: custom plugins extend `RouterPlugin` and register hooks through `PluginContext`. For usage and complete examples see [Plugin System](../guide/plugins).

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type {
	PluginContext,
	PluginData,
	PluginNavigationMode,
	NavigationPrepareContext,
	NavigationCompleteContext
} from '@meng-xi/unix-router'
```

## RouterPlugin

Abstract base class for plugins. **Must be implemented by extending with a class and registered as an instance** (on Kotlin / Swift, object literals containing methods are inferred as `UTSJSONObject` and cannot work as plugins):

```ts
abstract class RouterPlugin {
	/** Plugin name (matched by hasPlugin(name) and the PLUGIN_REQUIRED pre-check) */
	name: string
	/** Install the plugin: register hooks via context, read plugin options from options; called once inside createRouter */
	abstract install(context: PluginContext, options: RouterOptions): void
}
```

## PluginContext

The hook registration interface the router exposes to plugins (carried by a class):

**Hook registration (8)**:

| Member | Signature | Trigger point |
| --- | --- | --- |
| `onEnrichLocation` | `(hook: (location: RouteLocationRaw) => RouteLocationRaw) => void` | Enriches the raw location **before** `matcher.resolve()`; runs in a chain |
| `onAfterResolve` | `(hook: (enrichedLocation: RouteLocationRaw, pluginData: PluginData) => void) => void` | After resolve, before the guard chain; extracts plugin data |
| `onPrepareNavigation` | `(hook: (ctx: NavigationPrepareContext) => void) => void` | Before the uni API call; may modify `ctx.query` / `ctx.options` |
| `onBeforeNavigation` | `(hook: (ctx: NavigationPrepareContext) => Promise<void> \| void) => void` | Just before the uni API is actually invoked; may be async, multiple hooks run serially |
| `onCompleteNavigation` | `(hook: (ctx: NavigationCompleteContext) => void) => void` | After the uni API succeeds and the page stack is confirmed; may extend `ctx.result` |
| `onNavigationAbort` | `(hook: (pluginData: PluginData) => void) => void` | Cleans up when a navigation aborts / fails (exceptions are swallowed) |
| `onRouteSync` | `(hook: (query: Map<string, string>, params: Map<string, string>) => void) => void` | During route state sync; extracts plugin data from the URL query |
| `onAppInstall` | `(hook: (app: any) => void) => void` | Fired when `app.use(router)` is called |

**Context members**:

| Member | Type | Description |
| --- | --- | --- |
| `currentRoute` | `RouteLocation` | The current route location (read-only getter, fetched in real time) |
| `resolve` | `(location: RouteLocationRaw) => RouteLocation` | Resolves a route location (equivalent to `router.resolve`) |
| `router` | `any` | Reference to the router instance (avoids native-side interface degradation across files; cast with `as Router` inside plugins as needed) |
| `paramsManager` | `any` | The core's shared `ParamsManager` instance (cast with `as ParamsManager` as needed) |
| `hasPlugin` | `(name: string) => boolean` | Checks whether a given plugin is registered |

## PluginData

```ts
type PluginData = Map<string, any>
```

The data container shared by plugin stages within a single navigation: `onAfterResolve` writes → `onPrepareNavigation` / `onBeforeNavigation` / `onCompleteNavigation` read → `onNavigationAbort` cleans up. A redirect reuses the same `pluginData`.

## NavigationPrepareContext

The context of `onPrepareNavigation` / `onBeforeNavigation`:

| Member | Type | Description |
| --- | --- | --- |
| `to` / `from` | `RouteLocation` | Target / source route |
| `mode` | `PluginNavigationMode` | `'push' \| 'replace' \| 'relaunch' \| 'back'` |
| `pluginData` | `PluginData` | Data shared across stages |
| `query` | `Map<string, string>` | The query of the actual navigation URL (**mutable**: internal keys can be added) |
| `options` | `UniNavigationOptions` | The uni navigation options (**mutable**: animation params, etc., can be changed) |

## NavigationCompleteContext

The context of `onCompleteNavigation`:

| Member | Type | Description |
| --- | --- | --- |
| `to` | `RouteLocation` | The target route |
| `mode` | `PluginNavigationMode` | The navigation mode |
| `pluginData` | `PluginData` | Data shared across stages |
| `result` | `RouteLocation` | The navigation result (**mutable**: can be extended) |

## Built-in Plugins and Helper Exports

| Export | Description | Guide |
| --- | --- | --- |
| `ParamsPlugin` / `createParamsManager` | Page parameter passing (in-memory / persisted) | [Parameter Passing](../guide/params) |
| `EventsPlugin` / `eventBus` | Page-to-page communication (events registry + EventChannel) | [Page-to-Page Communication](../guide/events) |
| `AnimationPlugin` | Navigation window animation (App native pass-through / H5 WAAPI) | [Navigation Animation](../guide/animation) |
| `InterceptorPlugin` / `installInterceptors` / `removeInterceptors` | Intercepts the uni native navigation APIs | [uni API Interception](../guide/interceptor) |

## Related APIs

- [Plugin System](../guide/plugins) — architecture, registration, and a complete custom plugin example
- [RouterOptions](./type-router-options) — the `plugins` option and each plugin's companion options
- [Navigation Flow](../guide/navigation-flow) — the exact position of each hook in the navigation timeline
