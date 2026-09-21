# Error Handling

unix-router provides a complete vue-router-style error system: navigation failures are uniformly expressed as `Promise` rejections, precisely classified by error codes.

## Error Object Hierarchy

```
Error
└── RouterError          // router error base class
    └── NavigationFailure // navigation failure (aborted / cancelled / duplicated, etc.)
```

Besides `message` / `name`, `RouterError` / `NavigationFailure` carry three fields:

| Field | Type | Description |
| --- | --- | --- |
| `code` | `RouterErrorCode` | The error code; see the full table below |
| `to` | `RouteLocation` | The target route that triggered the error |
| `from` | `RouteLocation` | The source route that triggered the error |

There is also `UniNavigationApiError` (an interface): the error payload (`errMsg` / `context`) of the `fail` callback of the `uni.*` native navigation APIs, used to locate the native failure cause.

## Precise Checks with isNavigationFailure

`isNavigationFailure(error, codes?)` checks whether an error is a navigation failure (of the specified types):

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

// With an error code: check whether it is that kind of navigation failure
isNavigationFailure(err, RouterErrorCode.DUPLICATED) // boolean
// Without an error code: check whether it is a navigation failure at all
isNavigationFailure(err)
```

## Full Error Code Table

| Error code | Value | Trigger scenario |
| --- | --- | --- |
| `ABORTED` | `4` | A guard returned `false`, aborting the navigation (including a non-positive-integer `delta` for `back()`) |
| `CANCELLED` | `8` | A guard threw an `Error`, guard timeout (default 10s), redirect exceeded the depth limit (10), or `back()` with an insufficient page stack |
| `DUPLICATED` | `16` | Repeatedly `push` to the current address (`path`+`query`+`params`+`hash` all identical to current; checked by `push` only) |
| `ROUTE_NOT_FOUND` | `32` | No route matched (a `name` not registered in strict mode) or an invalid location |
| `NAVIGATION_API_ERROR` | `64` | A `uni.*` navigation API call failed, or the page-stack-top confirmation failed after navigation completed (500ms polling) |
| `SETUP_ERROR` | `128` | Router installation environment error |
| `PLUGIN_REQUIRED` | `256` | Using plugin capabilities such as `params` / `events` without registering the corresponding plugin (`ParamsPlugin` / `EventsPlugin`) |

## Promise Rejection Handling Patterns

Navigation failures **never throw synchronously**; they are all delivered via `Promise` rejection (aligned with vue-router). Two handling patterns:

```ts
import { isNavigationFailure, RouterErrorCode, NavigationFailure } from '@meng-xi/unix-router'

// Pattern 1: try/catch + await
async function goDetail() {
	try {
		await router.push({ name: 'detail' })
	} catch (e) {
		const failure = e as NavigationFailure
		if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
			return // already on the target page, ignore
		}
		console.error('navigation failed', (e as Error).message)
	}
}

// Pattern 2: .catch
router.push({ name: 'detail' }).catch((e: any) => {
	const failure = e as NavigationFailure
	console.warn('navigation failed', failure.message)
})
```

::: tip Navigation failures don't throw
Even when a guard returns `false` or a native API fails, the synchronous code after `router.push(...)` still executes normally; use `await` / `.catch` when you need to know the outcome.

```ts
router.push({ name: 'profile' })
console.log('navigation initiated') // runs immediately, not skipped by a guard abort
```
:::

## Global Capture with onError

`router.onError` registers a global error handler and **returns a cancel function**:

```ts
const offError = router.onError((error, to, from) => {
	// error: Error (a NavigationFailure when navigation failed; narrow it with isNavigationFailure)
	console.warn(`navigation failed ${from.fullPath} -> ${to.fullPath}: ${error.message}`)
})

// Cancel the listener
offError()
```

Trigger timing summary:

- **Guard abort / cancellation**: `afterEach(to, from, failure)` receives the failure and each `onError` callback is invoked;
- **Native API failure**: `currentRoute` falls back to the source route and error handling is triggered;
- **Duplicate navigation**: only rejects with `DUPLICATED`; ignore it as needed.

## Troubleshooting Table for Common Failures

| Symptom | Error code | Likely cause | Fix |
| --- | --- | --- | --- |
| Navigation mysteriously blocked | `ABORTED` (4) | Some guard returned `false` | Check whether the guard branches match expectations |
| Navigation cancelled | `CANCELLED` (8) | A guard threw / guard timeout (default 10s) / redirect loop over 10 levels / insufficient stack for `back` | Check warning logs to locate the guard; review redirect conditions and stack depth |
| Error on rapid button taps | `DUPLICATED` (16) | Repeated `push` to the current address | Catch and ignore, or switch to `replace` |
| Got 32 | `ROUTE_NOT_FOUND` | `name` not registered in `routes` / invalid location (strict mode) | Verify the route config against `pages.json` |
| Got 64 | `NAVIGATION_API_ERROR` | Page not registered in `pages.json` / native API `fail` / stack-top confirmation failed | Verify page registration and path consistency in `pages.json` |
| Got 256 | `PLUGIN_REQUIRED` | Using `params` / `events` without the corresponding plugin | Register `ParamsPlugin` / `EventsPlugin` |

> Tip: in UTS, narrow the `e` caught by `catch (e)` with `(e as NavigationFailure)` or `(e as Error)` before reading fields.

## Next Steps

- [Plugin System](./plugins) — how to register the plugins behind `PLUGIN_REQUIRED`
- [RouterErrorCode](../api/type-router-error-code) — error code enum type reference
