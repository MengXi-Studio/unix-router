# useRouter()

`useRouter(): Router` returns the [Router instance](./router-instance) in the current context; it is the standard way to access the router in the Composable API.

```ts
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()
```

## Instance Resolution Order

`useRouter()` resolves the router instance in the following order:

1. **Inject first inside setup**: when inside a component's `setup` (or `script setup`), it first reads the router `provide`d by `app.use(router)` on the H5 side. In multi-instance scenarios, a component gets the **nearest installed instance**.
2. **Fall back to the global active router outside setup**: in option-style `methods`, event callbacks, and other non-setup contexts (where `inject` is unavailable), it falls back to the globally registered active router (the instance registered by the most recent `app.use(router)`).
3. **Throw when neither exists**: if it is neither injected nor installed, it throws — `Router instance not found. Please call app.use(router) first`.

## What `app.use(router)` Does

`router.install(app)` is triggered by `app.use(router)`:

- **H5 (Web)**: registers `provide` (for `useRouter` injection inside setup), mounts the `$router` / `$route` global properties, and registers the `onShow` global mixin (which calls `router.syncRoute()` automatically).
- **Native platforms (App / Mini Program)**: registers the global active router for `useRouter()`'s non-setup fallback; it is recommended to call `router.syncRoute()` manually in each page's `onShow`.

On both platforms the plugins' app-level hook (`onAppInstall`) is triggered.

## Usage in Components

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = (): void => {
	router.push({ name: 'about', query: new Map<string, string>([['from', 'home']]) })
}
</script>

<template>
	<view class="page" @click="goAbout">
		<text>Go to About</text>
	</view>
</template>
```

## Notes

- It relies on `app.use(router)` having installed the router; otherwise it throws (no usable instance exists before installation).
- It is the **same instance** as the globally exported `router` (single-instance scenarios); in multi-instance scenarios the injected result inside setup takes precedence.

## Related APIs

- [Router Instance](./router-instance)
- [useRoute()](./use-route)
- [useLink()](./use-link)
