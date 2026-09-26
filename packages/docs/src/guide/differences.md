# 与 vue-router 的差异

unix-router 在 API 风格上对齐 vue-router 4，但由于 uni-app x 采用**静态 pages.json 页面模型**，二者存在本质差异。本文说明哪些能力**等价实现**、哪些**不适配**、哪些**行为不同**。

## 能力对照

| vue-router 4 | unix-router（uni-app x） |
| --- | --- |
| `createRouter({ history })` | `createRouter({ routes })`：历史由原生页面栈（`getCurrentPages`）承担，无 HTML5 history |
| 路径 / 命名匹配 | ✅ 支持（无嵌套段与动态段，见下文） |
| `route.query` | ✅ `Map<string, string>`，直接进 URL |
| `route.params` | ✅ `Map<string, string>`，需 ParamsPlugin（`__params__` 内部 key 通道），非路径参数 |
| `route.meta` / `fullPath` | ✅ 支持 |
| `route.hash` | 恒为 `''`（无 URL hash 概念） |
| `currentRoute` 响应式 | ✅ 支持 |
| push / replace / back | ✅ 支持（+ `relaunch`；`meta.isTab` 自动 `switchTab`） |
| `go(n)` | ⚠️ 语义受限：用 `back(delta)` 替代 |
| 守卫 `next()` 回调 | ❌ 改为**返回值风格**：`null`/`true` 放行、`false` 中止、`Error` 取消、位置对象重定向 |
| 重复导航 `resolve(false)` | ❌ 改为 **reject** `NavigationFailure`（`DUPLICATED`，仅 push 检测） |
| 并发导航（后者取消前者） | ❌ 改为**自动排队串行执行** |
| beforeEach / beforeResolve / afterEach / beforeEnter | ✅ 支持 |
| onBeforeRouteLeave / Update / Enter | ✅ 支持，但无 keep-alive → Leave 最可靠（见下文） |
| `useRouter / useRoute / useLink` | ✅ 支持（+ `useOpenerEventChannel`） |
| `isReady / onError` | ✅ 支持（+ `onRouteChange`） |
| 错误体系 NavigationFailure | ✅ 支持（7 类错误码） |
| **嵌套路由** `children` | ❌ 不支持（扁平页面模型） |
| **动态路由** `addRoute / removeRoute` | ❌ 不支持（页面必须编译期注册进 pages.json） |
| **动态路由段** `/user/:id` | ❌ 不支持（路径即页面路径；用 query / params 传参） |
| **命名视图 / RouterView** | ❌ 不支持（无页内渲染占位） |
| **scrollBehavior** | ❌ 不支持（滚动由 uni-app 原生管理） |
| hash 路由模式 | ❌ 不支持（`route.hash` 恒为 `''`） |
| `app.use(router)` 注入 provide / mixin | ⚠️ 仅 **H5 端**注册（`$router`/`$route` provide + onShow 全局 mixin 自动同步）；原生端均不注册，`useRouter` 回退全局活跃路由器 |

## 关键差异说明

### 1. 扁平页面模型：无嵌套、无动态路由段

页面必须先在 `pages.json` 注册，`path` 即真实页面路径（如 `pages/detail/detail`）。因此：

- 无 `children` 嵌套、无命名视图：页面之间是平级栈关系，"嵌套"语义由页面栈天然承担。
- 无 `/user/:id` 动态段：传参用 query（进 URL）或 params（ParamsPlugin 通道）。
- 无 `addRoute / removeRoute`：运行时新增的路径无法被编译进包。分包（`subPackages`）可作资源上的"懒加载"，但不是 vue-router 的嵌套/动态语义。

### 2. 没有 URL / history

uni-app x 无浏览器 URL，"历史"由原生页面栈（`getCurrentPages`）承担：

- 无 `createWebHistory / createWebHashHistory / createMemoryHistory`。
- `route.hash` 恒为 `''`；`fullPath` 由 path + query 组成。
- 无 `scrollBehavior`，滚动行为由原生管理。
- 返回即出栈：`back(delta)` 基于页面栈深度，delta 非正整数返回 `ABORTED`，栈深度不足返回 `CANCELLED`。

### 3. query / params 均为 Map<string, string>

