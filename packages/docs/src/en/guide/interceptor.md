# uni API Interception

The moment business code writes `uni.navigateTo`, the route guards become effectively useless. `InterceptorPlugin` uses `uni.addInterceptor` to intercept direct calls to the native navigation APIs and **hands them over to the router** to run the full guard chain, pushing guards "down" to the uni API layer.

## Motivation

The unix-router guard chain only covers navigations initiated by `router.*`. If business code (third-party components, legacy code) bypasses the router and calls `uni.navigateTo` and other native APIs directly, guard logic such as auth and analytics gets silently bypassed. With interception enabled, native direct calls enter exactly the same flow as `router.push`.

## Enabling Interception (Two Conditions)

Both conditions are **required — neither can be missing**:

```ts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()], // condition 1: register the plugin (instantiated)
	interceptUniApi: true               // condition 2: turn on the switch (default false)
})
```

- Registering the plugin without enabling `interceptUniApi` → the plugin skips installation entirely; no interceptors are installed;
- Enabling `interceptUniApi` without registering the plugin → a warning is logged and the option is ignored.

Once active, the following calls are equivalent (guards apply to both):

```ts
await router.push({ name: 'about' })            // via the router
uni.navigateTo({ url: '/pages/about/about' })  // intercepted → handed to the router
```

## Intercepted APIs

All 5 native navigation APIs are handed to the router to run the full guard chain:

| Intercepted uni API | Handed over as |
| --- | --- |
| `uni.navigateTo` | `router.push` |
| `uni.redirectTo` | `router.replace` |
| `uni.reLaunch` | `router.relaunch` |
| `uni.switchTab` | `router.push` (the router dispatches switchTab automatically based on `meta.isTab`) |
| `uni.navigateBack` | `router.back` |

::: tip Does not affect the router's own calls
uni calls initiated internally by the router carry an internal marker; the interceptor detects it and lets them through, so no double interception occurs.
:::

## Example: Unified Login Auth in Guards After Interception

Write the guard once and it applies to both `router.*` and native direct calls:

```ts
// router/index.uts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()],
	interceptUniApi: true
})

// global beforeEach guard: abort when not logged in (native direct calls, once intercepted and handed over, pass through here as well)
router.beforeEach((to) => {
	const token = uni.getStorageSync('token') as string
	const hasToken: boolean = token.length > 0
	if (to.path !== '/pages/login/login' && !hasToken) {
		return false
	}
})
```

```ts
// business / third-party code: bypasses the router and calls the native API directly — the guard still applies
uni.navigateTo({ url: '/pages/order/order' })
// actual flow: the interceptor blocks the original call → hands over to router.push → guard chain → aborted when not logged in
```

## Platform Version Thresholds

Interception relies on `uni.addInterceptor`, supported on each platform starting from these HBuilderX versions:

| Platform | Minimum HBuilderX version |
| --- | --- |
| Web | 4.0 |
| WeChat Mini Program | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

When the runtime capability is missing, the plugin **degrades automatically**: it logs a warning and disables interception, but **does not block navigation** (native calls execute with their original logic).

## Manual Install / Uninstall

Beyond automatic installation, you can also control it with finer granularity after the Router is instantiated (deferred installation, conditional start/stop):

```ts
import { installInterceptors, removeInterceptors } from '@meng-xi/unix-router'

// Manual install (internally calls uni.addInterceptor to register the 5 interceptors)
installInterceptors(router)

// Manual uninstall (removes the interceptors and releases the router reference)
removeInterceptors()
```

::: warning Single-instance limitation
Only one router instance's interceptors are supported at a time; installing again uninstalls the old interceptors first and logs a warning.
:::

### Example: Manual Install / Uninstall (Deferred Start/Stop)

Register the plugin but leave `interceptUniApi` off (skipping automatic installation at install time), then start/stop manually at runtime as needed:

