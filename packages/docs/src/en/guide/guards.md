# Navigation Guards

Guards **control, validate, and redirect** navigation. They are the core of authentication and analytics, with semantics aligned to vue-router 4.

## Guard Chain Execution Order

Guards run in the following order during a single navigation:

```
beforeEach → beforeEnter(per-route) → beforeResolve → navigate → afterEach
```

- Any guard returning "abort / redirect" ends the current chain;
- When a guard returns a redirect, the **full guard chain re-runs** toward the new target;
- `afterEach(to, from, failure)` is called after the navigation completes: `failure` is `null` on success and carries an `Error` on failure. It never blocks the flow.

## Guard Types Overview

| Type | Registration | Timing |
| --- | --- | --- |
| Global before | `router.beforeEach` | Runs first when navigation is triggered |
| Per-route | `RouteConfig.beforeEnter` | Only when entering that route |
| Global resolve | `router.beforeResolve` | After all before / per-route guards, right before the actual navigation |
| Global after | `router.afterEach` | After navigation completes (non-blocking) |
| In-component | `onBeforeRouteLeave` etc. | When leaving / entering / updating the current page |

## Full Table of Guard Return Values

A guard's return value decides where the navigation goes. Async is supported: an `async` guard's returned `Promise` is resolved before the decision (subject to `guardTimeout`).

| Return value | Behavior |
| --- | --- |
| `null` / `true` | Allow, continue with the remaining guards |
| `false` | Abort the navigation (error code `ABORTED`) |
| `Error` | Abort the navigation with that error as the failure reason (error code `CANCELLED`) |
| `string` | Redirect to that path |
| Location object (`{ path }` / `{ name }` / `{ name, query }` etc.) | Redirect to that location |
| `{ location, mode? }` | `NavigationRedirect`: redirect with an explicit navigation mode |

The `NavigationRedirect` structure:

```ts
{
	location: RouteLocationRaw             // redirect target (a string path or a location object)
	mode?: 'push' | 'replace' | 'relaunch' // navigation mode; omitted means keep the original mode
}
```

::: tip How the two object kinds are distinguished
If the returned object carries a `location` field → treated as `NavigationRedirect` (may specify `mode`); otherwise treated as a plain location object (keeps the original navigation mode).
:::

A redirect **re-runs the full guard chain**. The depth limit is `10`; exceeding it (e.g. guards redirecting each other in a loop) cancels with `CANCELLED` to prevent infinite loops.

## Registering Global Guards

All three global guards return a **cancel function** — call it to unregister:

```ts
const offBefore = router.beforeEach((to, from) => {
	console.log('before', from.fullPath, '->', to.fullPath)
	return true
})

const offResolve = router.beforeResolve((to, from) => {
	return true
})

const offAfter = router.afterEach((to, from, failure) => {
	// failure: Error | null
	console.log('done', to.fullPath, failure?.message ?? '')
})

// Unregister
offBefore()
offResolve()
offAfter()
```

::: tip beforeEach vs beforeResolve
`beforeEach` runs at the front of the guard chain (best for auth and analytics); `beforeResolve` runs after per-route guards and right before the navigation actually executes (best for logic depending on a "finalized" target; in-component guards are also implemented as a filter on top of it).
:::

## Guard Timeout guardTimeout

The `guardTimeout` option of `createRouter` (default `10000`ms) bounds guard execution time:

- On timeout it logs a warning and **aborts the navigation** (`CANCELLED`);
- Set it to `0` to disable the timeout check;
- Async guards (returning `Promise`) are subject to the same timeout.

```ts
const router = createRouter({
	routes,
	guardTimeout: 5000 // 5s
})
```

## Per-route beforeEnter

Defined on `RouteConfig`, effective **only for that route**. Accepts a single function or an array:

```ts
{
	path: 'pages/admin/admin',
	name: 'admin',
	meta: { title: 'Admin' },
	beforeEnter: (to, from) => {
		return isAdmin() ? true : { name: 'login' }
	}
}
```

## In-component Guards

Registered in a page's `setup`, implemented as a filter on the global `beforeResolve`, and they cooperate with the global resolve guard:

| API | Triggered when |
| --- | --- |
| `onBeforeRouteLeave` | Leaving this page (navigating elsewhere / going back) |
| `onBeforeRouteEnter` | Navigating into this page's path |
| `onBeforeRouteUpdate` | Same path with changed parameters ("update") |

