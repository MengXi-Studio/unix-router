# 实战指南

本章汇集真实业务场景的常见方案，均可直接用于项目。结合 unix-router 特性与 uni-app x 的限制提供最佳实践。

## 登录认证

未登录用户访问受保护页面时跳转登录页，登录后返回原页面。

### 路由与守卫

```ts
// router/routes.ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true, title: '首页' } },
	{ path: 'pages/login/login', name: 'login', meta: { title: '登录' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { requireAuth: true, title: '个人中心' } }
]
```

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes'

const router = createRouter({ routes, strict: true })

function isLoggedIn(): boolean {
	return !!uni.getStorageSync('token')
}

router.beforeEach((to, from) => {
	// 1. 未登录访问受保护页面 → 登录页（replace，避免返回到中间态）
	if (to.meta.requireAuth && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 2. 已登录访问登录页 → 首页
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
})

export default router
```

### 登录页返回原页面

```ts
onLoginSuccess(async () => {
	const redirect = route.query.get('redirect')
	if (redirect) {
		await router.replace(redirect) // 回原页，登录页不留栈
	} else {
		await router.relaunch({ name: 'home' })
	}
})
```

## 角色权限控制

通过扩展 `RouteMeta` 与守卫实现基于角色的访问控制。

```ts
// types/router.d.ts
import '@meng-xi/unix-router'

declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		roles?: string[]
	}
}
```

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/admin/admin', name: 'admin', meta: { roles: ['admin'], title: '管理后台' } }
]

router.beforeEach((to, from) => {
	const roles = to.meta.roles
	if (roles && !hasRole(roles)) {
		uni.showToast({ title: '无权访问', icon: 'none' })
		return { name: 'home' }
	}
})
```

## 表单离开确认

防止用户误操作离开未保存的表单，通过组件内离开守卫 `onBeforeRouteLeave` 实现。

```vue
<script setup lang="uts">
import { ref } from 'vue'
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

const dirty = ref(false)

onBeforeRouteLeave((to, from) => {
	if (dirty.value) {
		// 返回一个 Promise，resolve(true) 放行 / resolve(false) 阻止
		return new Promise<boolean>((resolve) => {
			uni.showModal({
				title: '提示',
				content: '有未保存的修改，确认离开？',
				success: (res) => resolve(res.confirm)
			})
		})
	}
})
</script>
```

::: tip 平台限制
组件内离开守卫在受控导航（`router.back` / `push` 等）下生效。小程序原生返回（顶部箭头 / 滑动）由宿主控制，无法同步拦截，需用 `onRouteChange` + `syncRoute` 事后处理。详见[平台兼容性](./compatibility)。
:::

## 数据预取

在导航前预取数据，使用 `router.beforeResolve`。

```ts
const preloaders: Record<string, (to: RouteLocation) => Promise<void>> = {
	detail: async (to) => {
		const store = useDetailStore()
		await store.fetchDetail(to.query.get('id'))
	}
}

router.beforeResolve(async (to, from) => {
	const loader = preloaders[to.name as string]
	if (loader) {
		uni.showLoading({ title: '加载中...' })
		try {
			await loader(to)
		} catch (err) {
			uni.showToast({ title: '加载失败', icon: 'none' })
			return false // 数据加载失败，中止导航
		} finally {
			uni.hideLoading()
		}
	}
})
```

## 页面标题自动设置

用 `afterEach` 统一设置导航栏标题。

```ts
router.afterEach((to) => {
	const title = to.meta.title as string | undefined
	uni.setNavigationBarTitle({ title: title || '默认标题' })
})
```

## 页面栈深度管理

防止小程序页面栈溢出（上限约 10 层），封装安全导航。

```ts
const STACK_WARNING_THRESHOLD = 8

async function safePush(location: RouteLocationRaw) {
	const pages = getCurrentPages()
	if (pages.length >= STACK_WARNING_THRESHOLD) {
		console.warn('[unix-router] page stack near limit, use relaunch instead')
		await router.relaunch(location)
	} else {
		await router.push(location)
	}
}
```

## 埋点统计

用 `afterEach` 完整导航 + `onRouteChange` 状态同步，构建页面埋点。

```ts
router.afterEach((to, from) => {
	analytics.report('page_view', to.path, from.path, false)
})

router.onRouteChange((to, from) => {
	// 状态同步（物理返回等）可在此区分
	if (to._synced) {
		analytics.report('page_view', to.path, from.path, true)
	}
})
```

## 守卫组装顺序

组装多个守卫时，按以下顺序注册：

1. 维护 / 全局拦截（最先）
2. 登录认证
3. 权限控制
4. 数据预取（`beforeResolve`）
5. 后置处理（`afterEach`）

```ts
setupMaintenanceGuard(router)
setupAuthGuard(router)
setupPermissionGuard(router)
setupPreloadGuard(router)
setupTitleGuard(router)
setupAnalyticsGuard(router)
```

## 路由模块化

大型项目将路由按模块拆分后再合并。

```ts
// modules/user/routes.ts
export const userRoutes: RouteConfig[] = [
	{ path: 'pages/profile/profile', name: 'profile', meta: { requireAuth: true } }
]

// router/routes.ts
import { userRoutes } from '@/modules/user/routes'
import { orderRoutes } from '@/modules/order/routes'

export const routes = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	...userRoutes,
	...orderRoutes
]
```

## 下一步

- [常见问题](./faq) — 高频问题与排查
- [平台兼容性](./compatibility) — 各平台限制
- [API 参考](../api/create-router) — 完整 API 文档