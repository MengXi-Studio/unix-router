# 组合式 API

unix-router 提供 Vue 3 组合式 API，与 `.uvue` 的 `<script setup>` 无缝集成：`useRouter` / `useRoute` / `useLink` / `useOpenerEventChannel` / `onRouteChange`，外加组件内守卫。

## useRouter()

获取**路由器实例**：

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')  // 导航
router.back()                      // 返回
router.currentRoute.path           // 当前路径（getter，非响应式）
```

获取顺序：

1. **setup 上下文**：优先走 `provide/inject`（多实例场景下取本组件树注入的路由器）；
2. **非 setup 上下文**（选项式 methods、事件回调）：回退到全局活跃路由器（`app.use(router)` 时注册）；
3. 两者都没有 → **throw**（请先 `app.use(router)` 再调用）。

因此不仅能在 `<script setup>` 中使用，选项式 `methods`、事件回调里同样可用。

## useRoute()

返回**全局 reactive 对象**（当前路由位置）。脚本与模板中都**直接访问字段**（无 `.value`）：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.path                     // /pages/about/about
route.fullPath                 // /pages/about/about?id=1
route.query.get('id')          // '1'（Map API）
route.params.get('from')       // 参数（Map API）
route.meta.title               // 元信息
route.name                     // 命名路由名；未命名时为 null
```

**它何时更新？**

- 一次导航成功记账完成后（`afterEach` 之前的状态写入）
- 页面 `onShow` 触发 `syncRoute()`，从页面栈重建成当前路由（**响应非路由器导航：物理返回键、TabBar 切换**）

> 因此**在非导航、非 onShow 的时机读到旧值很正常**；页面级数据请以本页 `onLoad` / `onShow` 为准。

```vue
<template>
	<view class="page">
		<text>当前: {{ route.path }}</text>
	</view>
</template>
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()
</script>
```

## useLink()

声明式导航的底层：返回响应式链接状态 + 触发函数，适合自定义链接 / Tab / 菜单组件。

```ts
import { useLink } from '@meng-xi/unix-router'
const link = useLink({ to: '/pages/about/about' })
// 完整选项：{ to, replace?: boolean, relaunch?: boolean }

link.route            // ComputedRef<RouteLocation> 解析后的目标路由
link.href             // ComputedRef<string> 完整路径（脚本中用 link.href.value）
link.isActive         // ComputedRef<boolean> 是否激活（路径前缀匹配）
link.isExactActive    // ComputedRef<boolean> 是否精确激活（路径相等）
await link.navigate() // 执行导航（relaunch / replace / push 按选项分派）
```

> `isActive` 等返回的是 `ComputedRef`，**脚本中需 `.value`**，模板中自动解包。

## useOpenerEventChannel()

被打开页获取与「打开方」之间的定向通信通道（`on` / `once` / `off` / `emit`）。需注册 `EventsPlugin`，且本页是被携带 `events` 的导航打开的；否则返回 `null`：

```ts
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel()
if (channel != null) {
	channel.emit('done', 'ok') // 向打开方回传数据
}
```

详见[页面间通信](./events)。

## onRouteChange 监听路由变化

`router.onRouteChange` 注册全局监听器（任意导航与状态同步后触发），返回**取消函数**：

```ts
const stop = router.onRouteChange((to, from) => {
	console.log(`从 ${from.fullPath} 到 ${to.fullPath}`)
})

// 不再需要时
stop()
```

## 路由状态同步

`route` 对象与真实页面栈之间靠 `syncRoute()` 对齐：

- **H5 端**：`app.use(router)` 注册全局 mixin，在每个页面 `onShow` 自动调用 `syncRoute()`，无需手动处理。
- **原生端**（App / 小程序）：建议在页面 `onShow` 中自行调用 `router.syncRoute()`，覆盖物理返回、TabBar 切换等非路由器导航。

`onShow` 触发早于同步完成时（如需要在首帧读参数），建议直接在 `onLoad(options)` 里读启动 query：

```ts
onLoad((options) => {
	// options 是原生启动参数，不依赖路由同步
	const id = options?.['id'] ?? ''
})
```

## 组件内守卫

| 函数 | 触发时机 |
| --- | --- |
| `onBeforeRouteLeave` | 离开当前页面时（**最常用**） |
| `onBeforeRouteUpdate` | 更新时（静态页模型下极少触发） |
| `onBeforeRouteEnter` | 进入时（效果有限） |

> uni-app x 每次导航都新建页面实例（无 keep-alive 复用），故 `onBeforeRouteEnter/Update` 受限，`onBeforeRouteLeave` 最实用。详见[路由守卫](./guards)。

## 下一步

- [路由守卫](./guards) — 全局与组件内守卫
- [页面间通信](./events) — useOpenerEventChannel 与 EventsPlugin
- [useRoute API](../api/use-route) | [useRouter API](../api/use-router)
