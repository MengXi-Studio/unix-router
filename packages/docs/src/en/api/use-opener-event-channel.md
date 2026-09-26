# useOpenerEventChannel()

A composable for the opened page to obtain the directed communication channel with its opener — the opened-side composable entry point of the [EventsPlugin](../guide/events) EventChannel semantics.

```ts
import { useOpenerEventChannel } from '@meng-xi/unix-router'
```

## Signature

```ts
function useOpenerEventChannel(): EventChannel | null
```

**Preconditions** (returns `null` without throwing when any of them is unmet):

- The router has registered `EventsPlugin`;
- The current page was opened by a **navigation carrying `events`** (the page URL query contains the plugin-injected `__evt__` channel key).

## The Returned EventChannel

| Member | Description |
| --- | --- |
| `emit(eventName, ...args)` | Sends data back to the **opener** (triggers the listener of the same name registered in the opener's `events`) |
| `on(eventName, handler)` | Listens for data pushed by the opener |
| `once(eventName, handler)` | Same as `on`, but auto-removed after firing once |
| `off(eventName, handler?)` | Removes a listener |

## Example

```ts
// Opener: register listeners via events
router.push({
	name: 'detail',
	events: {
		saved: (data) => {
			console.log('detail page saved', data)
		}
	}
})
```

```ts
// Opened page: get the channel and send data back to the opener
const channel = useOpenerEventChannel()
if (channel !== null) {
	channel.emit('saved', { id: 1024 })
}
```

::: tip Timing
The channel key prefers the in-memory value written during route sync; when the page's `onShow` runs before route sync (`onRouteSync`), it falls back to reading the `__evt__` param of the current page URL. When neither is available it returns `null` — check for `null` at the call site.
:::

## Related APIs

- [EventsPlugin](../guide/events) — the full page-communication guide (events registry / eventBus / EventChannel)
- [useRouter()](./use-router) — the opener registers listeners via `push({ events })`
