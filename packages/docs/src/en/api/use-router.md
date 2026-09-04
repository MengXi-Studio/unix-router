# useRouter()

`useRouter()` returns the [Router](./router-instance) instance in the current component context. It must be called in a component's `setup` (corresponding to the API of the same name in `vue-router`).

```ts
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()
```

## Return Value

Returns a `Router` instance (the same as the return value of `createRouter()`). You can call all of its navigation, guard registration, and state querying methods.

## Usage in a Component

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## Notes

- Can only be called in a component `setup` (or `script setup`)
- Requires the router to have been installed via `app.use(router)`, otherwise it throws `SETUP_ERROR`
- It is the **same instance** as the globally exported `router`; `useRouter()` is just a more Composition-API-style way to access the router without manual imports

## Related APIs

- [Router](./router-instance)
- [useRoute()](./use-route)
- [useLink()](./use-link)