# NavigationGuard

路由守卫类型。`beforeEach` / `beforeResolve` / `beforeEnter` / 组件内守卫均使用该签名。

```ts
type NavigationGuard = (
	to: RouteLocation,
	from: RouteLocation
) => NavigationGuardReturn | Promise<NavigationGuardReturn>
```

## NavigationGuardReturn

```
void | boolean | string | RawLocationLike | NavigationRedirect | Error | null
```

| 返回 | 行为 |
| --- | --- |
| `true` / `void` / `null` | 放行 |
| `false` | 中止当前导航（`ABORTED`） |
| `string` / `{ path }` / `{ name }` | 重定向到目标位置 |
| `{ location, mode }` | 重定向，并指定导航方式（`push` / `replace` / `relaunch`） |
| `Error` | 中止导航并抛错 |

## 相关类型

- [PostNavigationGuard](#postnavigationguard)
- [BeforeRouteEnterGuard / UpdateGuard / LeaveGuard](#组件内守卫)

### PostNavigationGuard

`afterEach` 的后置守卫，可接收可选失败信息：

```ts
type PostNavigationGuard = (
	to: RouteLocation,
	from: RouteLocation,
	failure?: Error | null
) => void
```

### 组件内守卫

`onBeforeRouteEnter` / `onBeforeRouteUpdate` / `onBeforeRouteLeave` 对应的守卫类型，签名与 `NavigationGuard` 一致。

## 相关 API

- [路由守卫指南](../guide/guards)
- [Router 实例 - 守卫方法](./router-instance#守卫注册)