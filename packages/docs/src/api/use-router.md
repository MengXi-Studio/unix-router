# useRouter()

`useRouter(): Router` 返回当前上下文中的 [Router 实例](./router-instance)，是组合式 API 中访问路由器的标准方式。

```ts
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()
```

## 实例获取顺序

`useRouter()` 按以下顺序解析路由器实例：

1. **setup 上下文内 inject 优先**：处于组件 `setup`（或 `script setup`）时，优先读取 `app.use(router)` 在 H5 端 `provide` 的路由器。多实例场景下，组件拿到的是**离它最近的已安装实例**。
2. **非 setup 上下文回退全局活跃路由器**：在选项式 `methods`、事件回调等非 setup 上下文中（此时 `inject` 不可用），回退到全局注册的活跃路由器（最近一次 `app.use(router)` 注册的实例）。
3. **均无则抛错**：既未注入也未安装时抛出——`未找到路由器实例，请先 app.use(router)`。

## `app.use(router)` 的作用

`router.install(app)` 由 `app.use(router)` 触发：

- **H5（Web）端**：注册 `provide`（供 `useRouter` setup 内注入）、挂载 `$router` / `$route` 全局属性、注册 `onShow` 全局 mixin（自动调用 `router.syncRoute()`）。
- **原生端（App / 小程序）**：注册全局活跃路由器，供 `useRouter()` 在非 setup 上下文回退使用；建议在页面 `onShow` 手动调用 `router.syncRoute()`。

两端均会触发插件的 app 级 hook（`onAppInstall`）。

## 在组件中使用

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = (): void => {
	router.push({ name: 'about', query: new Map<string, string>([['from', 'home']]) })
}
</script>

<template>
	<view class="page" @click="goAbout">
		<text>前往关于页</text>
	</view>
</template>
```

## 注意事项

- 依赖 `app.use(router)` 已安装路由器，否则抛出错误（未安装时不存在可用实例）。
- 与全局导出的 `router` 是**同一实例**（单实例场景）；多实例场景下 setup 内以 inject 结果为准。

## 相关 API

- [Router 实例](./router-instance)
- [useRoute()](./use-route)
- [useLink()](./use-link)
