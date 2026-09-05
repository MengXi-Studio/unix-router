# useLink()

`useLink()` provides the reactive state and trigger function for declarative navigation, suitable for custom link / menu components.

```ts
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
```

## Parameters

`useLink(options: UseLinkOptions)`

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `to` | `RouteLocationRaw` | — | The target route location (string or object) |
| `replace` | `boolean` | `false` | Whether to use `replace` navigation |
| `relaunch` | `boolean` | `false` | Whether to use `relaunch` navigation |

## Return Value

Returns `UseLinkReturn`:

| Property | Type | Description |
| --- | --- | --- |
| `route` | `ComputedRef<RouteLocation>` | The target route location (reactive) |
| `isActive` | `ComputedRef<boolean>` | Whether it is the currently active route (path prefix match) |
| `isExactActive` | `ComputedRef<boolean>` | Whether it is the currently exact-active route (full path match) |
| `href` | `ComputedRef<string>` | The reactive target href (equal to `route.fullPath`) |
| `navigate` | `() => Promise<void \| RouteLocation>` | Performs the navigation (internally calls `router.push` / `replace` / `relaunch`) |

## Custom Link Component Example

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const props = defineProps({
	to: { type: String, default: '' },
	replace: { type: Boolean, default: false }
})

const link = useLink({ to: props.to, replace: props.replace })

const onClick = () => {
	try {
		link.navigate()
	} catch (e) {
		// navigation failures are handled by the caller or router.onError
	}
}
</script>

<template>
	<view :class="link.isActive ? 'active' : ''" @click="onClick">
		<slot />
	</view>
</template>
```

## Related APIs

- [Composition API Guide](../guide/composables)