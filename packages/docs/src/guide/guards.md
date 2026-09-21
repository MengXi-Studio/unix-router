# 路由守卫

守卫用于在导航过程中**控制、校验、重定向**，是鉴权与埋点的核心，语义对齐 vue-router 4。

## 守卫链执行顺序

一次导航的守卫按以下顺序执行：

```
beforeEach → beforeEnter(路由独享) → beforeResolve → 执行导航 → afterEach
```

- 任意守卫返回「中止 / 重定向」都会结束当前链路；
- 守卫返回重定向时，会以新目标**重新走完整守卫链**；
- `afterEach(to, from, failure)` 在导航完成后调用：成功时 `failure` 为 `null`，失败时携带 `Error`，不阻断流程。

## 守卫类型总览

| 类型 | 注册方式 | 执行时机 |
| --- | --- | --- |
| 全局前置 | `router.beforeEach` | 导航触发时最先执行 |
| 路由独享 | `RouteConfig.beforeEnter` | 仅进入该路由时 |
| 全局解析 | `router.beforeResolve` | 所有前置 / 独享守卫之后、真正导航前 |
| 全局后置 | `router.afterEach` | 导航完成后（不阻断） |
| 组件内 | `onBeforeRouteLeave` 等 | 离开 / 进入 / 更新当前页面时 |

## 守卫返回值全表

守卫返回值决定导航走向。支持异步：`async` 守卫返回 `Promise` 会被解析后再决策（受 `guardTimeout` 约束）。

| 返回值 | 行为 |
| --- | --- |
| `null` / `true` | 放行，继续后续守卫 |
| `false` | 中止导航（错误码 `ABORTED`） |
| `Error` | 中止导航，该错误作为失败原因（错误码 `CANCELLED`） |
| `string` | 重定向到该路径 |
| 位置对象（`{ path }` / `{ name }` / `{ name, query }` 等） | 重定向到该位置 |
| `{ location, mode? }` | `NavigationRedirect`：重定向并指定导航方式 |

`NavigationRedirect` 结构：

```ts
{
	location: RouteLocationRaw             // 重定向目标（字符串路径或位置对象）
	mode?: 'push' | 'replace' | 'relaunch' // 导航方式，缺省沿用原导航模式
}
```

::: tip 两类对象如何区分
返回对象携带 `location` 字段 → 按 `NavigationRedirect` 处理（可指定 `mode`）；否则按普通位置对象处理（沿用原导航模式）。
:::

重定向会**重新走完整守卫链**。深度上限为 `10`，超出（例如守卫互相重定向成环）按 `CANCELLED` 取消，防止死循环。

## 全局守卫注册

三个全局守卫均返回**取消函数**，调用即注销：

```ts
const offBefore = router.beforeEach((to, from) => {
	console.log('前置', from.fullPath, '->', to.fullPath)
	return true
})

const offResolve = router.beforeResolve((to, from) => {
	return true
})

const offAfter = router.afterEach((to, from, failure) => {
	// failure: Error | null
	console.log('完成', to.fullPath, failure?.message ?? '')
})

// 注销
offBefore()
offResolve()
offAfter()
```

::: tip beforeEach 与 beforeResolve 的分工
`beforeEach` 在守卫链最前（适合鉴权、埋点）；`beforeResolve` 在路由独享守卫之后、导航真正执行前（适合依赖「目标已最终确定」的逻辑，组件内守卫也基于它过滤实现）。
:::

## 守卫超时 guardTimeout

`createRouter` 的 `guardTimeout` 选项（默认 `10000`ms）约束守卫的执行时长：

- 超时输出警告并**中止导航**（`CANCELLED`）；
- 设为 `0` 关闭超时检测；
- 异步守卫（返回 `Promise`）同样受超时约束。

```ts
const router = createRouter({
	routes,
	guardTimeout: 5000 // 5s
})
```

## 路由独享 beforeEnter

定义在 `RouteConfig` 上，仅对**本路由**生效，可传单函数或数组：

```ts
{
	path: 'pages/admin/admin',
	name: 'admin',
	meta: { title: '管理后台' },
	beforeEnter: (to, from) => {
		return isAdmin() ? true : { name: 'login' }
	}
}
```

## 组件内守卫

在页面 `setup` 中注册，基于全局 `beforeResolve` 过滤实现，可与全局解析守卫协同：

