# FAQ

A summary of frequently asked questions and troubleshooting approaches when using unix-router.

## Guards Not Taking Effect

**Check 1: Are you calling through the router?**

```ts
// ❌ Calling the uni API directly — guards don't run
uni.navigateTo({ url: '/pages/about/about' })

// ✅ Call through the router
await router.push({ name: 'about' })
```

uni-app x does not provide `uni.addInterceptor`, so unix-router does **not intercept native navigation APIs**. Always use the router API or `<RouterLink>`.

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
	if (!user) {
		return { name: 'login' }
	}
	return true
})
```

## query / params Lost

Both `query` and `params` are `Map<string, string>` and are passed as strings in the URL.

```ts
// ❌ Dot notation
route.query.id

// ✅ Map API
route.query.get('id')
route.query.has('id')
```

Complex object data needs to be serialized first, or carried by global state (such as Pinia).

## Duplicate Navigation Error

`push` to the same location (path + name + query all equal) throws `DUPLICATED`.

```ts
try {
	await router.push({ name: 'about' })
} catch (err) {
	if (err.code !== 16) throw err // RouterErrorCode.DUPLICATED
}
```

## Cannot Intercept Back Navigation

Different platforms have different abilities to intercept back navigation:

- **App side**: the physical back key / navigation bar back goes through the back guard chain, and `onBeforeRouteLeave` works
- **H5 side**: the browser back goes through the back guard chain, and `onBeforeRouteLeave` works
- **Mini Program side**: the top back arrow / swipe is controlled by the host and **cannot be intercepted synchronously**; handle it afterwards with `onRouteChange`

See [Platform Compatibility](./compatibility#cannot-intercept-back-navigation).

## H5 Refresh 404

uni-app x's H5 side uses hash mode. Visit addresses like `https://example.com/#/pages/index/index`; avoid accessing deep URLs without a hash directly.

## Page Stack Overflow

Mini Program page stacks have a limit; switch to `relaunch` when approaching it. See [Recipes - Page Stack Depth Management](./recipes#page-stack-depth-management).

## Deadline When Triggering Navigation Inside a Guard

Don't call `router.push` inside a guard; use `return` to redirect instead.

```ts
// ✅ redirect, no deadlock
router.beforeEach((to, from) => {
	if (needRedirect) {
		return { name: 'other' }
	}
})
```

## Cold-Start Guard Validation

In `App.vue`'s `onLaunch`, re-run the guards on the real entry page, optionally passing `options.path`:

```ts
onLaunch((options) => {
	router.isReady().then(() => {
		const launchPath = options?.path ? `/${options.path}` : undefined
		router.guardRoute(launchPath, {
			onAbort: (failure) => {
				router.relaunch({ name: 'home' })
			}
		})
	})
})
```

Reading `router.currentRoute` directly at startup may return the initial value; page-level data still comes from the page's own `onLoad` / `onShow`.

## White Screen on Navigation

1. Is the path correct (it should be the full page path `pages/xxx/xxx`)
2. Is the page registered in `pages.json`
3. Does the target page's `onLoad` / `setup` have any errors
4. Does the route config's `path` exactly match `pages.json`

## Route Lazy Loading

uni-app x decides page loading via `pages.json`, so vue-router's `() => import()` lazy loading is **not supported**. All pages are bundled; you can use subpackages (`subPackages`) to control this.

## Still Can't Solve It?

1. Check the [API Documentation](../api/create-router) to confirm usage
2. Check the [Navigation Flow](./navigation-flow) to understand the internal mechanism
3. Check the [Platform Compatibility](./compatibility) to confirm whether it's a platform limitation
4. File an issue on [GitHub Issues](https://github.com/MengXi-Studio/unix-router/issues) (include reproduction steps, platform, uni-app x and unix-router versions, and the full error)