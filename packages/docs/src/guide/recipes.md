# 完整实战

本章汇集真实业务场景的常见方案，均可直接用于项目。结合 unix-router 特性与 uni-app x 的限制提供最佳实践。

> **先看完整可运行版**：仓库 [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) 是集成了「首页入口 + query/params 传参 + 登录守卫 + useLink + 功能自检」的完整 uni-app x 工程，可直接对照本文各节验证。

## 登录鉴权全流程

未登录用户访问受保护页面时跳转登录页（`NavigationRedirect` 重定向 + `redirect` query 记录来源），登录后返回原页面。

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
	return uni.getStorageSync('token') != null
}

router.beforeEach((to, from) => {
	// 1. 未登录访问受保护页面 → 登录页（replace，避免返回到中间态），query 记录回跳地址
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 2. 已登录访问登录页 → 首页
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})

export default router
```

### 登录页返回来源页

```ts
async function onLoginSuccess(): Promise<void> {
	const redirect = route.query.get('redirect')
	if (redirect != null) {
		await router.replace(redirect) // 回原页，登录页不留栈
	} else {
		await router.relaunch({ name: 'home' })
	}
}
```

冷启动直接落在受保护页时，守卫未执行，见下文[冷启动守卫](#冷启动守卫)。

## TabBar 应用

`meta.isTab: true` 的页面导航时自动改用 `uni.switchTab`（`push` / `replace` / `relaunch` 均如此）。注意 **`switchTab` 不支持携带 query**，跳转 tabBar 页时 query 会被丢弃。

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true, title: '首页' } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true, title: '我的' } }
]

await router.push({ name: 'home' }) // 自动 switchTab
```

需要向 tabBar 页传数据时，**不能用 query，也不能用 params**（params 依赖的 `__params__` 内部 key 同样经 query 桥接，会被 switchTab 丢弃）。请使用全局状态或 storage：

```ts
// 使用全局状态（如模块级响应式变量 / 状态管理）
sharedState.mineEntry = 'settings'
await router.push({ name: 'mine' })
```

或使用 storage（适合大块数据 / 跨会话）：

```ts
// 跳转前写入
uni.setStorageSync('mine_entry', 'settings')
await router.push({ name: 'mine' })

// tabBar 页 onShow 中读取后清理
uni.removeStorageSync('mine_entry')
```

## 详情页参数传递

详情页等「打开即要 id」的场景，用 [ParamsPlugin](./params) 传参比拼 URL query 更直接（值不出现在 URL 上）：

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [new ParamsPlugin()] })

// 列表页
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})
```

```vue
<!-- pages/detail/detail.uvue -->
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
const id = route.params.get('id') // '1024'
</script>

<template>
	<view class="page">
		<text>详情 ID：{{ id }}</text>
	</view>
</template>
```

::: warning
`params` 值须为字符串且可 JSON 序列化。未注册 `ParamsPlugin` 却使用 `params` 会抛 `PLUGIN_REQUIRED`。
:::

## 页面间通信回传

「列表页 → 编辑页 → 保存后回传结果」用 [EventsPlugin](./events)：打开方注册 `events` 监听表，被打开页经 `useOpenerEventChannel()` 回传。

```ts
import { createRouter, EventsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [new EventsPlugin()] })

// 打开方（列表页）：注册监听表
await router.push({
	path: 'pages/edit/edit',
	events: new Map<string, (data: any) => any>([
		['saved', (data: any) => {
			console.log('编辑页回传：', data)
			refreshList()
		}]
	])
})
```

```vue
<!-- pages/edit/edit.uvue -->
<script setup lang="uts">
import { onUnload } from '@dcloudio/uni-app'
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel() // EventChannel | null

function save(): void {
	if (channel != null) {
		channel.emit('saved', { title: '新标题' }) // 回传给打开方
	}
}

onUnload(() => {
	if (channel != null) {
		channel.off('saved') // 可选：按 id 移除监听
	}
})
</script>
```

`useOpenerEventChannel()` 在页面 `onShow` 内即可调用并回传数据，不依赖路由状态同步时机。

## 导航动画

用 [AnimationPlugin](./animation) 配置全局默认动画，单次导航可覆盖：

```ts
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()],
	animation: { type: 'slide-in-right', duration: 300 } // 全局默认
})

