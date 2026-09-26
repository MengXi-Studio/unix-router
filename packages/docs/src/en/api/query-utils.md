# Query Utilities

Convenience helpers for atomically reading strongly-typed values from `route.query` (a `Map<string, string>`), replacing hand-written `parseInt` + boundary checks.

```ts
import { queryInt, queryNumber, queryBool } from '@meng-xi/unix-router'
```

All three functions share the same read rules: a missing key or an empty-string value returns the default immediately; a parse failure also returns the default — they **never throw**.

## queryInt()

Parses an integer value (`parseInt`, base 10):

```ts
function queryInt(query: Map<string, string>, key: string, defaultValue: number = 0): number
```

```ts
const route = useRoute()
const page = queryInt(route.query, 'page', 1) // ?page=3 → 3; missing/invalid → 1
```

## queryNumber()

Parses a floating-point value (`parseFloat`):

```ts
function queryNumber(query: Map<string, string>, key: string, defaultValue: number = 0): number
```

```ts
const price = queryNumber(route.query, 'price', 9.9) // ?price=19.5 → 19.5
```

## queryBool()

Parses a boolean. `'0'` and `'false'` are treated as `false`; **any other non-empty value is `true`** (`'1'` / `'true'` / `'yes'`, etc.):

```ts
function queryBool(query: Map<string, string>, key: string, defaultValue: boolean = false): boolean
```

```ts
const compact = queryBool(route.query, 'compact', false) // ?compact=1 → true; ?compact=0 → false
```

::: tip When to use
The typical place is a page's `onShow`: by then the route state has been synced, so `route.query` reflects the current page's real URL. See the playground's `queryNumber(route.query, 'num', 0)` for an example.
:::

## Related APIs

- [useRoute()](./use-route) — obtain the current route location (the source of `query`)
- [Navigation](../guide/navigation) — how query params are written
