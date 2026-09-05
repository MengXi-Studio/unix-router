# Route Meta

Every route can carry arbitrary custom data via `meta`, readable by guards, pages, and components. It ships with common fields and is strongly typed (UTS `type`).

## Built-in Fields

```ts
type RouteMeta = {
	title?: string        // page title (usable for the nav bar title)
	isTab?: boolean       // whether it is a tabBar page (decides switchTab)
	requireAuth?: boolean // whether login is required (usually paired with a guard)
}
```

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } }
```

## Reading meta

In pages / composables:

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.meta.title        // current page title
route.meta.isTab
```

In guards, read the **target page's** meta to decide:

```ts
router.beforeEach((to, from) => {
	// decide navigation behavior by meta
	if (to.meta.isTab === true) {
		// use switchTab
	}
})
```

## Three Typical Uses of meta

### 1. A login-auth switch: `requireAuth`

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login', query: new Map([['redirect', to.fullPath]]) }
	}
	return true
})
```

### 2. Page title: `title`

Set the nav bar title on `onShow`:

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

onShow(() => {
	uni.setNavigationBarTitle({ title: route.meta.title ?? '' })
})
```

### 3. TabBar detection: `isTab`

The router uses it to decide `switchTab` vs `navigateTo`. **Remember to set `meta.isTab = true` for TabBar pages** in your route config.

## Custom meta Fields

In UTS, `RouteMeta` is a `type`; add custom fields directly to the type declaration (recommend a shared types file):

```ts
// Extend in your own type file and re-export as needed
export type AppRouteMeta = {
	icon?: string
	tag?: 'new' | 'hot'
}
```

> ⚠️ Unlike vue-router, `declare module { interface RouteMeta {...} }` merges via interface inheritance, and **UTS does not support interface declaration merging**. So extend the type definition directly rather than relying on module augmentation.

## Common Pitfalls

- **Forgetting `meta.isTab`**: a TabBar page opened with `navigateTo` may fail / be abnormal. Give TabBar pages `isTab: true`.
- **Reading an undefined field**: optional fields may read as `undefined`/`null`; check with `=== true` / `=== undefined`, not truthiness (UTS is strongly typed).
- **Reading meta at the wrong time**: before the first frame you may be reading the initial route's meta; call `syncRoute()` first (see [Composables](./composables#working-with-the-page-lifecycle)).