// 单次覆盖：本次导航使用 fade-in
await router.push({
	path: 'pages/detail/detail',
	animationType: 'fade-in',
	animationDuration: 500
})
```

- App / 小程序端透传原生 `animationType`；H5 端由插件以 Web Animations API 实现（返回会先播退出动画再真正 `navigateBack`）。
- `back()` 使用全局默认动画作为退出动画（back 无位置参数，单次覆盖仅对前向导航有效）。
- `switchTab` 无动画。

## 404 兜底

`strict: true`（默认）下，命名路由未匹配立即抛 `RouterError ROUTE_NOT_FOUND`，配合 `onError` 与 `guardRoute` 的 `onAbort` 兜底回首页：

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

const router = createRouter({ routes, strict: true })

// 受控导航中：命名路由不存在 / 解析失败
router.onError((error, to, from) => {
	if (isNavigationFailure(error, RouterErrorCode.ROUTE_NOT_FOUND)) {
		router.relaunch({ name: 'home' })
	}
})

// 冷启动：页面已加载但守卫判定不可达（如 requireAuth 未登录、目标无效）
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' })
		}
	}).catch(() => {})
})
```

## 冷启动守卫

H5 直达 URL、App deeplink / scheme 唤起时，页面已加载但**守卫链从未执行**。`guardRoute()` 只补跑 `beforeEach` 守卫链、不执行实际导航：

```ts
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			// 页面已渲染无法真正阻止，跳转登录页或安全页
			router.relaunch({ name: 'login' })
		}
	}).catch(() => {})
})
```

- 守卫**放行**：返回目标位置，无需处理。
- 守卫**中止**：触发 `onAbort`（并 reject），页面已加载无法阻止，在此跳转安全页。
- 守卫**重定向**：按重定向模式真实导航，缺省 `relaunch`。

## 角色权限控制

通过扩展 `RouteMeta` 与守卫实现基于角色的访问控制。

```ts
// types/router.d.ts（仅对 TS / 编辑器生效）
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
	if (roles != null && !hasRole(roles)) {
		uni.showToast({ title: '无权访问', icon: 'none' })
		return { name: 'home' }
	}
	return true
})
```

> ⚠️ **UTS 限制**：上述 `declare module` 增强仅对 TS/编辑器补全有效；uni-app x 原生端不支持接口声明合并。若字段需在 App 原生编译期可用，请直接在类型声明处扩展，见[路由元信息](./meta#自定义-meta-字段)。

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
	return true
})
</script>
```

::: tip 平台限制
组件内离开守卫在受控导航（`router.back` / `push` 等）下生效。小程序原生返回（顶部箭头 / 滑动）由宿主控制，无法同步拦截，需用 `onRouteChange` + `syncRoute` 事后处理。详见[平台兼容性](./compatibility)。
:::

## 数据预取

在导航前预取数据，使用 `router.beforeResolve`。

```ts
const preloaders = new Map<string, (to: RouteLocation) => Promise<void>>()
preloaders.set('detail', async (to) => {
	await fetchDetail(to.query.get('id'))
})

router.beforeResolve(async (to, from) => {
	if (to.name == null) {
		return true
	}
	const loader = preloaders.get(to.name)
	if (loader != null) {
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
	return true
})
```

## 页面标题自动设置

用 `afterEach` 统一设置导航栏标题。

```ts
router.afterEach((to, from, failure) => {
	if (failure != null) {
		return
	}
	const title = to.meta.title
	uni.setNavigationBarTitle({ title: title != null ? title : '默认标题' })
})
```

## 页面栈深度管理

防止小程序页面栈溢出（上限约 10 层），封装安全导航。

```ts
const STACK_WARNING_THRESHOLD = 8

async function safePush(location: RouteLocationRaw): Promise<void> {
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

`afterEach` 只在受控导航完成后触发；`onRouteChange` 覆盖所有路由变化（导航完成 + 状态同步，如物理返回、tab 切换），两者结合可构建完整埋点。

```ts
// 受控导航埋点
router.afterEach((to, from) => {
	analytics.report('page_view', to.path, from.path)
})

// 所有路由变化（含物理返回、tab 切换等状态同步）
router.onRouteChange((to, from) => {
	analytics.report('route_change', to.path, from.path)
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

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	...userRoutes,
	...orderRoutes
]
```

## 下一步

- [导航流程原理](./navigation-flow) — 理解守卫与插件在时序中的位置
- [插件系统](./plugins) — 四个内置插件总览
- [常见问题](./faq) — 高频问题与排查
- [API 参考](../api/create-router) — 完整 API 文档
