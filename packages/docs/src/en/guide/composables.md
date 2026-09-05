# Composables

unix-router provides Vue 3 Composition API interfaces that integrate seamlessly with `.uvue`'s `<script setup>`. The core ones are `useRouter` / `useRoute` / `useLink`, plus in-component guards.

## useRouter()

Get the **router instance**:

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')  // navigate
router.back()                      // go back
router.currentRoute.path           // current path (getter, not reactive)
```

- It resolves via `provide/inject` first, then falls back to the global active router.
- You can also read `router.currentRoute` directly (same source as `useRoute()`).

## useRoute()

Returns the **reactive** current route. Access fields **directly** in both script and template (no `.value`):

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.path                     // /pages/about/about
route.fullPath                 // /pages/about/about?id=1
route.query.get('id')          // "1" (Map API)
route.params.get('from')       // params (Map API)
route.meta.title               // meta
route.name                     // named route name; null when unnamed
```

**When does it update?**
- When a navigation is committed (when `afterEach` fires)
- When `syncRoute()` rebuilds the current route from the page stack on `onShow` (**handles non-router navigation: physical back button, TabBar switches**)

> So reading stale values outside navigation / `onShow` is normal; for page data, rely on that page's `onLoad` / `onShow`.

```vue
<template>
	<text>Current: {{ route.path }}</text>  <!-- auto-unwrapped in template -->
</template>
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()
</script>
```

## useLink()

The underlying building block for declarative navigation: returns reactive link state plus a trigger function, ideal for custom link / Tab / menu components.

```ts
import { useLink } from '@meng-xi/unix-router'
const link = useLink({ to: '/pages/about/about', replace: false })

link.href             // ComputedRef<string> full path (use link.href.value in script)
link.isActive         // ComputedRef<boolean> active (path prefix match)
link.isExactActive    // ComputedRef<boolean> exact active
await link.navigate() // perform navigation
```

> `isActive` etc. return `ComputedRef`; **use `.value` in script**, auto-unwrapped in template.

## In-Component Guards

| Function | Trigger |
| --- | --- |
| `onBeforeRouteLeave` | when leaving the current page (**most used**) |
| `onBeforeRouteUpdate` | on update (rare with the static page model) |
| `onBeforeRouteEnter` | on entry (limited effect) |

> uni-app x creates a new page instance on every navigation (no keep-alive reuse), so `onBeforeRouteEnter/Update` are limited; `onBeforeRouteLeave` is the most practical. See [Route Guards](./guards).

## Working with the Page Lifecycle

To obtain the real route in `onLoad` / `onShow`, sync first:

```ts
onLoad(options => {
	router.syncRoute()          // initialize currentRoute from the page stack
	const route = useRoute()
	// route.query is now aligned with the launch options
})

onShow(() => {
	router.syncRoute()          // sync again to cover physical back / TabBar switches
})
```

> `app.use(router)` already injects a global mixin that calls `syncRoute()` on `onShow`; manual calls are for "read earlier" scenarios.

## Related

- [useRoute API](../api/use-route) | [useRouter API](../api/use-router) | [useLink API](../api/use-link)