```ts
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
	if (hasUnsavedChanges) {
		return false // block leaving
	}
	return true
})
```

::: warning Limited applicability
uni-app x creates a new page instance for every navigation (no keep-alive reuse): `onBeforeRouteLeave` is the **most reliable**; `onBeforeRouteEnter` has limited effect because the page is already being created when first entered; `onBeforeRouteUpdate` rarely triggers under the static page model.
:::

## In Practice: Full Login Auth Flow

**Goal**: an unauthenticated user visits a `requireAuth` page → intercepted → login page → redirected back to the original page after login.

**1) Route config** (`router/routes.ts`):

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/login/login', name: 'login', meta: { title: 'Login' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: 'Profile', requireAuth: true } }
]
```

**2) Global before guard** (`router/index.ts`) — redirect via `NavigationRedirect` carrying a `redirect` query:

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({ routes, strict: true })

let loggedIn: boolean = false
export function isLoggedIn(): boolean {
	return loggedIn
}
export function setLoggedIn(value: boolean): void {
	loggedIn = value
}

router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace' // replace: the login page doesn't stay in the back stack
		}
	}
	return true
})
```

**3) Redirect back after login** (`pages/login/login.uvue`):

```vue
<script setup lang="uts">
import { useRouter, useRoute } from '@meng-xi/unix-router'
import { setLoggedIn } from '../../router'

const router = useRouter()
const route = useRoute()

const onLogin = () => {
	setLoggedIn(true)
	const redirect = route.query.get('redirect')
	router.replace(redirect !== null ? redirect : '/pages/index/index')
}
</script>

<template>
	<view class="page">
		<text class="title">Login</text>
		<button @click="onLogin">Login and go back</button>
	</view>
</template>
```

::: danger Don't call router.push inside guards
Inside a guard, `return` a redirect location instead of calling `router.push` — the latter starts a new navigation that gets queued, producing an unpredictable navigation sequence.
:::

## Registration Cancellation and Cleanup

All guard registrations go into a global guard queue. Long-running apps should unregister to avoid piling up duplicate registrations:

```ts
const off = router.beforeEach(reportNavigation)

// Cancel when no longer needed (logout, page unmount, test teardown, etc.)
off()
```

## back Also Runs Guards

`router.back(delta)` runs **only `beforeEach → beforeResolve`** before going back (per-route `beforeEnter` is not executed); `afterEach` fires as usual after a successful back. To intercept on "back": check `to.path` (the back target) in a global guard, or intercept in `onBeforeRouteLeave` of the page being left.

- `delta` defaults to `1`; a non-positive integer → `ABORTED`; insufficient page stack → `CANCELLED`.

## guardRoute: Running Guards on Cold Start

When an H5 URL / App scene value / deeplink lands directly on a page, the framework has already loaded the page and **the guards never ran**. Re-run them in `onLaunch` of `App.uvue`:

```ts
import { router } from './router'

export function App() {
	onLaunch((options: any) => {
		router.isReady().then(() => {
			const launchPath = options?.path ? `/${options.path}` : undefined
			router.guardRoute(launchPath, {
				onAbort: (failure) => router.relaunch('/pages/index/index')
			})
		})
	})
}
```

- Allow: returns the target route
- Redirect: automatically navigates to the guard-returned target
- Abort: triggers `onAbort`; you can jump to a safe page

## Common Pitfalls

1. **Calling `router.push` inside a guard**: produces queued navigations with unpredictable behavior. `return` a redirect location instead.
2. **Missing the allow branch**: explicitly `return true` / `return null` in every branch.
3. **Async guards**: just use an `async` function; the `Promise` is resolved before the decision (subject to `guardTimeout`).
4. **Misusing truthiness checks**: read optional fields with `to.meta.requireAuth === true`.
5. **Business code calling `uni.navigateTo` directly, bypassing guards**: not intercepted by default; enable [uni API interception](./interceptor) to route native calls through the guard chain too.

## Next Steps

- [Inter-page Communication](./events) — targeted communication between pages after navigation completes
- [Error Handling](./error-handling) — the error system for aborted / cancelled / redirect failures
- [NavigationGuard Type](../api/type-navigation-guard) — the complete guide to guard return value types
