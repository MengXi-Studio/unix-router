# useRoute()

`useRoute(): RouteLocation` 返回当前 [RouteLocation](./type-route-location)。必须在组件 `setup`（或 `script setup`）中调用。

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
```

## 返回值

返回**全局 reactive 对象**，直接访问字段即可——**没有 `.value`**。

::: warning 与 vue-router 的差异
vue-router 中 `route` 是一个 ref 风格对象，模板中可用 `route.path`、脚本中访问原始值需借助 `computed` 等方式。本库的 `useRoute()` 返回的就是全局 reactive 的 `RouteLocation`，脚本与模板中**都直接 `route.path` 访问**，无需 `.value`，也不需要解包。
:::

## 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `path` | `string` | 页面路径（带前导斜杠，如 `/pages/index/index`） |
| `name` | `string \| null` | 命名路由名，未命名时为 `null` |
| `meta` | `RouteMeta` | 路由元信息（`title` / `isTab` / `requireAuth`） |
| `query` | `Map<string, string>` | 查询参数，读取用 `.get(key)` |
| `params` | `Map<string, string>` | 路由参数（须注册 `ParamsPlugin`） |
| `fullPath` | `string` | 完整路径（`path` + 序列化 query） |
| `hash` | `string` | 恒为 `''`（uni-app x 不支持 hash，保留字段） |
| `matched` | `RouteConfig[]` | 匹配到的路由记录（扁平模型下为单个 record 数组） |

## 读取路由信息

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()

// 直接访问字段（无 .value）
console.log(route.path)            // /pages/detail/detail
console.log(route.fullPath)        // /pages/detail/detail?id=1
console.log(route.query.get('id')) // '1'
console.log(route.name)            // 'detail'（未命名时为 null）
</script>

<template>
	<view class="page">
		<text>当前页：{{ route.path }}</text>
	</view>
</template>
```

::: tip query 与 params 均为 Map
uni-app x 中 query 以字符串在 URL 传递，`route.query` / `route.params` 均为 `Map<string, string>`，读取用 `.get(...)`，判断用 `.has(...)`。
:::

## 更新时机

- **导航完成时**：uni API 调用成功且目标页经页面栈顶确认后自动更新（剥离插件内部 key 后写入）。
- **仅前进导航写入本对象**：`back()` 与 `syncRoute()` 只更新路由器内部状态（`router.currentRoute`），**不回写本对象**。物理返回、tab 切换等不经过路由器的行为，请通过页面 `onLoad(options)` / `onShow` 直接读取最新状态（H5 端 `app.use(router)` 注册的 `onShow` mixin 会自动调 `syncRoute()`，但同步的是 `router.currentRoute`）。

## onShow 时机提示

页面 `onShow` 可能**早于路由状态同步**执行。若需要在 `onShow` 里确定性地读取本次页面参数，在页面 `onLoad(options)` 中读取原生 `options`（即 URL query）；注意 `router.syncRoute()` 不会更新 `useRoute()` 返回的对象，不要依赖它刷新 `route`。

## 相关 API

- [RouteLocation](./type-route-location)
- [useRouter()](./use-router)
- [Router 实例 - currentRoute](./router-instance#currentroute)
