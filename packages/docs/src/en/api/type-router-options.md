# RouterOptions

The initialization options for `createRouter()`.

```ts
import type { RouterOptions } from '@meng-xi/unix-router'

const options: RouterOptions = {
	routes,
	strict: true,
	plugins: [new ParamsPlugin()],
	guardTimeout: 10000,
	readyTimeout: 0
}
```

## Fields

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **Required**. The route configuration list; paths must match the registration in `pages.json` |
| `strict` | `boolean` | `true` | Strict mode. When `true`, an unmatched named route throws `RouterError ROUTE_NOT_FOUND`; when `false`, only a warning is emitted and the route is handled by path |
| `guardTimeout` | `number` | `10000` | Guard timeout (ms); `0` disables it. On timeout a warning is printed and the navigation is aborted |
| `readyTimeout` | `number` | `0` | Ready timeout (ms); `0` means never time out |
| `interceptUniApi` | `boolean` | `false` | **Opt-in**. When enabled, intercepts the `uni.*` native navigation APIs (`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`); external direct calls are also handed to `router.*` and run the full guard chain. **Requires `InterceptorPlugin`**. Subject to runtime version support: Web 4.0 / WeChat 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61; it automatically downgrades with a warning when missing |
| `plugins` | `RouterPlugin[]` | — | Plugin list; register extended capabilities on demand. **Pass instances**: `[new ParamsPlugin(), new InterceptorPlugin(), new EventsPlugin(), new AnimationPlugin()]`, see [Plugin System](../guide/plugins) |
| `paramsPersistent` | `boolean` | `false` | Whether to persist params to storage by default (automatically falls back to memory if a write fails). **Requires `ParamsPlugin`** |
| `animation` | `NavigationAnimation` | — | Global default navigation animation `{ type: AnimationType, duration?: number }` (`duration` defaults to 300ms). On App the native `animationType` is passed through (officially App-only); on H5 the plugin implements it with the Web Animations API. **Requires `AnimationPlugin`** |

::: warning Plugin-related options require their companion plugins
`interceptUniApi` / `paramsPersistent` depend on `InterceptorPlugin` / `ParamsPlugin` respectively; if an option is set but the corresponding plugin is not registered, the option is ignored with a warning. `animation` depends on `AnimationPlugin`; without it the option is **silently ignored** (no warning is printed).
:::

> **Note**: on WeChat Mini Program, `<navigator>` component jumps and tabBar taps (which do not trigger `uni.switchTab` under the hood) cannot be intercepted; handle those scenarios with an `onShow` fallback guard in the page.

## Related APIs

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)
