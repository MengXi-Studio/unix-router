# Inter-page Communication

Pages in uni-app x follow a static model, and **targeted** inter-page communication (sending data back to "the page that opened you") has no out-of-the-box capability. `EventsPlugin` fills in the `events` semantics of the official `uni.navigateTo`: the opener carries a listener table, and the opened page sends data back or receives pushes through a channel.

## Registering the Plugin

```ts
import { createRouter, EventsPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new EventsPlugin()] // plugins are abstract classes; you must instantiate when registering
})
```

## Basic Model

- **Opener**: carries an `events: Map<string, (data: any) => any>` listener table on `push`, listening for data sent back by the opened page;
- **Opened page**: obtains the channel via `useOpenerEventChannel()`, calls `emit` to send data back, and `on` to receive pushes from the opener.

Internal mechanism: every navigation carrying `events` creates an **independent EventChannel**. The channel key is bridged across pages through the internal URL parameter `__evt__`. `__evt__` is a router-internal parameter stripped during state sync — it **never appears in `route.query`** and never pollutes user queries.

## Complete Two-way Communication Example

**Opener** (`pages/index/index.uvue`) — `push` carries the listener table to receive data sent back by the opened page:

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openDetail = () => {
	router.push({
		path: 'pages/detail/detail',
		events: new Map<string, (data: any) => any>([
			// listen for channel.emit('confirm', ...) from the opened page
			['confirm', (data: any) => {
				console.log('detail page sent back', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<text class="title">Opener</text>
		<button @click="openDetail">Open detail page</button>
	</view>
</template>
```

**Opened page** (`pages/detail/detail.uvue`) — obtain the channel, send data back to the opener / listen for pushes:

```vue
<script setup lang="uts">
import { useOpenerEventChannel } from '@meng-xi/unix-router'

// null when not opened by a navigation carrying events (or the plugin is not registered)
const channel = useOpenerEventChannel()

// 1. Send data back to the opener (triggers the same-named listener in the opener's events table)
const onConfirm = () => {
	if (channel !== null) {
		channel.emit('confirm', { id: 1024, confirmed: true })
	}
}

// 2. Listen for pushes from the opener (on / once return a listener id, usable with off for precise removal)
if (channel !== null) {
	const id = channel.on('preview', (data: any) => {
		console.log('push from opener', data)
	})
	// remove when needed: channel.off('preview', id)
}
</script>

<template>
	<view class="page">
		<text class="title">Opened page</text>
		<button @click="onConfirm">Confirm and send back</button>
	</view>
</template>
```

The four methods of `EventChannel` returned by `useOpenerEventChannel(): EventChannel | null`:

| Method | Description |
| --- | --- |
| `emit(eventName, ...args)` | Send data back to the opener |
| `on(eventName, fn)` | Listen for pushes from the opener; returns a listener id |
| `once(eventName, fn)` | Listen once; removed automatically after firing |
| `off(eventName, id?)` | Remove a listener by id; omit the id to remove all listeners of that event |

::: tip Usable inside onShow
`useOpenerEventChannel()` does not depend on the route state sync timing: if the page's `onShow` runs before the sync, the channel key is read as a fallback from the `__evt__` parameter in the current page URL, so you can send data back right inside `onShow`.
:::

::: warning EventsPlugin must be registered
If a navigation carries `events` without the plugin registered, the navigation rejects with `PLUGIN_REQUIRED` (error code `256`):

```
Using events requires registering EventsPlugin: createRouter({ plugins: [new EventsPlugin()] })
```
:::

## More Examples

### once: Receiving Once

A listener registered with `once` is removed automatically after firing once — ideal for scenarios where you only care about the first receipt (e.g. a ready notification):

```vue
<!-- opener pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openWork = () => {
	router.push({
		path: 'pages/work/work',
		events: new Map<string, (data: any) => any>([
			// if the opened page emits 'ready' multiple times, only the first one is handled; the listener is removed automatically afterwards
			['ready', (data: any) => {
				console.log('page ready for the first time', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<button @click="openWork"><text>Open the work page</text></button>
	</view>
</template>
```

```vue
<!-- opened page pages/work/work.uvue: broadcast once when ready -->
<script setup lang="uts">
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel()
if (channel !== null) {
	channel.emit('ready', { ts: Date.now() })
}
</script>

<template>
	<view class="page">
		<text class="title">Work page</text>
	</view>
</template>
```

### Sending Back Before Going Back + Opener Receives the Result

The typical "picker page" pattern: the opened page sends the selected result back and returns immediately; the opener receives the result in its listener:

```vue
<!-- opener pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openPicker = () => {
	router.push({
		path: 'pages/picker/picker',
		events: new Map<string, (data: any) => any>([
			['selected', (data: any) => {
				// the selected result sent back before the opened page returns (fires at emit time, no need to wait for the back navigation to finish)
				console.log('selected result', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<button @click="openPicker"><text>Open the picker</text></button>
	</view>
</template>
```

```vue
<!-- picker page pages/picker/picker.uvue -->
<script setup lang="uts">
import { useOpenerEventChannel, useRouter } from '@meng-xi/unix-router'

type PickResult = {
	id: number
	name: string
}

const channel = useOpenerEventChannel()
const router = useRouter()

const confirmPick = () => {
	if (channel !== null) {
		const result: PickResult = { id: 1024, name: 'Option A' }
		// send back first, then go back: the channel survives while the page is on the stack, so emit reaches the opener immediately
		channel.emit('selected', result)
	}
	router.back()
}
</script>

<template>
	<view class="page">
		<button @click="confirmPick"><text>Select and go back</text></button>
	</view>
</template>
```

::: tip emit forwards only the first extra argument
The underlying bus callback signature takes a single parameter `(data: any) => any`; of `emit(eventName, ...args)`, only the first extra argument is passed to the listener and the rest are ignored. To pass multiple fields, merge them into one object.
:::

## Channel Lifecycle

- **Navigation aborted / failed**: channels created by that navigation are cleaned up automatically (`onNavigationAbort`) — no leaks;
- **Page pushed normally**: the channel is kept. After going back, the opener and the opened page **can keep communicating**;
- Channels of different navigations are isolated from each other (namespaced by channel key), so multi-level pages coexisting never interfere.

## Global Event Bus eventBus

Channels solve **targeted** communication; for non-targeted broadcast scenarios (e.g. notifying global login-state changes), use the exported `eventBus` (a `UniEventBus` instance):

```ts
import { eventBus } from '@meng-xi/unix-router'

// Subscribe ($on / $once return a listener id)
const id = eventBus.$on('login-changed', (data: any) => {
	console.log('login state changed', data)
})

// Emit
eventBus.$emit('login-changed', { userId: 1 })

// Remove by id (UTS static targets don't support function reference comparison, hence removal by id)
eventBus.$off('login-changed', id)
```

`eventBus` is a self-contained implementation (`$on` / `$once` / `$off` / `$emit`, with `on` / `once` / `off` / `emit` aliases). It does not rely on the official `uni.$on` and has no platform version threshold.

## API Reference

### EventsPlugin

A `RouterPlugin` subclass — the targeted inter-page communication plugin.

| Member | Signature | Description |
| --- | --- | --- |
| `name` | constant `'events'` | Plugin name |
| `constructor()` | no parameters | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | Registers four hooks — afterResolve / prepareNavigation / routeSync / navigationAbort: create the channel, inject `__evt__`, sync the channel key, clean up on abort |

Registration (must be instantiated; on non-JS targets plugins must be implemented as classes — an object literal containing methods gets inferred as UTSJSONObject):

```ts
plugins: [new EventsPlugin()]
```

### EventsMap

```ts
type EventsMap = Map<string, (data: any) => any>
```

| Position | Type | Description |
| --- | --- | --- |
| key | `string` | Event name, custom (e.g. `'confirm'`, `'selected'`) |
| value | `(data: any) => any` | Callback; receives the first extra argument pushed by the other side |

### EventChannel

A single inter-page communication channel — the return type of `useOpenerEventChannel()`:

```ts
class EventChannel {
	readonly key: string
	on(eventName: string, fn: (data: any) => any): number
	once(eventName: string, fn: (data: any) => any): number
	off(eventName: string, target?: number | null): void
	emit(eventName: string, ...args: any[]): void
}
```

| Member | Parameters | Return value | Description |
| --- | --- | --- | --- |
| `key` | — | `string` | Unique channel identifier (bridged across pages via the internal URL parameter `__evt__`) |
| `on` | `eventName: string`; `fn: (data: any) => any` | `number` (listener id) | Listen for pushes from the opener |
| `once` | same as `on` | `number` | Listen once; removed automatically after firing |
| `off` | `eventName: string`; `target?: number \| null` (omit/`null` removes all listeners of the event) | `void` | Remove precisely by listener id |
| `emit` | `eventName: string`; `...args: any[]` | `void` | Send data back to the opener (only the first extra argument is forwarded) |

### useOpenerEventChannel

```ts
function useOpenerEventChannel(): EventChannel | null
```

Obtains the channel between the opened page and its opener: it prefers the in-memory key written during route sync, and falls back to reading the `__evt__` parameter in the current page URL when the page's `onShow` runs before the sync. Returns `null` — without throwing — when this page was not opened by a navigation carrying `events` (or the plugin is not registered).

### eventBus (UniEventBus)

A self-contained global event bus singleton (a `UniEventBus` instance; the class is exported as a type from the package entry). Its `$on` / `$once` / `$off` / `$emit` API aligns with the official `uni.$on` series, with `on` / `once` / `off` / `emit` aliases:

| Method | Signature | Return value | Description |
| --- | --- | --- | --- |
| `$on` / `on` | `(eventName: string, fn: (data: any) => any)` | `number` (listener id) | Subscribe to an event |
| `$once` / `once` | same as above | `number` | Subscribe once; removed automatically after firing |
| `$off` / `off` | `(eventName: string, target?: number \| null)` | `void` | Remove precisely by id; omit to remove all listeners of the event |
| `$emit` / `emit` | `(eventName: string, ...args: any[])` | `void` | Emit an event; callbacks receive only the first extra argument |

The callback type is defined uniformly as `type EventCallback = (data: any) => any`. UTS static targets do not support function reference equality, so `$off` removes by id rather than by callback (aligning with the id semantics of the official `uni.$on` since 4.31+).

### Error Behavior

| Condition | Behavior |
| --- | --- |
| Navigation carries `events` without EventsPlugin registered | The navigation rejects with a `NavigationFailure`, error code `PLUGIN_REQUIRED` (`256`) |
| This page was not opened by a navigation carrying `events` | `useOpenerEventChannel()` returns `null`, no error raised |
| Navigation aborted / failed | Channels created by that navigation are cleaned up automatically (`onNavigationAbort`); listeners do not leak |

### Platform Notes

- The channel key is bridged across pages via the internal URL parameter `__evt__` (constant `EVT_QUERY_KEY`); it is stripped during route state sync and never enters `route.query`;
- EventChannel sits on a self-contained bus; events are namespaced as `evt:{channel key}:{event name}`, so multi-level pages coexisting never interfere;
- The self-contained bus does not rely on the official `uni.$on`, hence it carries none of the official version thresholds (the official `uni.$on` requires Web 4.0 / WeChat Mini Program 4.41 / Android 3.91 / iOS 4.11 / HarmonyOS 4.61);
- Package entry exports: `EventsPlugin`, `eventBus`, and the types `EventChannel`, `EventsMap`, `UniEventBus`; the channel manager `channelManager` (`create` / `get` / `remove`) is an internal singleton of the plugin module and is not exported from the package entry.

## Next Steps

- [Navigation Animations](./animation) — add window transition animations to navigation
- [Plugin System](./plugins) — where EventsPlugin sits in the plugin system and the plugin context
