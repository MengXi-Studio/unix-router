# 与 vue-router 的差异

unix-router 在 API 风格上对齐 vue-router 4，但由于 uni-app x 采用**静态 pages.json 页面模型**，二者存在本质差异。本文说明哪些能力**等价实现**、哪些**不适配**。

## 能力对照

| vue-router 4 | unix-router（uni-app x） |
| --- | --- |
| `createRouter({ history })` | `createRouter({ routes })`，无需 `history`（由原生页面栈承担） |
| 路径/命名/参数/查询匹配 | ✅ 支持 |
| `route.params / query / meta / fullPath` | ✅ 支持（params 经查询编码跨页传递） |
| `currentRoute` 响应式 | ✅ 支持 |
| push / replace / back | ✅ 支持（+ `relaunch`） |
| `router.go(n)` | ⚠️ 语义受限：退化用 `back(delta)` |
| beforeEach / beforeResolve / afterEach | ✅ 支持 |
| beforeEnter / onBeforeRouteLeave | ✅ 支持 |
| `useRouter / useRoute / useLink` | ✅ 支持 |
| `RouterLink` | ✅ 提供（适配式组件） |
| 错误体系 NavigationFailure | ✅ 支持 |
| `isReady / onError` | ✅ 支持（+ `onRouteChange`） |
| **动态路由** `addRoute / removeRoute` | ❌ 不支持（编译期忽略未注册页） |
| **嵌套路由** `children` | ❌ 不支持（扁平页面模型） |
| **命名视图 / RouterView** | ❌ 不支持（无页内渲染占位） |
| **scrollBehavior** | ❌ 不支持（滚动由 uni-app 原生管理） |
| hash 路由模式 | ❌ 不支持（无 URL 概念） |

## 关键差异说明

### 1. 没有 URL / history

uni-app x 无浏览器 URL；"历史"由原生页面栈（`getCurrentPages`）承担。因此：
- 无 `createWebHistory` / `createWebHashHistory` / `createMemoryHistory`。
- 无 hash 参数；滚动行为由原生管理。

### 2. 静态路由表

页面必须先在 `pages.json` 注册，运行时 `addRoute` 无法把页面编译进包 → 不支持动态路由。
分包（`subPackages`）可作资源上的"懒加载"，但不是 vue-router 的嵌套/动态语义。

### 3. params 的实现

原生不支持路径参数（路径即页面路径）。unix-router 将 `params` 经**查询编码**
（`__unixr_p_` 保留前缀）在页面 URL 间传递，目标页 `route.params` 可读取。因此：
- params 形如 `Map<string,string>`（字符串值）。
- params 会以编码形式出现在 URL 查询中（不暴露明文键名）。

### 4. `go(n)`

vue-router 的 `go` 基于 history 相对跳转，uni-app x 无该栈 API → 用 `back(delta)` 代替。

### 5. 组件内守卫

uni-app x 页面每次导航创建新实例（无 keep-alive），故 `onBeforeRouteUpdate` 极少触发、
`onBeforeRouteEnter` 效果有限，`onBeforeRouteLeave` 最常用。

## 迁移心智

如果你来自 vue-router，多数代码可直接迁移：`createRouter`、守卫、`useRouter/useRoute/useLink`、`push/replace/back` 用法一致；仅需注意将 `components` 配置改为 `pages.json` 注册，并放弃动态路由 / 嵌套 / 命名视图。