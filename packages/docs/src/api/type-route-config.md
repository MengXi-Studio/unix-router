# RouteConfig

单个路由配置记录。由于 uni-app x 采用静态 `pages.json` 页面模型，**页面路径即路由路径**，`path` 必须与 `pages.json` 注册一致。

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于', requireAuth: true } },
	{
		path: 'pages/admin/admin',
		name: 'admin',
		meta: { title: '管理后台', requireAuth: true },
		beforeEnter: (to, from) => {
			return isAdmin() ? true : { name: 'home' }
		}
	}
]
```

## 字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `path` | `string` | — | **必需**。页面路径，须与 `pages.json` 一致（如 `pages/index/index`） |
| `name` | `string` | — | 命名路由名，用于按名导航 |
| `redirect` | `RouteLocationRaw` | — | 重定向目标（未提供时对本路径直接重定向） |
| `meta` | `RouteMeta` | — | 路由元信息 |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]` | — | 路由独享前置守卫 |

## 相关 API

- [createRouter()](./create-router)
- [RouteMeta](./type-route-meta)
- [NavigationGuard](./type-navigation-guard)