# RouterErrorCode

The router error code enum. The values align with vue-router 4's `NavigationFailureType`, with additional uni-app x platform-related error codes.

```ts
import { RouterErrorCode } from '@meng-xi/unix-router'
```

| Enum | Value | Description |
| --- | --- | --- |
| `ABORTED` | `4` | The navigation was aborted by a guard (the guard returned `false`) |
| `CANCELLED` | `8` | A guard threw, or the redirect exceeded the maximum depth |
| `DUPLICATED` | `16` | Duplicate navigation to the current route |
| `ROUTE_NOT_FOUND` | `32` | No page route matched (uni-app x extension) |
| `NAVIGATION_API_ERROR` | `64` | The `uni.*` native navigation API call failed (uni-app x extension) |
| `SETUP_ERROR` | `128` | Error in the router installation environment (uni-app x extension) |
| `PLUGIN_REQUIRED` | `256` | A capability of an unregistered plugin was used (e.g. using `params` without registering `ParamsPlugin`); register the corresponding plugin first (uni-app x extension) |

::: tip
It is recommended to use `0b` bitwise operations or direct comparison, e.g. `failure.code === RouterErrorCode.ROUTE_NOT_FOUND`.
:::

## Related APIs

- [isNavigationFailure](./type-navigation-guard)
- [Error Handling Guide](../guide/error-handling)