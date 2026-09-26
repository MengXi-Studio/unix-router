# Navigation Animations

`AnimationPlugin` adds window transition animations to navigation (aligned with the native uni-app x `animationType`), supporting a global default plus per-navigation overrides.

## Registering the Plugin

```ts
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()], // plugins are abstract classes; you must instantiate when registering
	animation: { type: 'slide-in-right', duration: 300 } // global default animation (optional)
})
```

## Global Default Animation

`RouterOptions.animation` (`NavigationAnimation`: `{ type, duration? }`) sets the default animation for all navigations; when `duration` is omitted it defaults to `300`ms.

## Per-navigation Override

Carry `animationType` / `animationDuration` on the navigation location — effective for **this navigation only**, taking precedence over the global default:

```ts
await router.push({
	path: 'pages/detail/detail',
	animationType: 'fade-in',
	animationDuration: 500
})
```

::: tip No per-navigation override for back
`back()` takes no location parameters and uses the global default animation (automatically mapped to the exit variant, see below). Per-navigation overrides only apply to `push` / `replace` / `relaunch`.
:::

### Example: Global Default + Per-page Override on the Detail Page

```ts
// router/index.uts: global default slide-in-right
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()],
	animation: { type: 'slide-in-right', duration: 300 }
})
```

```vue
<!-- home page pages/index/index.uvue: override to fade-in when opening the detail page -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openDetail = () => {
	router.push({
		path: 'pages/detail/detail',
		animationType: 'fade-in',      // per-navigation override of the global default
		animationDuration: 400
	})
}
</script>

<template>
	<view class="page">
		<button @click="openDetail"><text>Open detail (fade-in)</text></button>
	</view>
</template>
```

## Full AnimationType Table

18 values in total, grouped into general, enter, and exit:

| Group | Values |
| --- | --- |
| General | `auto` (platform default), `none` (no animation) |
| Enter | `slide-in-right`, `slide-in-left`, `slide-in-top`, `slide-in-bottom`, `fade-in`, `zoom-in`, `zoom-fade-in`, `pop-in` |
| Exit | `slide-out-right`, `slide-out-left`, `slide-out-top`, `slide-out-bottom`, `fade-out`, `zoom-out`, `zoom-fade-out`, `pop-out` |

## Exit Animation Mapping for back

`back()` automatically maps the global default animation to the corresponding **exit variant**; values already of the exit kind are used as-is.

| Global default (enter) | Actually played by back |
| --- | --- |
| `slide-in-right` | `slide-out-right` |
| `slide-in-left` | `slide-out-left` |
| `slide-in-top` | `slide-out-top` |
| `slide-in-bottom` | `slide-out-bottom` |
| `fade-in` | `fade-out` |
| `zoom-in` | `zoom-out` |
| `zoom-fade-in` | `zoom-fade-out` |
| `pop-in` | `pop-out` |
| `auto` / `none` | `none` (no animation) |

### Example: Exit Animation Plays Automatically on Back

`back()` takes no per-navigation animation parameter; the back animation is auto-mapped from the global default (e.g. `slide-in-right` → `slide-out-right`), so pages need no extra configuration:

```vue
<!-- detail page pages/detail/detail.uvue (with global default slide-in-right) -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goBack = () => {
	// App: the native window plays slide-out-right (animation fields not supported officially on Mini Programs);
	// H5: the 300ms exit animation plays to completion first, then navigateBack actually executes
	router.back()
}
</script>

<template>
	<view class="page">
		<button @click="goBack"><text>Go back (auto exit animation)</text></button>
	</view>
</template>
```

## Platform Implementation Differences

| Platform | Implementation |
| --- | --- |
| App | Passes `animationType` / `animationDuration` through to the native `uni.*` navigation APIs (native window animation, officially App-only) |
| Mini Programs | Animation fields not supported officially, no animation |
| H5 | Web Animations API (`element.animate`) plays enter / exit animations on the page container — no CSS `@keyframes` needed |

Two timing details on H5:

- **Enter animation**: in the navigation-complete callback, the **starting keyframe styles are applied synchronously** (e.g. `translateX(100%)` with a forced reflow) so the new page's first frame is already off-screen, then the slide-in plays on the next frame — avoiding the jarring "content flashes in place, then jumps off-screen to slide in". Inline starting styles are cleaned up after the animation ends;
- **Back animation**: the current page's **exit animation plays first and completes**, then `navigateBack` actually executes.

