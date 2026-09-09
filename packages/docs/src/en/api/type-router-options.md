# RouterOptions

The initialization options for `createRouter()`.

```ts
import type { RouterOptions } from '@meng-xi/unix-router'

const options: RouterOptions = {
	routes,
	strict: true,
	guardTimeout: 10000,
	readyTimeout: 0
}
```

## Fields

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **Required**. The route configuration list; paths must match the registration in `pages.json` |
| `strict` | `boolean` | `true` | Strict mode; when enabled, unmatched named routes throw `ROUTE_NOT_FOUND` |
| `guardTimeout` | `number` | `10000` | Guard timeout (ms); `0` disables it |
| `readyTimeout` | `number` | `0` | Ready timeout (ms); `0` never times out |
| `interceptUniApi` | `boolean` | `false` | **Opt-in**. When enabled, intercepts the `uni.*` native navigation APIs (`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`); navigations that bypass the router and call these APIs directly are rerouted through `router.*` so the full guard chain runs — guards are sunk down to the uni API layer. Runtime support: Web 4.0 / WeChat 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61 |

> **Note**: On WeChat Mini Program, `<navigator>` component jumps and tabBar clicks (which do not trigger `uni.switchTab` under the hood) cannot be intercepted; cover these scenarios with an `onShow` fallback guard.

## Related APIs

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)