# RouterLink Component

`<RouterLink>` is a declarative navigation component built on [useLink()](./use-link), providing a clickable navigation entry.

> Note: uni-app x uses a static page model, so there is no in-page rendering slot like vue-router's `<router-view>`; this component only serves as an interactive navigation entry.

## Import

Import it directly from the library entry:

```ts
import { RouterLink } from '@meng-xi/unix-router'
```

## Props

| Prop | Type | Default | Description |
| --- | --- | --- | --- |
| `to` | `string` | `''` | Target route path (string) |
| `replace` | `boolean` | `false` | Whether to use `replace` navigation (replace the current page) |
| `relaunch` | `boolean` | `false` | Whether to use `relaunch` navigation (close all pages, then open the target) |

> `replace` and `relaunch` are mutually exclusive: when both are `true`, `relaunch` takes precedence (same semantics as `useLink`).

## Usage

```vue
<template>
	<view class="page">
		<RouterLink to="pages/about/about">About</RouterLink>
		<RouterLink to="pages/detail/detail" replace>Replace</RouterLink>
		<RouterLink to="pages/index/index" relaunch>Relaunch</RouterLink>
	</view>
</template>

<script setup lang="uts">
import { RouterLink } from '@meng-xi/unix-router'
</script>
```

Clicking the component triggers navigation; navigation failures are handled by `router.onError` / the caller.

## Relation to useLink

Under the hood the component is a thin wrapper around `useLink({ to, replace, relaunch })` (it calls `link.navigate()` on click). For custom interactions (e.g. menu active states, more style control), implement it directly with [useLink()](./use-link):

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
const onClick = () => link.navigate()
</script>
```

## Related APIs

- [useLink()](./use-link)
- [Composition API guide](../guide/composables)