```ts
import { installInterceptors, removeInterceptors, useRouter } from '@meng-xi/unix-router'

const router = useRouter()

// install manually at any time (internally calls uni.addInterceptor one by one to register the 5 interceptors)
installInterceptors(router)

// uninstall and release the router reference when you need to turn it off temporarily (debugging, lifting navigation restrictions)
removeInterceptors()
```

The corresponding router configuration (condition 1 satisfied; condition 2 is left to runtime):

```ts
const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()]
	// interceptUniApi is not set, so no interceptors are installed automatically at plugin install time
})
```

## API Reference

### InterceptorPlugin

A `RouterPlugin` subclass — the uni native navigation API interception plugin.

| Member | Signature | Description |
| --- | --- | --- |
| `name` | constant `'interceptor'` | Plugin name |
| `constructor()` | no parameters | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | When `options.interceptUniApi === true`, calls `installInterceptors(context.router)`; on Web, registers `removeInterceptors()` on app unmount for cleanup (HMR scenarios) |

Registration (must be instantiated; on non-JS targets plugins must be implemented as classes — an object literal containing methods gets inferred as UTSJSONObject):

```ts
plugins: [new InterceptorPlugin()] // usually paired with interceptUniApi: true
```

### Exported Functions

| Function | Signature | Description |
| --- | --- | --- |
| `installInterceptors` | `(router: Router): void` | Manual install: registers one `uni.addInterceptor` for each of the 5 navigation APIs; warns and disables when `uni.addInterceptor` is unavailable; uninstalls the existing registration first with a warning when one exists (only one at a time) |
| `removeInterceptors` | `(): void` | Removes the interceptors one by one via `uni.removeInterceptor`, resets and releases the router reference |
| `markRouterCall` | `(): void` | Marks the next uni API call as initiated internally by the router (used inside the navigation module; the interceptor lets marked calls through; external code rarely needs this) |

Intercepted API list (internal constant `INTERCEPTED_APIS`): `navigateTo`, `redirectTo`, `switchTab`, `reLaunch`, `navigateBack`.

### RouterOptions.interceptUniApi

```ts
interceptUniApi?: boolean // default false
```

The switch that enables interception; if `true` is set without InterceptorPlugin registered, a warning is logged at install time and the option is ignored.

### Error Behavior

This plugin produces no navigation-failure error codes (no `PLUGIN_REQUIRED` and the like):

| Condition | Behavior |
| --- | --- |
| Plugin registered but `interceptUniApi` not enabled | The plugin skips installation entirely; no interceptors are installed |
| `interceptUniApi` enabled but the plugin not registered | A warning is logged at install time and the option is ignored |
| `uni.addInterceptor` missing at runtime | A warning is logged and interception is disabled; native calls execute with their original logic and navigation is not blocked |
| Repeated installation | The old interceptors are uninstalled first with a warning (only one router instance is effective at a time) |

### Platform Notes

- Interception relies on `uni.addInterceptor`; the minimum HBuilderX version per platform is listed in the "Platform Version Thresholds" table above (Web 4.0 / WeChat Mini Program 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61);
- The interceptor uses a counter to distinguish "router-initiated" from "external direct" calls: `markRouterCall()` marks before the router calls, the interceptor consumes one mark and lets the call through, avoiding double interception;
- When an external direct call is handed over, the original call is blocked (`invoke` returns `false`, and `url` is cleared as a second safeguard);
- Special handling for `switchTab` on Web: the original call is let through, and only its `success` callback runs `syncRoute()` to sync state (blocking it would deadlock the TabBar component's state);
- Hand-over mapping: `navigateTo → push`, `redirectTo → replace`, `reLaunch → relaunch`, `switchTab → push` (the router dispatches automatically based on `meta.isTab`), `navigateBack → back` (with `delta > 0` passed through for the back levels); query is parsed via `parseUrl` and passed through to the router.

## Next Steps

- [Error Handling](./error-handling) — how to catch navigation failures after interception hands over
- [Plugin System](./plugins) — where InterceptorPlugin sits in the plugin system
- [Platform Compatibility](./compatibility) — details of addInterceptor support on each platform
