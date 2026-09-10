# createRouter()

`createRouter()` creates and returns a [Router](./router-instance) instance (adapted for uni-app x).

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
| `strict` | `boolean` | `true` | Strict mode; when enabled, unmatched named routes throw a `ROUTE_NOT_FOUND` error |
| `guardTimeout` | `number` | `10000` | Guard timeout (ms); set to `0` to disable |
| `readyTimeout` | `number` | `0` | Router ready timeout (ms); `0` means never time out |
| `plugins` | `RouterPlugin[]` | — | Plugin list; register extension capabilities on demand, e.g. `[ParamsPlugin]`, `[InterceptorPlugin]` |
| `interceptUniApi` | `boolean` | `false` | **Opt-in**. When enabled, intercepts the `uni.*` native navigation APIs (`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`); guards are sunk down to the uni API layer. Requires `plugins: [InterceptorPlugin]` |
| `paramsPersistent` | `boolean` | `false` | Whether to persist params to storage by default (requires `plugins: [ParamsPlugin]`) |

## Return Value

Returns a [Router](./router-instance). It does not start navigating immediately; it only provides services after being installed into the Vue app via `app.use(router)`.

## Example

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true,
	plugins: [ParamsPlugin, InterceptorPlugin], // page params + uni navigation interception
	interceptUniApi: true, // external uni.navigateTo also runs guards
	guardTimeout: 15000 // increase the timeout when guards perform network requests
})
```

## Related APIs

- [Router](./router-instance)
- [RouterOptions](./type-router-options)
- [RouteConfig](./type-route-config)