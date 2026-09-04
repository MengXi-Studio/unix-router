# Route Navigation

unix-router provides vue-router-style programmatic navigation, mapped underneath to uni-app x's native `uni.*` APIs.

## Four Navigation Methods

| Method | Matching Native API | Description |
| --- | --- | --- |
| `router.push(location)` | `uni.navigateTo` / `uni.switchTab` | Keeps the current page and navigates to a new page |
| `router.replace(location)` | `uni.redirectTo` / `uni.switchTab` | Replaces the current page |
| `router.relaunch(location)` | `uni.reLaunch` / `uni.switchTab` | Closes all pages and opens the target page |
| `router.back(delta)` | `uni.navigateBack` | Goes back one or more pages |

> Routes whose `meta.isTab` is `true` automatically use `uni.switchTab`.

## Location Forms (RouteLocationRaw)

The navigation target supports both a **string** or an **object**:

```ts
// string: can carry a query directly
router.push('/pages/about/about?from=home')

// object: path / name + query + params
router.push({ name: 'about', query: new Map([['a', '1']]) })
router.push({ path: '/pages/about/about', params: new Map([['from', 'Home']]) })
```

## params (Object Parameters)

uni-app x's static page model does not support path parameters, so unix-router passes `params` between page URLs through **query encoding** (with the reserved `__unixr_p_` prefix). The target page can read them from `route.params`:

```ts
router.push({ path: '/pages/params/params', params: new Map([['id', '42']]) })
```

```ts
// on the target page's onLoad / useRoute
const id = route.params.get('id')
```

## Going Back

```ts
router.back() // go back one page
router.back(2) // go back two pages
```

`back` runs the full guard chain (beforeEach → beforeResolve) and can be aborted or redirected by a guard.

## Duplicate Navigation Interception

`router.push` to the same location currently in use throws a `NAVIGATION_DUPLICATED` failure (detectable with `isNavigationFailure`) to avoid meaningless navigations.

```ts
try {
	await router.push('/pages/index/index')
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) {
		// already on this page, ignore
	}
}
```

## Concurrent Queuing

When a previous navigation hasn't finished yet, later navigations are automatically **queued** and run in order after the previous one completes, avoiding broken state.