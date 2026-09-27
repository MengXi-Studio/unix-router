# 路由导航

unix-router 提供 `push / replace / relaunch / back` 四种导航，底层映射到 uni 原生导航 API，并自动识别 tabBar 页面。每次导航返回 `Promise<NavigationResult>`：成功 **resolve 目标路由位置**，失败 **reject `NavigationFailure`**。

## 导航方式

| 方式 | 对应 uni API | 说明 |
| --- | --- | --- |
| `push` | `navigateTo` / `switchTab` | 入栈跳转，可返回 |
| `replace` | `redirectTo` / `switchTab` | 替换当前页，不新增栈 |
| `relaunch` | `reLaunch` / `switchTab` | 关闭所有页并打开目标 |
| `back` | `navigateBack` | 返回上一页/多级 |

## 位置形式（RouteLocationRaw）

既支持字符串，也支持对象：

```ts
// 1. 字符串路径（可内联 ?query）
await router.push('/pages/detail/detail?id=1024')

// 2. 路径对象（path 优先于 name）
await router.push({ path: 'pages/detail/detail', query: new Map<string, string>([['id', '1024']]) })

// 3. 命名对象（推荐：路径变更只改配置一处）
await router.push({ name: 'detail', query: new Map<string, string>([['id', '1024']]) })
```

::: tip query 与 params 都是 Map
uni-app x 的 query / params 以 `Map<string, string>` 承载，读取用 `.get(key)` / `.has(key)`。
:::

## 传参：query 与 params 怎么选

- **query**：出现在 URL 中、可刷新可收藏，适合短小的 id、来源标记等。任何导航方式直接可用。
- **params**：不进 URL，经 `ParamsPlugin` 关联存储跨页传递，适合结构化数据、不希望暴露在地址栏的内容。

两种方式的完整对比与用法见[参数传递](./params)。

## tabBar 页面

目标路由 `meta.isTab === true` 时，`push` / `replace` / `relaunch` 会自动改用 `uni.switchTab`：

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true } }
]

await router.push({ name: 'mine' }) // 自动走 switchTab
```

::: warning switchTab 不携带 query
uni 的 `switchTab` 不支持 query 参数，路由器也不会为它拼 URL 查询串。因此 tabBar 页**既无法用 query、也无法用 params 传参**（params 依赖的内部通道同样经 query 桥接），请改用全局状态或 storage（见[实战指南](./recipes#tabbar-应用)）。
:::

## 返回 back

```ts
await router.back()   // delta 默认 1，返回上一页
await router.back(2)  // 返回两级
```

行为细节：

- `delta` 默认 `1`；必须为**正整数**，否则不发起导航、直接 reject `ABORTED`
- 页面栈不足（少于两级，或 `delta` 超出栈深）→ reject `CANCELLED`
- 返回目标按**页面栈**解析（栈中倒数第 `delta` 个页面），再按路径匹配回路由记录
- 返回前的守卫阶段只执行全局 `beforeEach` + `beforeResolve`（路由独享 `beforeEnter` 不参与）；`afterEach` 在返回成功后照常触发

## 重复导航检测

仅 `push` 检测：目标位置与当前路由的 `path` / `query` / `params` / `hash` 完全一致时，reject `DUPLICATED`。`replace` / `relaunch` 语义即"强制到达"，不做检测。

```ts
try {
	await router.push({ name: 'about' })
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) return // 忽略重复导航
	throw e
}
```

## 并发导航排队

上一导航未完成时，新的导航会**自动等待其完成后再执行**（内部串行排队），无需自行加锁或防抖。

## 导航失败处理

导航失败时 Promise reject，可按错误码分流处理：

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

const goDetail = async () => {
	try {
		await router.push({ name: 'detail' })
	} catch (e) {
		if (isNavigationFailure(e, RouterErrorCode.ABORTED)) return     // 被守卫拦截，属正常业务
		if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) return  // 重复导航，忽略
		// 其余：ROUTE_NOT_FOUND / NAVIGATION_API_ERROR / PLUGIN_REQUIRED ...
		uni.showToast({ title: '跳转失败', icon: 'none' })
	}
}
```

也可用 `router.onError` 全局兜底，见[错误处理](./error-handling)。

## 单次导航动画

注册 `AnimationPlugin` 后可用全局默认动画；单次导航可在位置对象上覆盖：

```ts
await router.push({
	name: 'detail',
	animationType: 'slide-in-right',
	animationDuration: 300
})
```

详见[导航动画](./animation)。

## 程序化导航与声明式

除了调用 `router.push`，还可以：

```ts
// 程序化（任意上下文）
router.replace('/pages/login/login')

// 声明式（组件）
import { RouterLink } from '@meng-xi/unix-router'
// <RouterLink to="pages/about/about">关于</RouterLink>
```

`RouterLink` 的 props 仅 `to` / `replace` / `relaunch`，见 [RouterLink API](../api/router-link)。

## 冷启动直接进入

`uni-app x` 冷启动/直接 URL 进入页面**不经过守卫链**（页面由 pages.json 直接加载）。如需补跑守卫，用 `guardRoute`：

```ts
// App.vue onLaunch
router.isReady().then(() => {
	router.guardRoute(`/${options?.path ?? ''}`, {
		onAbort: (failure) => router.relaunch({ name: 'login' })
	})
})
```

## 下一步

- [参数传递](./params) — query / params 选型与 ParamsPlugin
- [组合式 API](./composables) — useRouter / useRoute / useLink
- [Router 实例](../api/router-instance) — 导航方法完整签名