vue-router 的 params 来自路径动态段；unix-router 中 query 与 params 统一为 `Map<string, string>`（字符串值）：

- `query` 直接编码进 URL。
- `params` 经 ParamsPlugin 以内部 key `__params__` 通道跨页传递，**不出现在用户可见 query 中**，目标页在状态同步时取回重建。
- params 需注册 **ParamsPlugin**（`plugins: [new ParamsPlugin()]`）；未注册却使用会 reject `PLUGIN_REQUIRED`。
- 需要跨刷新保留时可开启 `paramsPersistent`（持久化到 uni storage，失败回退内存）。

### 4. 守卫：返回值风格，无 next()

vue-router 守卫通过 `next()` 回调决定走向；unix-router 守卫**直接返回结果**：

- `null` / `true`：放行；`false`：中止（ABORTED）；`Error`：取消（CANCELLED）。
- 返回 `string` / 位置对象：重定向；返回 `{ location, mode? }` 可指定 `push` / `replace` / `relaunch`（缺省沿用原导航模式）。
- 执行链路：beforeEach → beforeEnter（路由独享）→ beforeResolve → 导航 → afterEach(to, from, failure | null)。
- 异步守卫受 `guardTimeout`（默认 10000ms，0 禁用）保护，超时警告并中止导航。

### 5. 重复导航 reject，并发导航排队

- **重复导航**：vue-router 对重复导航 `resolve(false)`；unix-router **仅 push** 检测重复导航并 **reject** `NavigationFailure`（`DUPLICATED`），需用 `isNavigationFailure(err, RouterErrorCode.DUPLICATED)` 捕获，不能依赖"静默成功"。
- **并发导航**：vue-router 中后发起的导航会取消前一个；unix-router 将并发导航**自动排队串行执行**，互不取消。
- **重定向循环防护**：守卫重定向深度上限 10（`MAX_REDIRECT_DEPTH`），超限返回 `CANCELLED`。
- **结果确认**：导航发起后轮询页面栈顶确认是否成功（500ms 内），失败返回 `NAVIGATION_API_ERROR`。

### 6. 组件内守卫的局限

uni-app x 页面每次导航创建新实例，**无 keep-alive**：

- `onBeforeRouteLeave`：最可靠，页面离开（含 back）前触发。
- `onBeforeRouteUpdate`：同组件复用场景几乎不存在，极少触发。
- `onBeforeRouteEnter`：组件实例尚未创建，无法访问组件状态，效果有限。
- 三者均基于 beforeResolve 过滤实现（注册调用本身无返回值、不支持注销，随路由器实例存活）。

### 7. 命名路由的类型提示分端不同

- **WEB 端**：`RouteName` 为 `keyof RouteNameMap & string`，可通过模块增强获得字面量提示：

```ts
// 仅对 TS / 编辑器提示有效
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: 'home'
		about: 'about'
	}
}
```

- **App / 小程序端**：UTS 不支持 `keyof` 组合类型，`RouteName` 退化为 `string`；开启 `strict` 后未注册的命名路由在运行时抛 `ROUTE_NOT_FOUND` 兜底（`strict: false` 则警告并按路径处理）。

### 8. app.use(router) 的端差异

- **H5 端**：`install` 注册 `$router` / `$route` provide 与全局属性，并注册全局 mixin 在 `onShow` 自动 `syncRoute()`。
- **原生端**：以上均不注册，仅将路由器设为全局活跃路由器（供 `useRouter()` 回退）；建议在页面 `onShow` 中自行调用 `router.syncRoute()`。

## 迁移心智

如果你来自 vue-router，多数代码可直接迁移：`createRouter`、守卫（改为返回值风格）、`useRouter/useRoute/useLink`、`push/replace/back` 用法一致；仅需注意将 `components` 配置改为 `pages.json` 注册，放弃动态路由 / 嵌套 / 命名视图 / scrollBehavior，params 改经 ParamsPlugin 传递，重复导航改用 reject 捕获，返回栈深不足会得到 `CANCELLED`。

## 下一步

- [路由配置](./route-config) — 了解 unix-router 的路由表与命名路由
- [常见问题](./faq) — 高频坑排查
- [平台兼容性](./compatibility) — 各端能力与版本要求