::: warning No animation for switchTab
When the target is a `meta.isTab` page, navigation goes through `uni.switchTab`, and the native `switchTab` accepts no animation parameters — neither global nor per-navigation animation settings have any effect on TabBar page navigation.
:::

::: tip Without the plugin registered
When `AnimationPlugin` is not registered, navigations carrying `animationType` still execute normally (the animation is ignored) — functionality is unaffected.
:::

## API Reference

### AnimationPlugin

A `RouterPlugin` subclass — the navigation animation plugin.

| Member | Signature | Description |
| --- | --- | --- |
| `name` | constant `'animation'` | Plugin name |
| `constructor()` | no parameters | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | Reads `options.animation` and the per-navigation `animationType` / `animationDuration`, resolves the effective animation at the prepare stage and injects it into the navigation options; on H5 it additionally hooks "play the exit animation before back" and "play the enter animation on completion" |

Registration (must be instantiated; on non-JS targets plugins must be implemented as classes — an object literal containing methods gets inferred as UTSJSONObject):

```ts
plugins: [new AnimationPlugin()]
```

### NavigationAnimation

```ts
type NavigationAnimation = {
	type: AnimationType
	duration?: number // ms; defaults to 300 when omitted
}
```

| Member | Type | Description |
| --- | --- | --- |
| `type` | `AnimationType` | Animation type; 18 values, see the "Full AnimationType Table" above |
| `duration` | `number` (optional) | Animation duration; defaults to `DEFAULT_ANIMATION_DURATION = 300` |

### Animation Configuration Entrypoints

| Entrypoint | Type | Scope |
| --- | --- | --- |
| `RouterOptions.animation` | `NavigationAnimation` | Global default; applies to all navigations |
| Navigation-location `animationType` | `string` (an `AnimationType` value) | Per-navigation override; only effective for `push` / `replace` / `relaunch`; `back` has no such parameter |
| Navigation-location `animationDuration` | `number` | Per-navigation duration override |

### Plugin Internal Utilities

The following utilities are exported by the plugin module (the package entry exports only `AnimationPlugin`; custom plugins can reuse them at module level):

| Name | Signature | Description |
| --- | --- | --- |
| `DEFAULT_ANIMATION_DURATION` | constant `300` | Default animation duration (ms) |
| `pickAnimation` | `(location: RouteLocationRaw \| null) => NavigationAnimation \| null` | Extracts the per-navigation animation from the navigation location (returns `null` when there is no `animationType`) |
| `normalizeAnimation` | `(anim: NavigationAnimation) => NavigationAnimation` | Fills in the default duration |
| `toExitType` | `(type: AnimationType) => AnimationType` | Enter → exit mapping (see the mapping table above); exit values pass through unchanged; `auto` / `none` return `none` |
| `ANIM_DATA_KEY` | constant `'__animation__'` | Storage key for the per-navigation animation in pluginData |
| `animatePageEnter` / `animatePageExit` | exported conditionally, Web only | H5 enter / exit animation implementation (WAAPI); return `boolean` / `Promise<void>` |

### Error Behavior

This plugin produces no navigation failures (no `PLUGIN_REQUIRED` or other error codes):

| Condition | Behavior |
| --- | --- |
| `animationType` carried without AnimationPlugin registered | The animation is ignored; the navigation executes normally |
| H5 environment without WAAPI support | Silent degradation: starting placeholder styles are cleaned up and the navigation proceeds normally |
| Target is a TabBar page (switchTab) | The native `switchTab` accepts no animation parameters, so animation configuration has no effect |

### Platform Notes

- **App**: the resolved `animationType` / `animationDuration` are passed through directly to the native `uni.*` navigation APIs and rendered as native window animations (Mini Programs do not support the animation fields officially, no animation);
- **H5**: keyframes are played on the page container (the `uni-page` element) via the Web Animations API (`element.animate`), without CSS `@keyframes`; the enter animation **applies its starting styles synchronously and forces a reflow** inside the navigation-complete callback, then plays one frame later, avoiding the "content flashes in place, then slides in" artifact; on back, the exit animation plays first (awaited by duration) before `navigateBack` actually executes;
- H5 keyframe names correspond one-to-one with the App-side `animationType` values (8 enter, 8 exit); unmatched types play no animation.

## Next Steps

- [uni API Interception](./interceptor) — route natively-initiated navigations through the guard chain too
- [Plugin System](./plugins) — where AnimationPlugin sits in the plugin system
- [RouterOptions](../api/type-router-options) — the `animation` option type definition
