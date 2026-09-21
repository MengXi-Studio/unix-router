# 页面间通信

uni-app x 的页面是静态模型，页面间**定向通信**（把数据回传给「打开你的那个页面」）缺少开箱能力。`EventsPlugin` 补齐官方 `uni.navigateTo` 的 `events` 语义：打开方携带监听表，被打开页通过通道回传数据或接收推送。

## 注册插件

```ts
import { createRouter, EventsPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new EventsPlugin()] // 插件为 abstract class，注册时必须实例化
})
```

## 基本模型

- **打开方**：`push` 时携带 `events: Map<string, (data: any) => any>` 监听表，监听被打开页回传的数据；
- **被打开页**：`useOpenerEventChannel()` 获得通道，`emit` 回传数据、`on` 接收打开方推送。

内部机制：每次携带 `events` 的导航都会创建一个**独立的 EventChannel**，通道 key 经导航 URL 的内部参数 `__evt__` 跨页桥接。`__evt__` 属于路由器内部参数，状态同步时会被剔除，**不会出现在 `route.query` 中**，也不污染用户 query。

## 双向通信完整示例

**打开方**（`pages/index/index.uvue`）——`push` 携带监听表，接收被打开页的回传：

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openDetail = () => {
	router.push({
		path: 'pages/detail/detail',
		events: new Map<string, (data: any) => any>([
			// 监听被打开页的 channel.emit('confirm', ...)
			['confirm', (data: any) => {
				console.log('详情页回传', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<text class="title">打开方</text>
		<button @click="openDetail">打开详情页</button>
	</view>
</template>
```

**被打开页**（`pages/detail/detail.uvue`）——获取通道，向打开方回传 / 监听推送：

```vue
<script setup lang="uts">
import { useOpenerEventChannel } from '@meng-xi/unix-router'

// 未被携带 events 的导航打开（或未注册插件）时为 null
const channel = useOpenerEventChannel()

// 1. 回传数据给打开方（触发打开方 events 表中的同名监听器）
const onConfirm = () => {
	if (channel !== null) {
		channel.emit('confirm', { id: 1024, confirmed: true })
	}
}

// 2. 监听打开方推送（on / once 返回监听器 id，可用于 off 精确移除）
if (channel !== null) {
	const id = channel.on('preview', (data: any) => {
		console.log('打开方推送', data)
	})
	// 需要时移除：channel.off('preview', id)
}
</script>

<template>
	<view class="page">
		<text class="title">被打开页</text>
		<button @click="onConfirm">确认并回传</button>
	</view>
</template>
```

`useOpenerEventChannel(): EventChannel | null` 的 `EventChannel` 四个方法：

| 方法 | 说明 |
| --- | --- |
| `emit(eventName, ...args)` | 向打开方回传数据 |
| `on(eventName, fn)` | 监听打开方推送，返回监听器 id |
| `once(eventName, fn)` | 监听一次，触发后自动移除 |
| `off(eventName, id?)` | 按 id 移除监听器；省略 id 移除该事件全部监听器 |

::: tip onShow 内即可使用
`useOpenerEventChannel()` 不依赖路由状态同步时机：页面 `onShow` 早于同步执行时，会按当前页 URL 中的 `__evt__` 兜底读取通道 key，因此在 `onShow` 里就能直接回传数据。
:::

::: warning 需要注册 EventsPlugin
未注册插件却在导航中携带 `events`，导航会 reject `PLUGIN_REQUIRED`（错误码 `256`）：

```
使用 events 需注册 EventsPlugin：createRouter({ plugins: [new EventsPlugin()] })
```
:::

## 更多示例

### once：单次接收

`once` 注册的监听器触发一次后自动移除，适合「只关心第一个回执」的场景（如就绪通知）：

```vue
<!-- 打开方 pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openWork = () => {
	router.push({
		path: 'pages/work/work',
		events: new Map<string, (data: any) => any>([
			// 被打开页多次 emit('ready') 时只处理第一次，随后监听器自动移除
			['ready', (data: any) => {
				console.log('页面首次就绪', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<button @click="openWork"><text>打开工作页</text></button>
	</view>
</template>
```

```vue
<!-- 被打开页 pages/work/work.uvue：就绪后广播一次 -->
<script setup lang="uts">
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel()
if (channel !== null) {
	channel.emit('ready', { ts: Date.now() })
}
</script>

<template>
	<view class="page">
		<text class="title">工作页</text>
	</view>
</template>
```

### 返回前 emit 回传 + 打开方接收结果

典型「选择器页」模式：被打开页把选中结果回传后立刻返回，打开方在监听器里拿到结果：

```vue
<!-- 打开方 pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openPicker = () => {
	router.push({
		path: 'pages/picker/picker',
		events: new Map<string, (data: any) => any>([
			['selected', (data: any) => {
				// 被打开页返回前回传的选中结果（emit 时刻即触发，无需等待返回完成）
				console.log('选中结果', data)
			}]
		])
	})
}
</script>

<template>
	<view class="page">
		<button @click="openPicker"><text>打开选择器</text></button>
	</view>
</template>
```

```vue
<!-- 选择器页 pages/picker/picker.uvue -->
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
		const result: PickResult = { id: 1024, name: '选项 A' }
		// 先回传，再返回：页面正常入栈后通道保留，emit 立即触达打开方
		channel.emit('selected', result)
	}
	router.back()
}
</script>

<template>
	<view class="page">
		<button @click="confirmPick"><text>选中并返回</text></button>
	</view>
</template>
```

::: tip emit 只透传第一个附加参数
底层总线回调签名为单参数 `(data: any) => any`，`emit(eventName, ...args)` 的第一个附加参数会传给监听器，其余参数被忽略。需要传多个字段时合并为一个对象。
:::

## 通道生命周期

- **导航中止 / 失败**：本次导航创建的通道被自动清理（`onNavigationAbort`），不会泄漏；
- **页面正常入栈**：通道保留。页面返回（back）后打开方与被打开页之间**仍可继续通信**；
- 每次导航的通道相互隔离（按通道 key 命名空间隔离），多级页面并存时不会串扰。

## 全局事件总线 eventBus

通道解决的是「**定向**」通信；非定向的广播场景（如全局登录态变化通知）用导出的 `eventBus`（`UniEventBus` 实例）：

```ts
import { eventBus } from '@meng-xi/unix-router'

// 订阅（$on / $once 返回监听器 id）
const id = eventBus.$on('login-changed', (data: any) => {
	console.log('登录态变化', data)
})

// 触发
eventBus.$emit('login-changed', { userId: 1 })

// 按 id 移除（UTS 静态端不支持函数引用比较，故按 id 移除）
eventBus.$off('login-changed', id)
```

`eventBus` 为自研实现（`$on` / `$once` / `$off` / `$emit`，另有 `on` / `once` / `off` / `emit` 别名），不依赖官方 `uni.$on`，无平台版本门槛。

## API 参考

### EventsPlugin

`RouterPlugin` 子类，页面间定向通信插件。

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `name` | 常量 `'events'` | 插件名 |
| `constructor()` | 无参数 | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | 注册 afterResolve / prepareNavigation / routeSync / navigationAbort 四个 hook：创建通道、注入 `__evt__`、同步通道 key、中止时清理 |

注册写法（必须实例化；非蒸汽端插件必须 class 实现，对象字面量含方法会被推断为 UTSJSONObject）：

```ts
plugins: [new EventsPlugin()]
```

### EventsMap

```ts
type EventsMap = Map<string, (data: any) => any>
```

| 位置 | 类型 | 说明 |
| --- | --- | --- |
| key | `string` | 事件名，自定义（如 `'confirm'`、`'selected'`） |
| value | `(data: any) => any` | 回调，接收对端推送的第一个附加参数 |

### EventChannel

单条页面间通信通道，`useOpenerEventChannel()` 的返回类型：

```ts
class EventChannel {
	readonly key: string
	on(eventName: string, fn: (data: any) => any): number
	once(eventName: string, fn: (data: any) => any): number
	off(eventName: string, target?: number | null): void
	emit(eventName: string, ...args: any[]): void
}
```

| 成员 | 参数 | 返回值 | 说明 |
| --- | --- | --- | --- |
| `key` | — | `string` | 通道唯一标识（经 URL 内部参数 `__evt__` 跨页传递） |
| `on` | `eventName: string`；`fn: (data: any) => any` | `number`（监听器 id） | 监听打开方推送 |
| `once` | 同 `on` | `number` | 监听一次，触发后自动移除 |
| `off` | `eventName: string`；`target?: number \| null`（省略/null 移除该事件全部监听器） | `void` | 按监听器 id 精确移除 |
| `emit` | `eventName: string`；`...args: any[]` | `void` | 向打开方回传数据（只透传第一个附加参数） |

### useOpenerEventChannel

```ts
function useOpenerEventChannel(): EventChannel | null
```

被打开页获取与打开方之间的通道：优先读路由同步写入的内存 key，页面 `onShow` 早于路由同步时按当前页 URL 中的 `__evt__` 兜底读取。本页未被携带 `events` 的导航打开（或未注册插件）时返回 `null`，不抛错。

### eventBus（UniEventBus）

自研全局事件总线单例（`UniEventBus` 实例，包入口以类型导出该类），`$on` / `$once` / `$off` / `$emit` API 对齐官方 `uni.$on` 系列，另有 `on` / `once` / `off` / `emit` 别名：

| 方法 | 签名 | 返回值 | 说明 |
| --- | --- | --- | --- |
| `$on` / `on` | `(eventName: string, fn: (data: any) => any)` | `number`（监听器 id） | 订阅事件 |
| `$once` / `once` | 同上 | `number` | 订阅一次，触发后自动移除 |
| `$off` / `off` | `(eventName: string, target?: number \| null)` | `void` | 按 id 精确移除；省略移除该事件全部监听器 |
| `$emit` / `emit` | `(eventName: string, ...args: any[])` | `void` | 触发事件，回调只接收第一个附加参数 |

回调类型统一定义为 `type EventCallback = (data: any) => any`。UTS 静态端不支持函数引用相等比较，`$off` 按 id 移除而非按回调移除（对齐官方 `uni.$on` 4.31+ 的 id 语义）。

### 错误行为

| 条件 | 行为 |
| --- | --- |
| 未注册 EventsPlugin 时导航携带 `events` | 导航 reject `NavigationFailure`，错误码 `PLUGIN_REQUIRED`（`256`） |
| 本页未被携带 `events` 的导航打开 | `useOpenerEventChannel()` 返回 `null`，不报错 |
| 导航中止 / 失败 | 本次导航创建的通道自动清理（`onNavigationAbort`），监听器不泄漏 |

### 平台注意

- 通道 key 经导航 URL 内部参数 `__evt__`（常量 `EVT_QUERY_KEY`）跨页桥接，路由状态同步时被剥离，不进入 `route.query`；
- EventChannel 底层为自研总线，事件按 `evt:{通道key}:{事件名}` 命名空间隔离，多级页面并存不串扰；
- 自研总线不依赖官方 `uni.$on`，因此没有官方的版本门槛（官方 `uni.$on` 要求 Web 4.0 / 微信小程序 4.41 / Android 3.91 / iOS 4.11 / HarmonyOS 4.61）；
- 包入口导出：`EventsPlugin`、`eventBus`，类型 `EventChannel`、`EventsMap`、`UniEventBus`；通道管理器 `channelManager`（`create` / `get` / `remove`）为插件模块内部单例，未从包入口导出。

## 下一步

- [导航动画](./animation) — 为导航注入窗口过渡动画
- [插件系统](./plugins) — EventsPlugin 在插件体系中的位置与插件上下文
