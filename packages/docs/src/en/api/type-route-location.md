# RouteLocation

A resolved and normalized route location, and the object returned by `useRoute()` and `router.currentRoute`.

```ts
interface RouteLocation {
	path: string            // page path (with a leading slash, e.g. /pages/index/index)
	name?: string           // the named route name
	meta: RouteMeta         // route metadata
	query: Map<string, string> // query params
	params: Map<string, string> // route params
	fullPath: string        // path + serialized query
	hash: string            // hash (not yet supported in uni-app x, always an empty string)
	matched: RouteConfig[]  // the matched route records
}
```

::: tip query / params are Maps
In uni-app x, query is passed in the URL as a string; use `.get(key)` / `.has(key)` to access it.
:::

## Related APIs

- [useRoute()](./use-route)
- [Router - currentRoute](./router-instance#currentroute)
- [RouteMeta](./type-route-meta)