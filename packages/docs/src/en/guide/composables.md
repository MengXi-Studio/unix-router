# Composables

unix-router provides Vue 3 composables that integrate seamlessly with `<script setup>` in `.uvue` files: `useRouter` / `useRoute` / `useLink` / `useOpenerEventChannel` / `onRouteChange`, plus in-component guards.

## useRouter()

Returns the **router instance**:

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')  // navigate
router.back()                      // go back
router.currentRoute.path           // current path (getter, not reactive)
```

Resolution order:

1. **setup context**: goes through `provide/inject` first (in multi-instance scenarios, picks the router injected into this component tree);
2. **non-setup context** (options-style methods, event callbacks): falls back to the global active router (registered via `app.use(router)`);
3. neither available → **throws** (call `app.use(router)` first).

So it works not only in `<script setup>` but also in options-style `methods` and event callbacks.

## useRoute()

Returns the **global reactive object** (the current route location). Access fields **directly** in both script and template (no `.value`):

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.path                     // /pages/about/about
route.fullPath                 // /pages/about/about?id=1
route.query.get('id')          // '1' (Map API)
route.params.get('from')       // params (Map API)
route.meta.title               // metadata
route.name                     // named route name; null when unnamed
```

**When does it update?**

- After a navigation completes and is recorded (the state write that happens before `afterEach`)
- When a page's `onShow` triggers `syncRoute()`, rebuilding the current route from the page stack (**responds to non-router navigation: the physical back button, TabBar switching**)

> Therefore **reading a stale value outside of navigation or onShow is normal**; for page-level data, rely on the page's own `onLoad` / `onShow`.

```vue
<template>
	<view class="page">
		<text>Current: {{ route.path }}</text>
	</view>
</template>
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()
</script>
```

## useLink()

The machinery behind declarative navigation: returns reactive link state plus a trigger function, ideal for custom link / Tab / menu components.

```ts
import { useLink } from '@meng-xi/unix-router'
const link = useLink({ to: '/pages/about/about' })
// full options: { to, replace?: boolean, relaunch?: boolean }

link.route            // ComputedRef<RouteLocation> — the resolved target route
link.href             // ComputedRef<string> — full path (use link.href.value in script)
link.isActive         // ComputedRef<boolean> — active (path prefix match)
link.isExactActive    // ComputedRef<boolean> — exactly active (path equality)
await link.navigate() // performs the navigation (relaunch / replace / push, dispatched by options)
```

> `isActive` and friends return `ComputedRef` — **you need `.value` in script**; they auto-unwrap in templates.

## useOpenerEventChannel()

On the opened page, returns the directed communication channel with the "opener" (`on` / `once` / `off` / `emit`). Requires `EventsPlugin`, and this page must have been opened by a navigation carrying `events`; otherwise it returns `null`:

```ts
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel()
if (channel != null) {
	channel.emit('done', 'ok') // send data back to the opener
}
```

See [Inter-Page Communication](./events) for details.

## onRouteChange: Listening for Route Changes

`router.onRouteChange` registers a global listener (fires after any navigation and state sync) and returns a **cancel function**:

```ts
const stop = router.onRouteChange((to, from) => {
	console.log(`from ${from.fullPath} to ${to.fullPath}`)
})

// when no longer needed
stop()
```

## Route State Sync

The `route` object is aligned with the real page stack via `syncRoute()`:

- **H5**: `app.use(router)` registers a global mixin that automatically calls `syncRoute()` on every page's `onShow` — no manual handling needed.
- **Native platforms** (App / Mini Program): call `router.syncRoute()` yourself in the page's `onShow` to cover non-router navigation such as the physical back button and TabBar switching.

When `onShow` fires before the sync completes (e.g. you need launch parameters in the first frame), prefer reading the launch query directly in `onLoad(options)`:

```ts
onLoad((options) => {
	// options holds the native launch parameters, independent of route sync
	const id = options?.['id'] ?? ''
})
```

## In-Component Guards

| Function | When it fires |
| --- | --- |
| `onBeforeRouteLeave` | When leaving the current page (**most useful**) |
| `onBeforeRouteUpdate` | On update (rarely fires under the static page model) |
| `onBeforeRouteEnter` | On enter (limited effect) |

> uni-app x creates a new page instance for every navigation (no keep-alive reuse), so `onBeforeRouteEnter/Update` are limited and `onBeforeRouteLeave` is the most practical. See [Route Guards](./guards) for details.

## Next Steps

- [Route Guards](./guards) — global and in-component guards
- [Inter-Page Communication](./events) — useOpenerEventChannel and EventsPlugin
- [useRoute API](../api/use-route) | [useRouter API](../api/use-router)
