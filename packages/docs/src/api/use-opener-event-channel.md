# useOpenerEventChannel()

被打开页获取与「打开方」之间的定向通信通道，是 [EventsPlugin](../guide/events) EventChannel 语义在被打开侧的组合式入口。

```ts
import { useOpenerEventChannel } from '@meng-xi/unix-router'
```

## 签名

```ts
function useOpenerEventChannel(): EventChannel | null
```

**前置条件**（任一不满足则返回 `null`，不抛错）：

- 路由器注册了 `EventsPlugin`；
- 当前页面是被**携带 `events` 的导航**打开的（本页 URL 查询串中含插件注入的 `__evt__` 通道 key）。

## 返回的 EventChannel

| 成员 | 说明 |
| --- | --- |
| `emit(eventName, ...args)` | 向**打开方**回传数据（触发打开方 `events` 中注册的同名监听器） |
| `on(eventName, handler)` | 监听打开方推送的数据 |
| `once(eventName, handler)` | 同 `on`，触发一次后自动移除 |
| `off(eventName, handler?)` | 移除监听 |

## 示例

```ts
// 打开方：携带 events 注册监听
router.push({
	name: 'detail',
	events: {
		saved: (data) => {
			console.log('详情页保存了', data)
		}
	}
})
```

```ts
// 被打开页：获取通道并向打开方回传
const channel = useOpenerEventChannel()
if (channel !== null) {
	channel.emit('saved', { id: 1024 })
}
```

::: tip 时机说明
通道 key 优先取路由同步写入的内存值；页面 `onShow` 早于路由同步（`onRouteSync`）时会按当前页 URL 的 `__evt__` 参数兜底读取。两种方式都取不到时返回 `null`，调用侧需判空。
:::

## 相关 API

- [EventsPlugin](../guide/events) — 页面间通信完整指南（events 注册表 / eventBus / EventChannel）
- [useRouter()](./use-router) — 打开方通过 `push({ events })` 注册监听
