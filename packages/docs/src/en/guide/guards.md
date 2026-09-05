# Route Guards

Guards control, validate, and redirect navigation, and are the core of auth and analytics. Semantics align with vue-router 4. This page walks through it using a **login-auth** example.

## Guard Order in One Navigation

```
beforeEach → beforeEnter(route-local) → beforeResolve → navigation → afterEach
```

If any guard returns `false` / `Error` / a redirect location, the flow stops or is redirected. Any single guard can block the whole chain.

## Three Global + One Route-Local + One In-Component

| Type | Register | When it runs |
| --- | --- | --- |
| Global before | `router.beforeEach` | before the navigation, applies to all |
| Global resolve | `router.beforeResolve` | after all before/enter guards, before navigation |
| Global after | `router.afterEach` | after navigation completes (non-blocking) |
| Route-local | `RouteConfig.beforeEnter` | only when entering this route |
| In-component | `onBeforeRouteLeave` etc. | when leaving/entering the component |

## Return Value Semantics

| Return | Behavior |
| --- | --- |
| `true` / `null` | Continue (prefer an explicit `return null` or `true`) |
| `false` | Abort navigation (`ABORTED`) |
| `Error` | Cancel navigation (`CANCELLED`) |
| string / location object | Redirect to the target |
| `Promise` | Async guards supported (`async`) |

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login' }   // redirect
	}
	return true                    // continue
})

router.afterEach((to, from, failure) => {
	console.log(`Nav: ${from.fullPath} -> ${to.fullPath}`, failure?.message ?? '')
})
```

Remove a guard: the registration function returns an **off function**:

```ts
const off = router.beforeEach(g)
off() // remove
```

## Practice: a Complete Login Auth Flow

**Goal**: unauthenticated access to a `requireAuth` page → intercepted → login page → redirect back after login.

**1) Route config** (`router.config.ts`):

```ts
export const routes: RouteConfig[] = [
	{ path: 'pages/login/login', name: 'login', meta: { title: 'Login' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: 'Profile', requireAuth: true } }
]
```

**2) Global before guard** (`router.ts`):

```ts
export const router = createRouter({ routes, strict: true })

router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		// record the source fullPath so it can redirect back after login
		return {
			name: 'login',
			query: new Map([['redirect', to.fullPath]])
		}
	}
	return true
})
```

**3) Login page** (`pages/login/login.uvue`), redirect back on success:

```ts
import { useRouter, useRoute } from '@meng-xi/unix-router'
const router = useRouter()
const route = useRoute()

function loginOk() {
	setLoggedIn(true)
	const target = route.query.get('redirect') ?? '/pages/index/index'
	router.replace(target) // replace: the login page is not kept in the stack
}
```

> Key point: use `return { name: 'login' }` inside the guard, not `router.push` — see "Pitfalls".

## Route-Local beforeEnter

Defined in `RouteConfig` and applies **only to this route**:

```ts
{
	path: 'pages/admin/admin',
	name: 'admin',
	meta: { requireAdmin: true },
	beforeEnter: (to, from) => (isAdmin() ? true : { name: 'login' })
}
```

## In-Component Guard (onBeforeRouteLeave)

Registered in page `setup`; **returning `false` truly blocks leaving** (e.g. unsaved-changes confirmation):

```ts
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
	if (hasUnsavedChanges) {
		return false // block leaving
	}
	return true
})
```

> uni-app x creates a new page instance on every navigation (no keep-alive), so `onBeforeRouteUpdate` rarely fires and `onBeforeRouteEnter` has limited effect; **`onBeforeRouteLeave` is the most useful**.

## guardRoute: Cold-Start Guard Re-run

When a page is entered directly (H5 URL / scene value / deeplink), the framework has already loaded it and guards never ran. Re-run in `App.uvue`'s `onLaunch`:

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

- Continue: returns the target route
- Redirect: automatically navigates to the guard's target
- Abort: triggers `onAbort`, where you can jump to a safe page

## Guard Timeout and Redirect Depth

- `guardTimeout` (default `10000ms`): warns and aborts on timeout; `0` disables it.
- **Redirect depth limit**: a guard returning a redirect recurses navigation; beyond `MAX_REDIRECT_DEPTH` it cancels (`CANCELLED`) to prevent infinite loops.

## back Also Runs Guards

`router.back(delta)` runs `beforeEach → beforeResolve`, so to intercept "going back", check `to.path` (the back target) in those guards, or intercept in `onBeforeRouteLeave`.

## Common Pitfalls

1. **Deadlock by calling `router.push` in a guard**: `return` a redirect location instead.
2. **Missing a continue branch**: a guard not returning `true`/`null` in every branch leaves the flow undefined. Return explicitly in each branch.
3. **Async guard not awaited**: use `async` guards; `Promise` is resolved before deciding.
4. **Misusing truthiness**: reading optional `to.meta.requireAuth` should use `=== true`.
5. **Bypassing guards with `uni.navigateTo`**: unix-router does **not** intercept native navigation APIs; always use `router.*`.

## Related

- Execution order details: see [Navigation Flow](./navigation-flow)
- Platform differences on back interception: see [Platform Compatibility](./compatibility#back-interception)