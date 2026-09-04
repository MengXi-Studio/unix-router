# useRoute()

`useRoute()` returns the **reactive** current [RouteLocation](./type-route-location) (derived from `router.currentRoute`). It must be called in a component's `setup`.

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
```

## Return Value

Returns a reactive `RouteLocation`; changes to its properties trigger updates in any rendering / computed values that depend on it.

## Reading Route Information

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()

// reactive reads
console.log(route.path)      // /pages/detail/detail
console.log(route.fullPath)  // /pages/detail/detail?id=1
console.log(route.query.get('id')) // '1'
console.log(route.name)
console.log(route.meta)
</script>

<template>
	<text>Current page: {{ route.path }}</text>
</template>
```

::: tip query and params are both Maps
In uni-app x, query is passed in the URL as a string, and `RouteLocation.query` / `params` are both `Map<string, string>`. Use `.get(...)` to read and `.has(...)` to check.
:::

## Notes

- Can only be called in a component `setup` (or `script setup`)
- Requires the router to have been installed, otherwise it throws `SETUP_ERROR`
- If you need to read the route before the page's `onLoad`, call `router.syncRoute()` manually once first

## Related APIs

- [RouteLocation](./type-route-location)
- [useRouter()](./use-router)
- [Router - currentRoute](./router-instance#currentroute)