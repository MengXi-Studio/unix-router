# 路由守卫

路由守卫用于在导航过程中控制、校验、重定向。语义对齐 vue-router 4。

## 全局守卫

| 守卫 | 执行时机 |
| --- | --- |
| `router.beforeEach` | 导航触发前，对所有导航生效 |
| `router.beforeResolve` | 所有前置守卫与独享守卫执行完成后 |
| `router.afterEach` | 导航完成后（不阻断导航） |

### 返回值语义

| 返回 | 行为 |
| --- | --- |
| `true` / `undefined` / `null` | 放行 |
| `false` | 中止导航（`NAVIGATION_ABORTED`） |
| `Error` | 取消导航（`NAVIGATION_CANCELLED`） |
| 字符串 / 位置对象 | 重定向到目标 |
| `Promise` | 支持异步守卫 |

```ts
router.beforeEach((to, from) => {
	// 未登录访问需登录页 → 重定向到登录
	if (to.meta.requireAuth && !isLoggedIn()) {
		return { name: 'login', query: new Map([['redirect', to.fullPath]]) }
	}
	return true
})

router.afterEach((to, from) => {
	console.log(`导航: ${from.fullPath} -> ${to.fullPath}`)
})
```

注销守卫：`beforeEach` 等返回一个**取消注册函数**。

```ts
const removeGuard = router.beforeEach(guard)
removeGuard() // 移除
```

## 路由独享守卫 beforeEnter

定义在 `RouteConfig.beforeEnter`，仅对该路由生效：

```ts
{ path: 'pages/guards/guards', name: 'guards', beforeEnter: (to, from) => { ... } }
```

## 组件内守卫

通过组合式 API 在页面 `setup` 内注册：

```ts
import { onBeforeRouteLeave, onBeforeRouteUpdate, onBeforeRouteEnter } from '@meng-xi/unix-router'

// 离开当前页面时触发，返回 false 可阻止离开
onBeforeRouteLeave((to, from) => true)

onBeforeRouteUpdate((to, from) => true)
onBeforeRouteEnter((to, from) => true)
```

> 说明：uni-app x 页面每次导航都会创建**新实例**（无 keep-alive 复用），因此
> `onBeforeRouteUpdate` 极少触发、`onBeforeRouteEnter` 效果有限；`onBeforeRouteLeave` 最常用。

## 守卫重定向与深度保护

守卫返回重定向位置时会递归执行新的导航。为防死循环，重定向深度超过
`MAX_REDIRECT_DEPTH` 时将取消导航（`NAVIGATION_CANCELLED`）。

## guardRoute（冷启动）

用户通过 H5 URL / App 场景值 / deeplink **直接进入**页面时，页面由框架直接加载，
不经守卫。调用 `router.guardRoute()` 可对当前/目标路由补执行守卫链并按结果决定是否重定向：

```uts
// App.uvue onLaunch
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: failure => router.relaunch('/pages/index/index')
	})
})
```

## 导航流程

完整顺序见[导航流程原理](./navigation-flow)。