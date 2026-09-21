# RouteLocation

The parsed and normalized route location — the object returned by `useRoute()` and `router.currentRoute`, and also the value the navigation promise resolves with on success (i.e. `NavigationResult`).

```ts
type RouteLocation = {
	path: string                  // page path (with leading slash, e.g. /pages/index/index)
	name: string | null           // named route name (null when unnamed)
	meta: RouteMeta               // route metadata
	query: Map<string, string>    // query parameters
	params: Map<string, string>   // route params (rebuilt by ParamsPlugin)
	fullPath: string              // path + serialized query
	hash: string                  // hash (not yet supported by uni-app x, always '')
	matched: RouteConfig[]        // matched route records (a single-element array under the flat model; [] for unregistered paths)
}
```

::: tip query / params are Maps
In uni-app x, query is passed as a string in the URL; access values with `.get(key)` / `.has(key)`.
:::

## Field Details

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string` | Page path, normalized with a leading slash |
| `name` | `string \| null` | Named route name; `null` (not undefined) for routes without a `name` |
| `meta` | `RouteMeta` | Comes from the matched `RouteConfig.meta`; an empty object for unregistered paths |
| `query` | `Map<string, string>` | URL query parameters; plugin-internal keys (e.g. `__params__`) are already stripped during `syncRoute` |
| `params` | `Map<string, string>` | Params rebuilt by `ParamsPlugin` via the associated store; an empty Map when unused |
| `fullPath` | `string` | `path` + the serialized query |
| `hash` | `string` | Reserved field, always `''` |
| `matched` | `RouteConfig[]` | Under the flat model the match result is a single-element array; `[]` when the path is unregistered |

## Navigation Result NavigationResult

```ts
type NavigationResult = RouteLocation
```

`push` / `replace` / `relaunch` / `back` all return `Promise<NavigationResult>`: on success they resolve with the target `RouteLocation`, on failure they reject with a [`NavigationFailure`](./type-router-error-code).

## Input Type RouteLocationRaw

The target location of a navigation (the argument to `push` / `replace` / `relaunch` / `resolve`):

```ts
type RouteLocationRaw = string | RawLocation
```

**String form**: `'/pages/detail/detail?id=1'` (can inline query).

**Object form RawLocation**:

| Field | Type | Description |
| --- | --- | --- |
| `path` | `string?` | Target path (takes precedence over `name`) |
| `name` | `string?` | Named route name |
| `query` | `Map<string, string>?` | Query parameters |
| `params` | `Map<string, string>?` | Route params (requires `ParamsPlugin`, passed via the associated store) |
| `hash` | `string?` | Reserved field (not yet supported by uni-app x, ignored) |
| `animationType` | `string?` | Transition animation type for this navigation (per-navigation override, consumed by `AnimationPlugin`) |
| `animationDuration` | `number?` | Transition animation duration for this navigation (ms, per-navigation override) |
| `events` | `Map<string, (data: any) => any>?` | Inter-page communication listener table (event name → callback, consumed by `EventsPlugin`, aligned with the events semantics of `uni.navigateTo`) |

Throws `ROUTE_NOT_FOUND` when neither `path` nor `name` is provided.

## Split Forms

- **RouteLocationNamedRaw**: only the `name`-based object form is allowed (`name` required, other fields same as RawLocation).
- **RouteLocationPathRaw**: only the `path`-based object form is allowed (`path` required, other fields same as RawLocation).

Both are used for type-narrowing scenarios (e.g. forcing one of the two in a custom navigation wrapper).

## Related APIs

- [useRoute()](./use-route)
- [Router Instance - currentRoute](./router-instance#currentroute)
- [RouteMeta](./type-route-meta)
- [RouterErrorCode](./type-router-error-code)
