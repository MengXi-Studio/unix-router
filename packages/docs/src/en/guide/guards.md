# Route Guards

Route guards are used to control, validate, and redirect navigation. Their semantics align with vue-router 4.

## Global Guards

| Guard | Execution Timing |
| --- | --- |
| `router.beforeEach` | Called before a navigation is triggered; applies to every navigation |
| `router.beforeResolve` | After all before guards and route-local guards have run |
| `router.afterEach` | After the navigation completes (does not block it) |

### Return Value Semantics

| Returns | Behavior |
| --- | --- |
| `true` / `undefined` / `null` | Allow the navigation |
| `false` | Abort the navigation (`NAVIGATION_ABORTED`) |
| `Error` | Cancel the navigation (`NAVIGATION_CANCELLED`) |
| string / location object | Redirect to the target |
| `Promise` | Supports async guards |

```ts
router.beforeEach((to, from) => {
	// visiting a guarded page without login → redirect to login
	if (to.meta.requireAuth && !isLoggedIn()) {
		return { name: 'login', query: new Map([['redirect', to.fullPath]]) }
	}
	return true
})

router.afterEach((to, from) => {
	console.log(`navigation: ${from.fullPath} -> ${to.fullPath}`)
})
```

Unregistering a guard: `beforeEach` and friends return an **unregister function**.

```ts
const removeGuard = router.beforeEach(guard)
removeGuard() // remove
```

## Route-Local Guard beforeEnter

Defined in `RouteConfig.beforeEnter`, it only applies to that route:

```ts
{ path: 'pages/guards/guards', name: 'guards', beforeEnter: (to, from) => { ... } }
```

## In-Component Guards

Register them inside a page's `setup` via the Composition API:

```ts
import { onBeforeRouteLeave, onBeforeRouteUpdate, onBeforeRouteEnter } from '@meng-xi/unix-router'

// triggers when leaving the current page; returning false prevents leaving
onBeforeRouteLeave((to, from) => true)

onBeforeRouteUpdate((to, from) => true)
onBeforeRouteEnter((to, from) => true)
```

> Note: uni-app x creates a **new instance** for every navigation (no keep-alive reuse), so
> `onBeforeRouteUpdate` rarely triggers and `onBeforeRouteEnter` has limited effect; `onBeforeRouteLeave` is the most commonly used.

## Guard Redirects and Depth Protection

When a guard returns a redirect location, a new navigation runs recursively. To prevent infinite loops, the navigation is cancelled (`NAVIGATION_CANCELLED`) when the redirect depth exceeds `MAX_REDIRECT_DEPTH`.

## guardRoute (Cold Start)

When a user **enters a page directly** via an H5 URL / an App scene value / a deeplink, the page is loaded directly by the framework without passing through guards. Calling `router.guardRoute()` re-runs the guard chain for the current/target route and decides whether to redirect based on the result:

```uts
// App.uvue onLaunch
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: failure => router.relaunch('/pages/index/index')
	})
})
```

## Navigation Flow

For the full order, see [Navigation Flow](./navigation-flow).