# Composition API

unix-router provides the Vue 3 Composition API, integrating seamlessly with `.uvue`'s `<script setup>`.

## useRouter()

Returns the router instance:

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')
router.back()
```

## useRoute()

Returns the **reactive** current route:

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

console.log(route.path) // current path
console.log(route.query.get('keyword')) // query
console.log(route.params.get('id')) // params
```

`useRoute` updates automatically when the router navigates and when state is synced (`syncRoute`).

## useLink()

Returns reactive link state (the `RouterLink` component is built on it internally):

```ts
import { useLink } from '@meng-xi/unix-router'
import { computed } from 'vue'

const link = useLink({ to: { name: 'about' }, replace: false })

link.href // ComputedRef<string> full path
link.isActive // ComputedRef<boolean>
link.isExactActive // ComputedRef<boolean>
link.navigate() // performs the navigation
```

## In-Component Guards

- `onBeforeRouteLeave` — triggered when leaving the current page
- `onBeforeRouteUpdate` — triggered on update (rare under the static page model)
- `onBeforeRouteEnter` — triggered on entry (limited effect)

See [Route Guards](./guards) for details.

## Usage in Lifecycle Hooks

If you need to read the route inside `onLoad` / `onShow`, call `router.syncRoute()` to sync manually first, then read:

```ts
onLoad(options => {
	router.syncRoute()
	const route = useRoute()
	// route.query comes from the onLoad options
})
```