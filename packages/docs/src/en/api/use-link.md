# useLink()

`useLink(options: UseLinkOptions): UseLinkReturn` provides reactive state and a trigger function for declarative navigation, suited for custom link / menu components. The [RouterLink component](./router-link) is internally built on it.

```ts
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
```

## Parameters

`useLink(options: UseLinkOptions)`

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `to` | `RouteLocationRaw` | — | Target route location (a string path or an object) |
| `replace` | `boolean` | `false` | Whether to navigate with `replace` |
| `relaunch` | `boolean` | `false` | Whether to navigate with `relaunch` |

> When `replace` and `relaunch` are both `true`, `relaunch` takes precedence.

## Return Value

Returns `UseLinkReturn`:

| Property | Type | Description |
| --- | --- | --- |
| `route` | `ComputedRef<RouteLocation>` | The resolved target route location (reactive) |
| `isActive` | `ComputedRef<boolean>` | Whether it is the currently active route (**path prefix matching**: equal to, or starting with, `target path + '/'`) |
| `isExactActive` | `ComputedRef<boolean>` | Whether it is the currently exactly active route (page paths equal) |
| `href` | `ComputedRef<string>` | Reactive target href (equals `route.value.fullPath`) |
| `navigate` | `() => Promise<RouteLocation \| null>` | Performs the navigation (`relaunch` → `router.relaunch`, `replace` → `router.replace`, default `router.push`; `push` returns the target location, `replace` / `relaunch` return `null`) |

## Custom Navigation Menu Component Example

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const props = defineProps({
	to: { type: String, default: '' },
	replace: { type: Boolean, default: false }
})

const link = useLink({ to: props.to, replace: props.replace })

const onClick = (): void => {
	link.navigate()
}
</script>

<template>
	<view :class="link.isActive.value ? 'menu-item active' : 'menu-item'" @click="onClick">
		<text>{{ link.href.value }}</text>
		<slot />
	</view>
</template>
```

::: tip
`isActive` and friends are `ComputedRef`s; **accessing them in `<script>` requires `.value`** (the template unwraps automatically). Navigation failures surface via Promise reject — you can `catch` at the `navigate()` call site, or delegate them uniformly to `router.onError`.
:::

## Relationship with RouterLink

`<RouterLink>` is the componentized wrapper of `useLink({ to, replace, relaunch })`: a click calls `link.navigate()`. When you need custom interactions (menu active states, complex styling, etc.), implement them yourself with `useLink()` directly.

## Related APIs

- [RouterLink Component](./router-link)
- [Composable API Guide](../guide/composables)
