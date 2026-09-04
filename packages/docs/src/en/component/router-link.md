# RouterLink

`RouterLink` is a declarative navigation component, implemented internally through [useLink()](../api/use-link). Clicking it performs a navigation and reactively provides active state.

## Import

```ts
// register globally, or import on demand
import { RouterLink } from '@meng-xi/unix-router'
```

## Props

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `to` | `string` | `''` | The target route location (path or object) |
| `replace` | `boolean` | `false` | Whether to use `replace` navigation |
| `relaunch` | `boolean` | `false` | Whether to use `relaunch` navigation |

## Example

```vue
<template>
	<RouterLink to="pages/about/about">About</RouterLink>
	<RouterLink :to="{ name: 'about', query: new Map([['from', 'home']]) }">About (with params)</RouterLink>
	<RouterLink to="pages/profile/profile" replace>Replace to Profile</RouterLink>
</template>
```

::: tip `to` supports a string or an object
`to` is a `RouteLocationRaw` (a string path or an object), matching the arguments of `router.push`.
:::

## Custom Styling and Active State

If you need to customize styles based on the active state, wrap it yourself with the lower-level [useLink()](../api/use-link), which returns the `isActive` / `isExactActive` reactive state.

## Related APIs

- [useLink()](../api/use-link)
- [Route Navigation Guide](../guide/navigation)