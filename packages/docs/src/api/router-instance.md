# Router 实例

`createRouter()` 返回的 `Router` 实例提供导航、守卫与状态查询能力。本文列出全部成员。

## 属性

### `currentRoute`

- 类型：`RouteLocation`
- 说明：当前路由位置。是**模板中触发的响应式对象**，`useRoute()` 基于它派生。

```ts
console.log(router.currentRoute.path) // /pages/index/index
```

## 导航方法

### `push(location)`

- 朝向：`Promise<NavigationResult>`
- 说明：导航到新页面（对应 `uni.navigateTo`，TabBar 页面自动改用 `uni.switchTab`）。

### `replace(location)`

- 朝向：`Promise<NavigationResult>`
- 说明：替换当前页面（对应 `uni.redirectTo`）。

### `relaunch(location)`

- 朝向：`Promise<NavigationResult>`
- 说明：关闭所有页面并打开目标（对应 `uni.reLaunch`）。

### `back(delta?)`

- 朝向：`Promise<NavigationResult>`
- 说明：返回上一页或多级页面（对应 `uni.navigateBack`），`delta` 需为正整数。

```ts
await router.push({ name: 'about' })
await router.back()      // 返回上一页
await router.back(2)     // 返回两级
```

## 守卫注册

| 方法 | 朝向 | 说明 |
| --- | --- | --- |
| `beforeEach(guard)` | `() => void` | 全局前置守卫，返回取消注册函数 |
| `beforeResolve(guard)` | `() => void` | 全局解析守卫 |
| `afterEach(guard)` | `() => void` | 全局后置守卫 |
| `onError(handler)` | `() => void` | 路由错误处理回调 |

```ts
const off = router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !isLoggedIn()) return { name: 'login' }
})
// 需要时移除
off()
```

## 状态与工具方法

### `resolve(location)`

- 朝向：`RouteLocation`
- 说明：解析路由位置为完整 `RouteLocation`，**不执行导航**。

### `hasRoute(name)`

- 朝向：`boolean`
- 说明：检查是否存在指定名称的路由。

### `getRoutes()`

- 朝向：`RouteConfig[]`
- 说明：获取所有已注册路由配置。

### `isReady()`

- 朝向：`Promise<void>`
- 说明：等待路由器初始化完成。

### `syncRoute()`

- 朝向：`void`
- 说明：同步路由状态与实际页面栈（基于 `getCurrentPages()`）。安装时已通过全局 mixin 在页面 `onShow` 自动调用。

### `guardRoute(location?, options?)`

- 朝向：`Promise<RouteLocation>`
- 说明：对指定路由补执行守卫链（冷启动场景），**不执行实际导航**。

```ts
router.isReady().then(() => {
	const launchPath = options?.path ? `/${options.path}` : undefined
	router.guardRoute(launchPath, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' })
		}
	})
})
```

### `install(app)`

- 朝向：`void`
- 说明：安装路由器到 Vue 应用实例（**provide** `router` 与 `route` + 挂载 `$router` / `$route` + 注册全局 mixin）。通常由 `app.use(router)` 调用。

### `onRouteChange(listener)`

- 朝向：`() => void`
- 说明：注册路由变化监听器（完整导航或状态同步时触发），返回取消注册函数。

```ts
router.onRouteChange((to, from) => {
	console.log('路由变化:', from.path, '→', to.path)
})
```

## 相关 API

- [createRouter()](./create-router)
- [useRouter()](./use-router)
- [NavigationGuard](./type-navigation-guard)