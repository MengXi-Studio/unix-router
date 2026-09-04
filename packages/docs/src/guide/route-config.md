# 路由配置

unix-router 基于 uni-app x 的**静态页面模型**：每条路由对应 `pages.json` 中注册的一个页面，路由路径即页面路径。

## RouteConfig

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `path` | `string` | 页面路径，须与 `pages.json` 注册一致，如 `pages/index/index` |
| `name` | `string?` | 命名路由，用于按名导航 |
| `meta` | `RouteMeta?` | 路由元信息（title / isTab / requireAuth 等） |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | 路由独享前置守卫 |
| `redirect` | `RouteLocationRaw?` | 重定向目标 |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{
		path: 'pages/index/index',
		name: 'home',
		meta: { title: '首页', isTab: true }
	},
	{
		path: 'pages/guards/guards',
		name: 'guards',
		meta: { title: '守卫', requireAuth: true },
		beforeEnter: (to, from) => {
			return isLoggedIn() ? true : { name: 'login' }
		}
	},
	// 重定向：访问此路径时直接跳到目标
	{ path: 'pages/old/old', redirect: 'pages/index/index' }
]
```

## 路径规范

路径会自动规范化：补前导斜杠、去尾部斜杠。

- `pages/index/index` → `/pages/index/index`
- `'/pages/about/about/'` → `/pages/about/about`

## 严格模式

`createRouter({ strict: true })` 时，解析未知**命名路由**会抛出 `ROUTE_NOT_FOUND` 错误；非严格模式下会警告并按路径处理。

## 命名路由类型提示（可选）

通过模块增强 `RouteNameMap` 可让 `name` 获得类型提示：

```ts
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: void
		about: void
	}
}
```

## 不支持的能力

由于静态页面模型，以下能力**不支持**（详见[与 vue-router 的差异](./differences)）：

- 动态路由 `addRoute` / `removeRoute`
- 嵌套路由 `children`
- 命名视图 / `RouterView`
- `scrollBehavior`