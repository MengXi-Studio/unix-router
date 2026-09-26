# FAQ

A collection of frequently asked questions and troubleshooting approaches when using unix-router.

## params Not Readable

**Check 1: Is ParamsPlugin registered correctly?**

Without `ParamsPlugin` registered, navigations carrying `params` reject immediately with `PLUGIN_REQUIRED`. If the navigation succeeds but `route.params` is empty on the target page, first verify the registration style (the plugin must be instantiated):

```ts
// ❌ Old style — passing the class directly (deprecated)
createRouter({ routes, plugins: [ParamsPlugin] })

// ✅ Register an instance
createRouter({ routes, plugins: [new ParamsPlugin()] })
```

**Check 2: Are you reading the wrong field?**

params and query are two independent fields. params travel through the internal `__params__` key channel and **do not appear in the user-visible URL query**; the target page can only read the rebuilt `route.params` after state sync completes.

**Check 3: Are the values serializable?**

params values are a `Map<string, string>` and are serialized to JSON internally to cross pages. Convert complex objects to strings first (e.g. with `JSON.stringify`) and deserialize them on the target page.

## Stale query in onShow

**Symptom**: inside a page's `onShow`, the `query` read via `useRoute()` is still the previous page's / an old value.

**Cause**: `onShow` fires **earlier than** the router finishes `syncRoute` (syncing `currentRoute` from the page stack), so the route state has not been updated to this page yet.

**Solution**: read the query uni passes to this page directly in `onLoad(options)` instead of relying on `useRoute()`:

```uts
import { onLoad } from '@dcloudio/uni-app'

onLoad((options: UTSJSONObject) => {
	const id = options['id'] as string | null
	// options is this page's URL query, in sync with this page's load
})
```

## switchTab Drops query

Navigations to pages with `meta.isTab === true` automatically switch to `uni.switchTab`, and the **switchTab API discards the entire query string** — neither `query` nor `params` reaches the target tab page (plugin params and `events` travel via internal query keys, which are dropped as well).

To pass data to a tab page, use instead:

