# 组合式 API

unix-router 提供 Vue 3 组合式 API，与 `.uvue` 的 `<script setup>` 无缝集成。核心是 `useRouter` / `useRoute` / `useLink`，外加组件内守卫。

## useRouter()

获取**路由器实例**：

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')  // 导航
router.back()                      // 返回
router.currentRoute.path           // 当前路径（getter，非响应式）
```

- 优先走 `provide/inject` 拿到路由器；未注入时回退到全局活跃路由器。
- 也可用 `router.currentRoute` 直接读当前路由（与 `useRoute()` 同源）。

## useRoute()

返回**响应式**的当前路由对象。脚本与模板中都可**直接访问字段**（无需 `.value`）：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.path                     // /pages/about/about
route.fullPath                 // /pages/about/about?id=1
route.query.get('id')          // "1"（Map API）
route.params.get('from')       // 参数（Map API）
route.meta.title               // 元信息
route.name                     // 命名路由名；未命名时为 null
```

**它何时更新？**
- 一次导航记账完成后（`afterEach` 触发时）
- 页面 `onShow` 时 `syncRoute()` 从页面栈重建成当前路由（**响应非路由器导航：物理返回键、TabBar 切换**）

> 因此**在非导航、非 onShow 的时机读到旧值很正常**；页面级数据请以本页 `onLoad` / `onShow` 为准。

```vue
<template>
	<text>当前: {{ route.path }}</text>  <!-- 模板自动解包 -->
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
const link = useLink({ to: '/pages/about/about', replace: false })

link.href             // ComputedRef<string> 完整路径（脚本中用 link.href.value）
link.isActive         // ComputedRef<boolean> 是否激活（路径前缀匹配）
link.isExactActive    // ComputedRef<boolean> 是否精确激活
await link.navigate() // 执行导航
```

> `isActive` 等返回的是 `ComputedRef`，**脚本中需 `.value`**，模板中自动解包。

## 组件内守卫

| 函数 | 触发时机 |
| --- | --- |
| `onBeforeRouteLeave` | 离开当前页面时（**最常用**） |
| `onBeforeRouteUpdate` | 更新时（静态页模型下极少触发） |
| `onBeforeRouteEnter` | 进入时（效果有限） |

> uni-app x 每次导航都新建页面实例（无 keep-alive 复用），故 `onBeforeRouteEnter/Update` 受限，`onBeforeRouteLeave` 最实用。详见[路由守卫](./guards)。

## 与页面生命周期的配合

要在 `onLoad` / `onShow` 里拿到真实路由，可先手动同步：

```ts
onLoad(options => {
	router.syncRoute()          // 从页面栈初始化 currentRoute
	const route = useRoute()
	// 此时 route.query 与启动 options 对齐
})

onShow(() => {
	router.syncRoute()          // 再次同步，覆盖物理返回 / TabBar 切换
})
```

> `app.use(router)` 已自动注入全局 mixin 在 `onShow` 调 `syncRoute()`；手动调用用于"提前读"的场景。

## 相关

- [useRoute API](../api/use-route) | [useRouter API](../api/use-router) | [useLink API](../api/use-link)