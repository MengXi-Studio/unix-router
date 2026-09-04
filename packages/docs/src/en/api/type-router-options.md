# RouterOptions

The initialization options for `createRouter()`.

```ts
import type { RouterOptions } from '@meng-xi/unix-router'

const options: RouterOptions = {
	routes,
	strict: true,
	guardTimeout: 10000,
	readyTimeout: 0
}
```

## Fields

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **Required**. The route configuration list; paths must match the registration in `pages.json` |
| `strict` | `boolean` | `true` | Strict mode; when enabled, unmatched named routes throw `ROUTE_NOT_FOUND` |
| `guardTimeout` | `number` | `10000` | Guard timeout (ms); `0` disables it |
| `readyTimeout` | `number` | `0` | Ready timeout (ms); `0` never times out |

## Related APIs

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)