- **Global state / storage** (recommended — see [Recipes — TabBar Apps](./recipes#tabbar-apps))
- **eventBus** (EventsPlugin's app-level bus, which does not depend on the navigation URL)

## Guards Not Taking Effect

**Check 1: Are you navigating through the router?**

```ts
// ❌ Calling the uni API directly — guards don't run
uni.navigateTo({ url: '/pages/about/about' })

// ✅ Call through the router
await router.push({ name: 'about' })
```

unix-router does **not intercept** native navigation APIs by default — calling `uni.navigateTo` and friends directly **bypasses the guards**. Either navigate consistently through `router.*` or `<RouterLink>`, or enable InterceptorPlugin + `interceptUniApi: true` to intercept external calls (see [uni API Interception](./interceptor)).

**Check 2: Does the guard return a value correctly?**

```ts
// ❌ A branch is missing a return
router.beforeEach((to, from) => {
	if (needAuth) {
		return { name: 'login' }
	}
	// the "allow" branch is missing
})

// ✅ Explicitly allow
router.beforeEach((to, from) => {
	if (needAuth) {
		return { name: 'login' }
	}
	return true
})
```

**Check 3: Are async guards awaited correctly?**

```ts
// ✅ Use async/await
router.beforeEach(async (to, from) => {
	const user = await fetchUser()
	if (user == null) {
		return { name: 'login' }
	}
	return true
})
```

## Guard Timeout Abort

**Symptom**: a guard timeout warning appears in the console and the navigation is aborted.

**Cause**: an async guard did not return within `guardTimeout` (default **10000ms**, set to 0 to disable), so the router warns after the timeout and aborts the navigation.

**Troubleshooting**:

1. Does a Promise inside the guard never settle (e.g. a request without a timeout / catch)?
2. Are you awaiting a task that hangs forever?
3. Does every branch of the guard return a value? (A missing return keeps the router waiting indefinitely.)

If the logic is correct but genuinely needs more time, increase `guardTimeout`:

```ts
const router = createRouter({
	routes,
	guardTimeout: 30000 // 30s; 0 disables the timeout protection
})
```

## Duplicate Navigation Error (DUPLICATED)

Pushing to the same location (path + query + params + hash all identical to the current route) rejects with `DUPLICATED` (detected by `push` only; unlike vue-router's `resolve(false)`, this is a real reject).

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

try {
	await router.push({ name: 'about' })
} catch (err) {
	// ignore duplicate navigations, rethrow everything else
	if (!isNavigationFailure(err, RouterErrorCode.DUPLICATED)) {
		throw err
	}
}
```

## Deadlock When Navigating Inside a Guard

Do not call `router.push` inside a guard — return a redirect instead.

```ts
// ✅ Redirect, no deadlock
router.beforeEach((to, from) => {
	if (needRedirect) {
		return { name: 'other' }
	}
	return true
})
```

Redirects have a depth limit (10); exceeding it returns `CANCELLED`, so there is no infinite loop.

## Back Navigation Cannot Be Intercepted

Platforms differ in their ability to intercept back navigation:

- **App**: the physical back key / navigation bar back goes through the back guard chain, and `onBeforeRouteLeave` works
- **H5**: the browser back goes through the back guard chain, and `onBeforeRouteLeave` works
- **Mini Program**: the top back arrow / swipe gesture is controlled by the host and **cannot be intercepted synchronously** — handle it afterwards with `onRouteChange`

See [Platform Compatibility](./compatibility).

## Cold-Start Deep Links on H5 Skip Guards

**Symptom**: the user opens a deep page directly via URL (e.g. entering a detail page from a shared link), and the global guards never run.

**Cause**: the guard chain is attached to navigation calls. A cold-start deep link does not issue a `router.push`, so the guards naturally don't run.

**Solution**: in `App.uvue`'s `onLaunch`, wait for the router to be ready, then use `guardRoute` to **re-run the global `beforeEach` only** on the real entry page (it does not run `beforeEnter` / `beforeResolve` / `afterEach` again; no actual navigation happens — a guard abort triggers `onAbort` and rejects, while a redirect is executed by default as a real `relaunch` jump):

```uts
import { onLaunch } from '@dcloudio/uni-app'

onLaunch((options) => {
	router.isReady().then(() => {
		let launchPath: string | null = null
		if (options.path != null && options.path.length > 0) {
			launchPath = '/' + options.path
		}
		router.guardRoute(launchPath, {
			onAbort: (failure) => {
				// blocked by a guard on cold start — redirect to home
				router.relaunch({ name: 'home' })
			}
		})
	})
})
```

Reading `router.currentRoute` directly at startup may return the initial value; for page-level data, still rely on the page's own `onLoad` / `onShow`.

## query / params Lost

Both `query` and `params` are `Map<string, string>` and are passed as strings.

```ts
// ❌ Dot access
route.query.id

// ✅ Map API
route.query.get('id')
route.query.has('id')
```

For numeric / boolean semantics, use the built-in helpers — `queryInt(query, 'id')` / `queryNumber(query, 'price')` / `queryBool(query, 'enabled')` (all with default-value fallbacks).

Complex object data must be serialized first; for structured data across pages, prefer params (ParamsPlugin) or EventsPlugin / eventBus.

## switchTab Pages Receive No Params

See "[switchTab drops query](#switchtab-drops-query)" above — `switchTab` discards the entire query, so `params` cannot reach the tab page either; use global state / storage instead.

## Page Stack Overflow

Mini Program page stacks have a limit; switch to `relaunch` when approaching it. See [Recipes — Page Stack Depth Management](./recipes#page-stack-depth-management).

## H5 Refresh 404

uni-app x's H5 side uses hash mode. Visit addresses like `https://example.com/#/pages/index/index` and avoid opening deep URLs without a hash directly.

## White Screen on Navigation

1. Is the path correct (it should be the full page path `pages/xxx/xxx`, without a leading `/`)?
2. Is the page registered in `pages.json`?
3. Does the target page's `onLoad` / `setup` throw any errors?
4. Does the route config's `path` exactly match `pages.json`?
5. Was the navigation failure rejected? (`NAVIGATION_API_ERROR` usually means the stack-top verification failed or the target page does not exist.)

## Route Lazy Loading

uni-app x decides page loading via `pages.json`, so vue-router's `() => import()` lazy loading is **not supported**. All pages are bundled; use subpackages (`subPackages`) to control grouping.

## Still Stuck?

1. Check the [API docs](../api/create-router) to confirm the usage
2. Check [Navigation Flow](./navigation-flow) to understand the internal mechanism
3. Check [Platform Compatibility](./compatibility) to see whether it is a platform limitation
4. File an issue on [GitHub Issues](https://github.com/MengXi-Studio/unix-router/issues) (include reproduction steps, platform, uni-app x and unix-router versions, and the full error)

## Next Steps

- [Differences from vue-router](./differences) — the design trade-offs behind these behaviors
- [Error Handling](./error-handling) — RouterError / NavigationFailure and error codes in detail
- [Recipes](./recipes) — complete solutions for common scenarios
