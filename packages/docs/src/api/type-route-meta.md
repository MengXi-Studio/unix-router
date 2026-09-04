# RouteMeta

路由元信息（`RouteConfig.meta` 的类型）。内置常用字段，支持通过模块增强扩展自定义 meta。

```ts
interface RouteMeta {
	title?: string      // 页面标题
	isTab?: boolean     // 是否为 tabBar 页面（决定使用 switchTab 导航）
	requireAuth?: boolean // 是否要求登录（可由守卫据此拦截）
}
```

## 扩展自定义 meta

```ts
// env.d.ts / router.d.ts
import '@meng-xi/unix-router'

declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		roles?: string[]
		keepAlive?: boolean
	}
}
```

扩展后即可在配置与守卫中凭类型提示使用：

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/admin/admin', name: 'admin', meta: { roles: ['admin'], title: '管理后台' } }
]

router.beforeEach((to, from) => {
	const roles = to.meta.roles
	if (roles && !hasRole(roles)) return { name: 'home' }
})
```

## 相关 API

- [RouteConfig](./type-route-config)
- [路由元信息指南](../guide/meta)