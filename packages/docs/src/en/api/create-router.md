# createRouter()

`createRouter(options: RouterOptions): Router` creates and returns a [Router instance](./router-instance) (adapted for uni-app x).

```ts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({
	routes, // required
	strict: true
})
```

## Parameters

`createRouter(options: RouterOptions)`

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **Required**. The route configuration list; paths must match the registration in `pages.json` |
| `strict` | `boolean` | `true` | Strict mode. When `true`, an unmatched named route throws `RouterError ROUTE_NOT_FOUND`; when `false`, only a warning is emitted and the route is handled by path |
| `guardTimeout` | `number` | `10000` | Guard timeout (ms); set to `0` to disable. On timeout a warning is printed and the navigation is aborted |
| `readyTimeout` | `number` | `0` | Router ready timeout (ms); `0` means never time out |
| `interceptUniApi` | `boolean` | `false` | **Opt-in**. When enabled, intercepts the `uni.*` native navigation APIs (`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`); external direct calls are also handed over to the router and run the full guard chain. **Requires `InterceptorPlugin`**, see [uni API Interception](../guide/interceptor) |
| `plugins` | `RouterPlugin[]` | — | Plugin list; register extended capabilities on demand. **Pass instances**: `[new ParamsPlugin(), new InterceptorPlugin()]`, see [Plugin System](../guide/plugins) |
| `paramsPersistent` | `boolean` | `false` | Whether to persist params to storage by default (automatically falls back to memory if a write fails). **Requires `ParamsPlugin`** |
| `animation` | `NavigationAnimation` | — | Global default navigation animation `{ type: AnimationType, duration?: number }` (`duration` defaults to 300ms). On App the native `animationType` is passed through (officially App-only); on H5 the plugin implements it with the Web Animations API. **Requires `AnimationPlugin`**, see [Navigation Animation](../guide/animation) |

::: warning Plugin-related options require their companion plugins
`interceptUniApi` / `paramsPersistent` depend on `InterceptorPlugin` / `ParamsPlugin` respectively — when an option is set but the corresponding plugin is not registered, the option is ignored with a warning. `animation` depends on `AnimationPlugin`; without it the option is **silently ignored** (no warning is printed).
:::

## Return Value

Returns a [Router instance](./router-instance). It does not navigate immediately; it only provides services after being installed into the Vue app via `app.use(router)`.

## Errors

- With `strict: true` (the default), resolving an unmatched **named route** throws `RouterError` (`ROUTE_NOT_FOUND`); in controlled navigation the error surfaces via Promise reject + `router.onError`.
- With `strict: false`, it degrades to a warning and the route is handled by path.

## Example

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true,
	plugins: [new ParamsPlugin(), new InterceptorPlugin()], // page params + uni navigation interception
	interceptUniApi: true, // external uni.navigateTo calls also run guards
	paramsPersistent: true, // persist params to storage
	guardTimeout: 15000 // increase the timeout when guards perform network requests
})
```

## Exported Constants

| Constant | Type | Value | Description |
| --- | --- | --- | --- |
| `DEFAULT_GUARD_TIMEOUT` | `number` | `10000` | Default guard timeout (ms); the `guardTimeout` default |
| `DEFAULT_READY_TIMEOUT` | `number` | `0` | Default router-ready timeout; the `readyTimeout` default |
| `MAX_REDIRECT_DEPTH` | `number` | `10` | Maximum redirect depth from guards; beyond it the navigation is cancelled (`CANCELLED`) to prevent infinite loops |
| `ROUTER_SYMBOL` | `string` | `'__unix_router__'` | The `provide` / `inject` pairing key (a string key, more stable across platforms) |

## Related APIs

- [Router Instance](./router-instance)
- [RouterOptions](./type-router-options)
- [RouteConfig](./type-route-config)
