# useRoute()

`useRoute()` 返回**响应式**的当前 [RouteLocation](./type-route-location)（基于 `router.currentRoute` 派生）。必须在组件 `setup` 中调用。

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
```

## 返回值

返回响应式 `RouteLocation`，其属性变化会触发依赖它的渲染 / 计算更新。

## 读取路由信息

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()

// 响应式读取
console.log(route.path)      // /pages/detail/detail
console.log(route.fullPath)  // /pages/detail/detail?id=1
console.log(route.query.get('id')) // '1'
console.log(route.name)
console.log(route.meta)
</script>

<template>
	<text>当前页：{{ route.path }}</text>
</template>
```

::: tip query 与 params 均为 Map
uni-app x 中 query 以字符串在 URL 传递，`RouteLocation.query` / `params` 均为 `Map<string, string>`，读取用 `.get(...)`，判断用 `.has(...)`。
:::

## 注意事项

- 仅可在组件 `setup`（或 `script setup`）中调用
- 依赖路由器已安装，否则抛出 `SETUP_ERROR`
- 在页面 `onLoad` 之前若需读取路由，可先手动调用一次 `router.syncRoute()`

## 相关 API

- [RouteLocation](./type-route-location)
- [useRouter()](./use-router)
- [Router 实例 - currentRoute](./router-instance#currentroute)