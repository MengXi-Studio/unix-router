# RouteLocation

已解析并规范化的路由位置，是 `useRoute()` 与 `router.currentRoute` 返回的对象。

```ts
interface RouteLocation {
	path: string            // 页面路径（带前导斜杠，如 /pages/index/index）
	name?: string           // 命名路由名
	meta: RouteMeta         // 路由元信息
	query: Map<string, string> // 查询参数
	params: Map<string, string> // 路由参数
	fullPath: string        // path + 序列化 query
	hash: string            // hash（uni-app x 暂不支持，恒为空串）
	matched: RouteConfig[]  // 匹配到的路由记录
}
```

::: tip query / params 为 Map
uni-app x 中 query 以字符串在 URL 传递，使用 `.get(key)` / `.has(key)` 存取。
:::

## 相关 API

- [useRoute()](./use-route)
- [Router 实例 - currentRoute](./router-instance#currentroute)
- [RouteMeta](./type-route-meta)