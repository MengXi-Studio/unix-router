# Navigation

unix-router offers vue-router-style **programmatic navigation** mapped to uni-app x's native `uni.*` APIs. This page first clarifies when to use each mode, then dives into `params` / `query` passing and common traps.

## The Four Navigation Modes

| Method | Native API | Page stack behavior | Use case |
| --- | --- | --- | --- |
| `router.push(location)` | `uni.navigateTo` / `uni.switchTab` | **Keeps** the current page, pushes a new one | Most forward navigation |
| `router.replace(location)` | `uni.redirectTo` / `uni.switchTab` | **Replaces** the current page | Login and other "no going back" flows |
| `router.relaunch(location)` | `uni.reLaunch` / `uni.switchTab` | **Closes all** pages, opens the target | Back to root / exit to home |
| `router.back(delta)` | `uni.navigateBack` | Back one or more pages | Going back |

> If the target route is a TabBar page (`meta.isTab`), `push / replace / relaunch` automatically use `uni.switchTab` (which does not support query/params).

**Choosing:**

- Want "back" to return here → `push`
- Don't want the user to return here (e.g. after login) → `replace`
- Clear the stack to the root / land-and-enter the main UI → `relaunch`
- Going back → `back(delta)`

## Location Form (RouteLocationRaw)

The target supports a **string** or an **object**.

```ts
// String: can inline query
router.push('/pages/about/about?from=home')

// Object: navigate by name (recommended, decouples path)
router.push({ name: 'about', query: new Map([['a', '1']]) })

// Object: path navigation + params
router.push({ path: '/pages/detail/detail', params: new Map([['from', 'Home']]) })

// When both name and path are given: name wins
router.push({ name: 'about', path: '/pages/index/index' }) // matches name='about'
```

## Passing Parameters: query vs params

### query (query string, visible in URL)

Carried as `Map<string,string>` and serialized into the URL. Suitable for small, simple, shareable data:

```ts
router.push({ name: 'about', query: new Map([['id', '42'], ['utm', 'banner']]) })
// URL ≈ /pages/about/about?id=42&utm=banner

// Read on the target page (Map API, not dot access)
const id = useRoute().query.get('id') ?? ''
```

### params (object params, URL-encoded without exposing plaintext keys)

uni-app x does not support path parameters, so unix-router passes `params` across pages via **query encoding** (reserved `__unixr_p_` prefix). **Keys are encoded and do not expose plaintext names**, and are kept separate from ordinary query:

```ts
router.push({ path: '/pages/detail/detail', params: new Map([['from', 'Home'], ['id', '42']]) })
// Encoded in the URL as ...?__unixr_p_...=...

// The target reads back clean keys
const from = route.params.get('from')  // "Home"
const id = route.params.get('id')      // "42"
// params' internal keys do not appear in route.query
```

### query or params?

| Dimension | query | params |
| --- | --- | --- |
| URL visibility | plaintext key + value | keys encoded, values still visible |
| Best for | small, simple, sharable, analytics | named params where you don't want key names exposed |
| Read via | `route.query.get` | `route.params.get` |
| Type | both `Map<string,string>` | both `Map<string,string>` |

**Note**: both are passed as strings in the URL. For complex objects, `JSON.stringify` first or use global state (e.g. a `reactive` module). For complex/sensitive/large cross-page data, prefer the latter.

## Going Back

```ts
router.back()  // back one page
router.back(2) // back two pages
```

`back` runs the **full guard chain** (`beforeEach` → `beforeResolve`), so it can be aborted or redirected by guards — one way to implement "back interception".

## Duplicate Navigation Interception

`push` to a location identical to the **current one** (same path + query) throws a `DUPLICATED` failure. Use `isNavigationFailure` to detect and ignore it:

```ts
try {
	await router.push('/pages/about/about')
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) {
		// already on that page, ignore
	} else {
		throw e
	}
}
```

> To "refresh the current page", use `router.replace` instead (or navigate with different query/params).

## Concurrent Queueing

If a previous navigation has not finished, later navigations are **queued** automatically and run in order, avoiding state corruption. In most cases no manual debouncing is needed.

## TabBar Switching

TabBar pages navigate via `switchTab` and do not add a page-stack item. To keep the active TabBar state aligned with the current page, rely on `syncRoute()` (see [Composables](./composables)) aligning automatically on `onShow`.

## Common Pitfalls

1. **Using dot access `route.query.id`** → should be `route.query.get('id')` (query/params are Maps).
2. **Passing query/params to a TabBar page fails**: `switchTab` doesn't support it; use global state instead.
3. **`push` to the current page does nothing**: intercepted as `DUPLICATED`; use `replace` or carry different params.
4. **Complex objects lost in params**: params only carry strings; serialize first or use global state.

## Related

- [Parameter passing deep-dive + practice](./recipes)
- [Navigation API](../api/router-instance)