| API | 触发场景 |
| --- | --- |
| `onBeforeRouteLeave` | 从本页面离开（导航到其他页 / back 返回） |
| `onBeforeRouteEnter` | 导航进入本页面路径时 |
| `onBeforeRouteUpdate` | 路径不变、参数变化的「更新」 |

```ts
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
	if (hasUnsavedChanges) {
		return false // 阻止离开
	}
	return true
})
```

::: warning 适用场景有限
uni-app x 页面每次导航都会新建实例（无 keep-alive 复用）：`onBeforeRouteLeave` **最可靠**；`onBeforeRouteEnter` 在首次进入时页面已在创建途中、效果有限；`onBeforeRouteUpdate` 在静态页面模型下极少触发。
:::

## 实战：完整登录鉴权流程

**目标**：未登录访问 `requireAuth` 页面 → 拦截 → 登录页 → 登录后回跳原页面。

**1) 路由配置**（`router/routes.ts`）：

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/login/login', name: 'login', meta: { title: '登录' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: '我的', requireAuth: true } }
]
```

**2) 全局前置守卫**（`router/index.ts`）——用 `NavigationRedirect` 重定向并携带 `redirect` query：

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({ routes, strict: true })

let loggedIn: boolean = false
export function isLoggedIn(): boolean {
	return loggedIn
}
export function setLoggedIn(value: boolean): void {
	loggedIn = value
}

router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace' // replace：登录页不留在返回栈
		}
	}
	return true
})
```

**3) 登录页回跳**（`pages/login/login.uvue`）：

```vue
<script setup lang="uts">
import { useRouter, useRoute } from '@meng-xi/unix-router'
import { setLoggedIn } from '../../router'

const router = useRouter()
const route = useRoute()

const onLogin = () => {
	setLoggedIn(true)
	const redirect = route.query.get('redirect')
	router.replace(redirect !== null ? redirect : '/pages/index/index')
}
</script>

<template>
	<view class="page">
		<text class="title">登录</text>
		<button @click="onLogin">登录并回跳</button>
	</view>
</template>
```

::: danger 守卫里不要调 router.push
守卫中应 `return 重定向位置`，而不是调用 `router.push`——后者会发起新导航并被排队，产生不可预期的导航序列。
:::

## 注册取消与清理

所有守卫注册都写入全局守卫队列，长期运行的应用要注意注销，避免重复注册堆积：

```ts
const off = router.beforeEach(reportNavigation)

// 无需时取消（如退出登录、页面卸载、测试 teardown）
off()
```

## back 也走守卫

`router.back(delta)` 在返回前**只执行 `beforeEach → beforeResolve`**（不执行路由独享 `beforeEnter`），返回成功后 `afterEach` 照常触发。想在「返回时」拦截：在全局守卫里判断 `to.path`（返回目标），或在被离开页面的 `onBeforeRouteLeave` 里拦截。

- `delta` 默认 `1`；非正整数 → `ABORTED`；页面栈不足 → `CANCELLED`。

## guardRoute：冷启动补执行守卫

H5 URL / App 场景值 / deeplink 直接进入页面时，页面已由框架加载，**守卫没机会跑**。在 `App.uvue` 的 `onLaunch` 补跑：

```ts
import { router } from './router'

export function App() {
	onLaunch((options: any) => {
		router.isReady().then(() => {
			const launchPath = options?.path ? `/${options.path}` : undefined
			router.guardRoute(launchPath, {
				onAbort: (failure) => router.relaunch('/pages/index/index')
			})
		})
	})
}
```

- 放行：返回目标路由
- 重定向：自动导航到守卫返回的目标
- 中止：触发 `onAbort`，可跳安全页

## 常见坑

1. **守卫里调 `router.push`**：会产生排队导航、行为不可预期。应 `return 重定向位置`。
2. **漏写放行分支**：每个分支显式 `return true` / `return null`。
3. **异步守卫**：直接用 `async` 函数；`Promise` 会被解析后决策（受 `guardTimeout` 约束）。
4. **误用 truthy 判断**：读可选字段用 `to.meta.requireAuth === true`。
5. **业务代码直调 `uni.navigateTo` 绕过守卫**：默认不拦截；可启用 [uni API 拦截](./interceptor)让原生调用也走守卫链。

## 下一步

- [页面间通信](./events) — 导航完成后的页面间定向通信
- [错误处理](./error-handling) — 中止 / 取消 / 重定向失败的错误体系
- [NavigationGuard 类型](../api/type-navigation-guard) — 守卫返回值类型全解
