# Error Handling

unix-router provides a complete vue-router-style error system.

## Error Types

| Type | Description |
| --- | --- |
| `RouterError` | Base class for router errors (contains `code` / `to` / `from`) |
| `NavigationFailure` | A navigation failure (inherits `RouterError`; thrown when aborted, cancelled, or duplicated) |
| `RouterErrorCode` | Error code enum |

## Error Codes RouterErrorCode

| Enum | Value | Description |
| --- | --- | --- |
| `ABORTED` | 4 | A guard returned `false`, aborting the navigation |
| `CANCELLED` | 8 | A guard threw, or the redirect exceeded the depth limit |
| `DUPLICATED` | 16 | Duplicate navigation to the current location |
| `ROUTE_NOT_FOUND` | 32 | No named route matched in strict mode |
| `NAVIGATION_API_ERROR` | 64 | The `uni.*` native navigation API failed |
| `SETUP_ERROR` | 128 | Error in the router installation environment |

## Catching Navigation Failures

Programmatic navigation can reject; narrow the check with `isNavigationFailure`:

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

try {
	await router.push('/pages/index/index')
} catch (e) {
	// e is an Error; isNavigationFailure narrows it to a NavigationFailure
	const failure = e as NavigationFailure
	if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
		// duplicate navigation, can be ignored
	} else if (isNavigationFailure(failure, RouterErrorCode.ABORTED)) {
		// aborted by a guard
	}
}
```

## Global Error Handling onError

Errors thrown by navigation synchronously trigger all `onError` handlers (`afterEach` also receives the failure):

```ts
router.onError((error, to, from) => {
	console.warn(`navigation failed(${to.fullPath}): ${error.message}`)
})
```

## Errors Thrown Inside Guards

An `Error` thrown by a guard cancels the navigation (`CANCELLED`) and triggers `onError` / `afterEach(failure)`.

## Summary of Trigger Timing

- Guard **aborts/cancels**: `afterEach(to, from, failure)` and each `onError` callback are invoked.
- Native API **call fails**: `currentRoute` rolls back to the source route, and error handling is triggered.
- **Duplicate navigation**: only throws `DUPLICATED`, without extra logic outside `onError` (can be ignored as needed).

## Practical Handling Strategy

Navigation failures fall into roughly three categories with different goals:

| Scenario | Code | How to handle |
| --- | --- | --- |
| Duplicate navigation | `DUPLICATED` | Ignore (already on the target page) |
| Guard aborted | `ABORTED` / `CANCELLED` | Stay silent or prompt "action cancelled"; the guard already handled the redirect, don't navigate again |
| Real error | `ROUTE_NOT_FOUND` / `NAVIGATION_API_ERROR` etc. | Report + prompt the user |

**Recommended** — wrap `push` in try/catch, branch by code, and let the rest reach `onError`:

```ts
async function safePush(location: RouteLocationRaw) {
	try {
		await router.push(location)
		return true
	} catch (e) {
		const failure = e as NavigationFailure
		if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
			return false          // already on the target page, not a failure
		}
		if (isNavigationFailure(failure, RouterErrorCode.ABORTED)) {
			return false          // guard proactively aborted; expected
		}
		console.error('navigation failed', (e as Error).message)
		return false
	}
}
```

**Universal fallback**: register all unexpected failures centrally in `onError` (analytics / logging) to avoid repeating it at every call site.

> Tip: in `try/catch`, `catch (e)` is `unknown`/`Error` in UTS; read it via `(e as Error).message` or narrow with `(e as NavigationFailure)`.