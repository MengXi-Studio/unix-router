# RouterErrorCode

The route error code enum. Its values are aligned with vue-router 4's `NavigationFailureType`, with additional uni-app x platform-specific error codes.

```ts
import { RouterErrorCode } from '@meng-xi/unix-router'
```

| Error code | Value | Trigger scenario |
| --- | --- | --- |
| `ABORTED` | `4` | A guard returned `false`, aborting the navigation (including a non-positive-integer `delta` for `back()`) |
| `CANCELLED` | `8` | A guard threw an `Error`, guard timeout (`guardTimeout` default 10000ms), redirect exceeded the max depth (10), or `back()` with an insufficient page stack |
| `DUPLICATED` | `16` | Repeatedly `push` to the current address (`path`+`query`+`params`+`hash` all identical to current; checked by `push` only) |
| `ROUTE_NOT_FOUND` | `32` | No route matched (a `name` not registered in strict mode) or an invalid location (uni-app x extension) |
| `NAVIGATION_API_ERROR` | `64` | A `uni.*` native navigation API call failed, or the page-stack-top confirmation failed after navigation completed (uni-app x extension) |
| `SETUP_ERROR` | `128` | Router installation environment error (uni-app x extension). **Reserved code — not thrown by the current version.** |
| `PLUGIN_REQUIRED` | `256` | Using a plugin capability without registering the corresponding plugin (e.g. using `params` without `ParamsPlugin`, or `events` without `EventsPlugin`) (uni-app x extension) |

::: tip How to check
Pair with `isNavigationFailure(error, type?)` to narrow the check, or compare directly (`failure.code === RouterErrorCode.ROUTE_NOT_FOUND`). The `to` / `from` fields of `NavigationFailure` help locate the failure context further.
:::

## Related API

- [Precise Checks with isNavigationFailure](../guide/error-handling#precise-checks-with-isnavigationfailure)
- [Error Handling Guide](../guide/error-handling)
