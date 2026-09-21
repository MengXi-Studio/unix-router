# NavigationGuard

路由守卫类型。`beforeEach` / `beforeResolve` / `beforeEnter` 均使用该签名。

```ts
type NavigationGuard = (
	to: RouteLocation,
	from: RouteLocation
) => any
```

::: tip 为什么返回类型是 any
守卫的合法返回值是多类型联合（`boolean` / `string` / 位置对象 / `NavigationRedirect` / `Error` / `null`）。在非蒸汽（Kotlin / Swift）编译模式下，联合类型与函数型变存在不匹配（如 `Boolean?` 无法赋给联合类型生成的 `Any`），故签名上放宽为 `any`。实际允许的返回值见 [NavigationGuardReturn](#navigationguardreturn)。
:::

## NavigationGuardReturn

守卫返回值的正式联合类型：

```ts
type NavigationGuardReturn =
	| boolean
	| string
	| RawLocationLike
	| NavigationRedirect
	| Error
	| null
```

UTS 无 `undefined`，`null` 即「无返回 / 放行」。各返回值的行为：

| 返回 | 行为 |
| --- | --- |
| `null` / `true` | 放行 |
| `false` | 中止导航（`ABORTED`） |
| `Error` | 中止导航并作为失败原因（`CANCELLED`） |
| `string` | 重定向到该路径 |
| `RawLocationLike` | 重定向到该位置（沿用原导航模式） |
| `NavigationRedirect` | 重定向到 `location`，并可指定导航方式 `mode` |

组件内守卫（`onBeforeRouteEnter` / `onBeforeRouteUpdate` / `onBeforeRouteLeave`）返回 `NavigationGuardReturn | Promise<NavigationGuardReturn>`。

## NavigationRedirect

导航重定向指令（守卫返回此对象以重定向到其他路由）：

```ts
type NavigationRedirect = {
	/** 重定向目标位置（字符串路径或位置对象） */
	location: RouteLocationRaw
	/** 导航方式：'push' | 'replace' | 'relaunch'，缺省沿用原导航模式 */
	mode?: NavigationRedirectMode
}
```

重定向会重新走完整守卫链，深度上限 10，超出按 `CANCELLED` 取消。

## RawLocationLike

守卫返回的对象位置形式（普通位置对象，区别于携带 `location` 字段的 `NavigationRedirect`）：

```ts
type RawLocationLike = {
	path?: string
	name?: string
	query?: Map<string, string>
	params?: Map<string, string>
}
```

## PostNavigationGuard

`afterEach` 的后置守卫类型，可接收失败信息（UTS 函数类型参数不可选，用显式可空）：

```ts
type PostNavigationGuard = (
	to: RouteLocation,
	from: RouteLocation,
	failure: Error | null
) => void
```

导航成功时 `failure` 为 `null`，中止 / 失败时携带对应 `Error`。

## 组件内守卫类型

三个组件内守卫签名一致，仅使用场景不同：

```ts
type BeforeRouteEnterGuard = (
	to: RouteLocation,
	from: RouteLocation
) => NavigationGuardReturn | Promise<NavigationGuardReturn>

type BeforeRouteUpdateGuard = (
	to: RouteLocation,
	from: RouteLocation
) => NavigationGuardReturn | Promise<NavigationGuardReturn>

type BeforeRouteLeaveGuard = (
	to: RouteLocation,
	from: RouteLocation
) => NavigationGuardReturn | Promise<NavigationGuardReturn>
```

组件内守卫基于全局 `beforeResolve` 过滤实现，注册在页面 `setup` 中。

## 相关 API

- [路由守卫指南](../guide/guards) — 返回值语义与实战
- [RouterErrorCode](./type-router-error-code) — 中止 / 取消错误码
- [Router 实例](./router-instance) — 守卫注册方法
