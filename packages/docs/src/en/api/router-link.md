# RouterLink Component

`<RouterLink>` is a declarative navigation component built on [useLink()](./use-link): a click triggers navigation, providing an interactive navigation entry point.

> Note: uni-app x uses a static page model with no vue-router-style in-page render outlet (`<router-view>`), so this component **only serves as an "interactive navigation entry point"** and does not render route content.

## Import

Import it directly from the library entry:

```ts
import { RouterLink } from '@meng-xi/unix-router'
```

## Props

| Prop | Type | Default | Description |
| --- | --- | --- | --- |
| `to` | `String` | `''` | Target route path (a string) |
| `replace` | `Boolean` | `false` | Whether to navigate with `replace` (the current page is replaced by the target page) |
| `relaunch` | `Boolean` | `false` | Whether to navigate with `relaunch` (all pages close, then the target opens) |

::: warning No active-class or other vue-router extension props
vue-router props like `active-class`, `custom`, and `exact-active-class` **do not exist** on this component. The static page model of uni-app x has no in-page render outlet; implement active states and similar needs yourself based on [useLink()](./use-link)'s `isActive` / `isExactActive`.
:::

When `replace` and `relaunch` are both `true`, `relaunch` takes precedence (matching `useLink` semantics).

## Basic Usage

```vue
<script setup lang="uts">
import { RouterLink } from '@meng-xi/unix-router'
</script>

<template>
	<view class="page">
		<RouterLink to="pages/about/about">
			<text>About</text>
		</RouterLink>
		<RouterLink to="pages/detail/detail" :replace="true">
			<text>Replace navigation</text>
		</RouterLink>
		<RouterLink to="pages/index/index" :relaunch="true">
			<text>Relaunch navigation</text>
		</RouterLink>
	</view>
</template>
```

Clicking the component triggers the internal `useLink().navigate()` to navigate; navigation failures surface via Promise reject and are handled by `router.onError` or the caller.

## Relationship with useLink

Internally, the component is a thin wrapper around `useLink({ to, replace, relaunch })` (a click calls `link.navigate()`). If you need custom interactions (menu active states, more style control), implement them yourself with [useLink()](./use-link):

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
const onClick = (): void => {
	link.navigate()
}
</script>
```

## Related APIs

- [useLink()](./use-link)
- [Composable API Guide](../guide/composables)
