# RouteMeta

路由元信息（`RouteConfig.meta` 的类型，UTS `type`）。内置三个常用字段：

```ts
type RouteMeta = {
	title?: string        // 页面标题
	isTab?: boolean       // 是否为 tabBar 页面（决定使用 switchTab 导航）
	requireAuth?: boolean // 是否要求登录（可由守卫据此拦截）
}
```

## 用法示例

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/home/home', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: '我的', requireAuth: true } }
]
```

守卫中读取目标页 meta 决策：

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) }
	}
	return true
})
```

## 自定义 meta 字段：直接修改 type 定义

::: warning 与 vue-router 的重要差异
vue-router 中自定义 meta 靠 `declare module { interface RouteMeta {...} }` **接口声明合并**实现。但 **UTS 不支持接口声明合并**，该写法在原生端（Kotlin/Swift）无效。自定义字段必须**直接修改 `RouteMeta` 的 type 定义**。
:::

在本库源码（或你 fork / 复制的 `route.uts` 类型文件）中直接追加字段：

```ts
// RouteMeta 类型定义处（types/route.uts）
export type RouteMeta = {
	title?: string
	isTab?: boolean
	requireAuth?: boolean
	// ↓ 按需追加你的自定义字段
	icon?: string
	tag?: string
}
```

追加后即可在配置与守卫中获得完整类型提示：

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/new/new', name: 'new', meta: { title: '新品', tag: 'new' } }
]
```

> 可选字段读取时用 `=== true` / `=== undefined` 显式判断（UTS 强类型，无 truthy 隐式转换）。更多说明见[路由元信息指南](../guide/meta)。

## 相关 API

- [RouteConfig](./type-route-config)
- [路由元信息指南](../guide/meta)
