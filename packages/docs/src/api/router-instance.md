# Router 实例

`createRouter()` 返回的 `Router` 实例提供导航、守卫与状态查询能力。本文列出全部成员。

## 属性

### `currentRoute`

- 签名：`get currentRoute(): RouteLocation`（**只读**）
- 说明：当前路由位置，响应式对象，[useRoute()](./use-route) 基于它派生。导航完成（页面栈顶确认后）与 `syncRoute()` 状态同步时更新。

```ts
console.log(router.currentRoute.path) // /pages/index/index
```

## 导航方法

导航方法均返回 `Promise<NavigationResult>`（`NavigationResult` 即目标 [RouteLocation](./type-route-location)，成功时 resolve、失败时 reject）。并发导航自动排队；导航失败一律 reject（见[错误处理](../guide/error-handling)）。

### `push(location)`

- 签名：`push(location: RouteLocationRaw): Promise<NavigationResult>`
- 说明：导航到新页面，对应 `uni.navigateTo`；目标为 `meta.isTab` 页面时自动改用 `uni.switchTab`（不携带 query）。`push` 到与当前完全一致的地址会 reject `DUPLICATED`。

```ts
const to = await router.push({ name: 'detail', query: new Map<string, string>([['id', '1']]) })
```

### `replace(location)`

- 签名：`replace(location: RouteLocationRaw): Promise<NavigationResult>`
- 说明：替换当前页面，对应 `uni.redirectTo`；目标为 tabBar 页面时自动改用 `uni.switchTab`。不检测重复导航。

### `relaunch(location)`

- 签名：`relaunch(location: RouteLocationRaw): Promise<NavigationResult>`
- 说明：关闭所有页面并打开目标页面，对应 `uni.reLaunch`；目标为 tabBar 页面时自动改用 `uni.switchTab`。

### `back(delta?)`

- 签名：`back(delta?: number | null): Promise<NavigationResult>`
- 说明：返回上一页或多级页面，对应 `uni.navigateBack`。`delta` 默认 `1`（传 `null` 同样按 `1` 处理）：
  - 非正整数 → reject `ABORTED`；
  - 页面栈不足（栈长 < 2 或 `delta >= 栈长`）→ reject `CANCELLED`。
- 返回前只执行 `beforeEach` + `beforeResolve` 守卫链，不经过插件的 enrich / afterResolve。

```ts
await router.push({ name: 'about' })
await router.back()      // 返回上一页
await router.back(2)     // 返回两级
```

## 守卫注册

三个守卫注册方法均**返回取消注册函数**。返回值语义：`null | true` 放行；`false` → `ABORTED`；`Error` → `CANCELLED`；字符串或对象位置 → 重定向；`{ location, mode? }` → `NavigationRedirect`。

| 方法 | 签名 | 说明 |
| --- | --- | --- |
| `beforeEach(guard)` | `(guard: NavigationGuard) => () => void` | 全局前置守卫，导航排队后最先执行 |
| `beforeResolve(guard)` | `(guard: NavigationGuard) => () => void` | 全局解析守卫，真正调用 uni API 前的最后一道闸；组件内守卫基于它过滤实现 |
| `afterEach(guard)` | `(guard: PostNavigationGuard) => () => void` | 全局后置守卫，签名 `(to, from, failure: Error \| null) => void`，导航完成或失败后触发 |

```ts
const off = router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login' }
	}
	return true
})
// 需要时移除
off()
```

## 状态与工具方法

### `getRoutes()`

- 签名：`getRoutes(): RouteConfig[]`
- 说明：获取所有已注册路由配置（浅拷贝）。

### `hasRoute(name)`

- 签名：`hasRoute(name: string): boolean`
- 说明：检查是否存在指定名称的路由。

### `resolve(location)`

- 签名：`resolve(location: RouteLocationRaw): RouteLocation`
- 说明：解析路由位置为完整 `RouteLocation`，**不执行导航**。解析非法位置（如命名路由不存在）时抛 `RouterError ROUTE_NOT_FOUND`。

```ts
const to = router.resolve({ name: 'detail' })
console.log(to.path) // /pages/detail/detail
```

### `isReady()`

- 签名：`isReady(): Promise<void>`
- 说明：等待路由器初始化完成（`app.use(router)` 即标记就绪；配置了 `readyTimeout` 时按超时约束）。

### `onError(handler)`

- 签名：`onError(handler: (error: Error, to: RouteLocation, from: RouteLocation) => void): () => void`
- 说明：注册路由错误处理回调，导航失败（含守卫中止、API 失败、解析失败）时触发，**返回取消注册函数**。

```ts
const offError = router.onError((error, to, from) => {
	console.error('导航失败:', error.message)
})
offError() // 取消
```

### `onRouteChange(listener)`

- 签名：`onRouteChange(listener: (to: RouteLocation, from: RouteLocation) => void): () => void`
- 说明：注册路由变化监听器，导航完成或状态同步时触发，**返回取消注册函数**。

```ts
router.onRouteChange((to, from) => {
	console.log('路由变化:', from.path, '→', to.path)
})
```

### `syncRoute()`

- 签名：`syncRoute(): void`
- 说明：从页面栈（`getCurrentPages`）同步路由状态到 `currentRoute`。H5 端 `app.use(router)` 已注册 `onShow` mixin 自动同步；**原生端建议在页面 `onShow` 自行调用**。

### `guardRoute(location?, options?)`

- 签名：`guardRoute(location?: RouteLocationRaw, options?: GuardRouteOptions): Promise<RouteLocation>`
- 说明：对指定路由补执行守卫链（冷启动场景，如 H5 直达 / deeplink），**不执行实际导航**。守卫放行时 resolve 目标位置；中止时触发 `options.onAbort(failure)` 并 reject；重定向时按重定向模式**真实导航**（缺省 `relaunch`）。

```ts
router.isReady().then(() => {
	const launchPath = options?.path != null ? `/${options.path}` : undefined
	router.guardRoute(launchPath, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' }) // 页面已加载无法阻止，转跳安全页
		}
	}).catch(() => {})
})
```

### `install(app)`

- 签名：`install(app: any): void`
- 说明：安装路由器到 Vue 应用实例，通常由 `app.use(router)` 调用。**仅 H5** 注册 `provide`（`useRouter` setup 注入）、挂载 `$router` / `$route` 全局属性、注册 `onShow` 全局 mixin（自动 `syncRoute()`）；**原生端**注册全局活跃路由器（供非 setup 上下文回退），并触发插件的 app 级 hook。

## 相关 API

- [createRouter()](./create-router)
- [useRouter()](./use-router)
- [NavigationGuard](./type-navigation-guard)
