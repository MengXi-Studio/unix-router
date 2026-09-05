# 路由守卫

守卫用于在导航过程中**控制、校验、重定向**，是鉴权与埋点的核心。语义对齐 vue-router 4。本篇以「登录鉴权」为主线讲透。

## 一次导航的守卫顺序

```
beforeEach → beforeEnter(路由独享) → beforeResolve → 执行导航 → afterEach
```

一个守卫返回 `false` / `Error` / 重定向位置都会中止改道。任一守卫即可阻断整条链路。

## 三种全局 + 一种独享 + 一种组件内

| 类型 | 注册 | 执行时机 |
| --- | --- | --- |
| 全局前置 | `router.beforeEach` | 导航触发前，所有导航生效 |
| 全局解析 | `router.beforeResolve` | 所有前置/独享守卫之后、真正导航前 |
| 全局后置 | `router.afterEach` | 导航完成后（不阻断） |
| 路由独享 | `RouteConfig.beforeEnter` | 仅进入该路由时 |
| 组件内 | `onBeforeRouteLeave` 等 | 离开/进入当前组件时 |

## 返回值语义

| 返回 | 行为 |
| --- | --- |
| `true` / `null` | 放行（推荐显式 `return null` 或 `true`） |
| `false` | 中止导航（`ABORTED`） |
| `Error` | 取消导航（`CANCELLED`） |
| 字符串 / 位置对象 | 重定向到目标 |
| `Promise` | 支持异步守卫（`async`） |

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login' }        // 重定向
	}
	return true                          // 放行
})

router.afterEach((to, from, failure) => {
	console.log(`导航: ${from.fullPath} -> ${to.fullPath}`, failure?.message ?? '')
})
```

注销守卫：注册函数返回**取消函数**：

```ts
const off = router.beforeEach(g)
off() // 移除
```

## 实战：完整登录鉴权流程

**目标**：未登录访问 `requireAuth` 页面 → 拦截 → 登录页 → 登录后回跳。

**1) 路由配置**（`router.config.ts`）：

```ts
export const routes: RouteConfig[] = [
	{ path: 'pages/login/login', name: 'login', meta: { title: '登录' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: '我的', requireAuth: true } }
]
```

**2) 全局前置守卫**（`router.ts`）：

```ts
export const router = createRouter({ routes, strict: true })

router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		// 记录来源 fullPath，登录后可回跳
		return {
			name: 'login',
			query: new Map([['redirect', to.fullPath]])
		}
	}
	return true
})
```

**3) 登录页**（`pages/login/login.uvue`），登录成功后回跳：

```ts
import { useRouter, useRoute } from '@meng-xi/unix-router'
const router = useRouter()
const route = useRoute()

function loginOk() {
	setLoggedIn(true)
	const target = route.query.get('redirect') ?? '/pages/index/index'
	router.replace(target) // replace：登录页不留在栈中
}
```

> 关键点：守卫里用 `return { name: 'login' }` 而非 `router.push` —— 见下方「坑」。

## 路由独享 beforeEnter

定义在 `RouteConfig`，仅对**本路由**生效：

```ts
{
	path: 'pages/admin/admin',
	name: 'admin',
	meta: { requireAdmin: true },
	beforeEnter: (to, from) => (isAdmin() ? true : { name: 'login' })
}
```

## 组件内守卫（onBeforeRouteLeave）

在页面 `setup` 中注册，**返回 `false` 真正阻止离开**（如未保存离开确认）：

```ts
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
	if (hasUnsavedChanges) {
		return false // 中止离开
	}
	return true
})
```

> uni-app x 页面每次导航新建实例（无 keep-alive），因此 `onBeforeRouteUpdate` 极少触发、`onBeforeRouteEnter` 效果有限；**`onBeforeRouteLeave` 最实用**。

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

## 守卫超时与重定向深度

- `guardTimeout`（默认 `10000ms`）：守卫超时则告警并中止导航，`0` 关闭。
- **重定向深度上限**：守卫返回重定向会递归导航；超过 `MAX_REDIRECT_DEPTH` 时取消（`CANCELLED`），防死循环。

## back 也走守卫

`router.back(delta)` 会执行 `beforeEach → beforeResolve`，因此想在"返回时"拦截，可在这些守卫里判断 `to.path`（返回目标）或在 `onBeforeRouteLeave` 里拦截。

## 常见坑

1. **守卫里调 `router.push` 死锁**：应 `return 重定向位置`，不要 `router.push`。
2. **漏写放行分支**：守卫没在任何分支 `return true/null` → 后续导航状态不明。每个分支显式返回。
3. **异步守卫没 await**：用 `async` 守卫；`Promise` 会被解析后再决策。
4. **误用 truthy 判断**：`if (to.meta.requireAuth)` 读可选字段应 `=== true`。
5. **直接用 `uni.navigateTo` 绕过守卫**：unix-router **不拦截原生导航 API**，请统一走 `router.*`。

## 相关

- 执行时序细节：见[导航流程原理](./navigation-flow)
- 平台对返回拦截的差异：见[平台兼容性](./compatibility#返回拦截)