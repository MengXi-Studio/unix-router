# RouteLocation

已解析并规范化的路由位置，是 `useRoute()` 与 `router.currentRoute` 返回的对象，也是导航成功时 Promise resolve 的值（即 `NavigationResult`）。

```ts
type RouteLocation = {
	path: string                  // 页面路径（带前导斜杠，如 /pages/index/index）
	name: string | null           // 命名路由名（未命名时为 null）
	meta: RouteMeta               // 路由元信息
	query: Map<string, string>    // 查询参数
	params: Map<string, string>   // 路由参数（ParamsPlugin 重建）
	fullPath: string              // path + 序列化 query
	hash: string                  // hash（uni-app x 暂不支持，恒为 ''）
	matched: RouteConfig[]        // 匹配到的路由记录（扁平模型下为单元素数组；未注册路径为 []）
}
```

::: tip query / params 为 Map
uni-app x 中 query 以字符串在 URL 传递，使用 `.get(key)` / `.has(key)` 存取。
:::

## 字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `path` | `string` | 页面路径，规范化后带前导斜杠 |
| `name` | `string \| null` | 命名路由名；无 `name` 的路由为 `null`（非 undefined） |
| `meta` | `RouteMeta` | 来自匹配的 `RouteConfig.meta`；未注册路径为空对象 |
| `query` | `Map<string, string>` | URL 查询参数；`syncRoute` 时已剥离插件内部 key（如 `__params__`） |
| `params` | `Map<string, string>` | 由 `ParamsPlugin` 经关联存储重建的参数；未使用时为空 Map |
| `fullPath` | `string` | `path` + 序列化后的 query |
| `hash` | `string` | 保留字段，恒为 `''` |
| `matched` | `RouteConfig[]` | 扁平模型下匹配结果为单元素数组；路径未注册时为 `[]` |

## 导航结果 NavigationResult

```ts
type NavigationResult = RouteLocation
```

`push` / `replace` / `relaunch` / `back` 均返回 `Promise<NavigationResult>`：成功 resolve 目标 `RouteLocation`，失败 reject [`NavigationFailure`](./type-router-error-code)。

## 输入类型 RouteLocationRaw

导航目标位置（`push` / `replace` / `relaunch` / `resolve` 的入参）：

```ts
type RouteLocationRaw = string | RawLocation
```

**字符串形式**：`'/pages/detail/detail?id=1'`（可内联 query）。

**对象形式 RawLocation**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `path` | `string?` | 目标路径（优先级高于 `name`） |
| `name` | `string?` | 命名路由名 |
| `query` | `Map<string, string>?` | 查询参数 |
| `params` | `Map<string, string>?` | 路径参数（须注册 `ParamsPlugin`，经关联存储传递） |
| `hash` | `string?` | 保留字段（uni-app x 暂不支持，置空） |
| `animationType` | `string?` | 本次导航的过渡动画类型（单次覆盖，`AnimationPlugin` 消费） |
| `animationDuration` | `number?` | 本次导航的过渡动画时长（ms，单次覆盖） |
| `events` | `Map<string, (data: any) => any>?` | 页面间通信监听表（事件名 → 回调，`EventsPlugin` 消费，对齐 `uni.navigateTo` 的 events 语义） |

`path` 与 `name` 都未提供时抛 `ROUTE_NOT_FOUND`。

## 拆分形式

- **RouteLocationNamedRaw**：只允许 `name` 形式的对象（`name` 必填，其余字段同 RawLocation）。
- **RouteLocationPathRaw**：只允许 `path` 形式的对象（`path` 必填，其余字段同 RawLocation）。

两者用于类型收窄场景（如自定义导航封装时强制二选一）。

## 相关 API

- [useRoute()](./use-route)
- [Router 实例 - currentRoute](./router-instance#currentroute)
- [RouteMeta](./type-route-meta)
- [RouterErrorCode](./type-router-error-code)
