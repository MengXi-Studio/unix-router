# 路由配置

unix-router 基于 uni-app x 的**静态页面模型**：一条路由 = `pages.json` 里注册的一个页面，路由路径即页面路径。

## 路由与页面的一一对应

```
pages.json                            router.config.ts
─────────────────────                 ──────────────────
pages/                                routes:
  index/index.uvue   ◀── 对应 ───▶   { path: 'pages/index/index', name: 'home', ... }
  about/about.uvue   ◀── 对应 ───▶   { path: 'pages/about/about', name: 'about', ... }
```

**关键约束**：`RouteConfig.path` 必须与 `pages.json` 注册的页面路径**完全一致**，否则目标页不会被编译进包、跳转会白屏。这也是为何 unix-router **不支持动态路由**（新增路由无法在运行时把页面编译进包）。

## RouteConfig 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `path` | `string` | **必填**。页面路径，与 `pages.json` 注册一致，**不带前导斜杠**，如 `pages/index/index` |
| `name` | `string?` | 命名路由，用于按名导航（推荐） |
| `redirect` | `RouteLocationRaw?` | 重定向目标（0.7.0 仅声明类型，导航流程暂未消费，预留字段） |
| `meta` | `RouteMeta?` | 路由元信息（见[路由元信息](./meta)） |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | 路由独享前置守卫（仅进入本路由时执行） |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	// 最简：只有 path
	{ path: 'pages/index/index' },

	// 常用：path + name + meta（isTab 决定使用 switchTab）
	{ path: 'pages/home/home', name: 'home', meta: { title: '首页', isTab: true } },

	// 路由独享守卫：函数形式，仅访问本路由时执行
	{
		path: 'pages/profile/profile',
		name: 'profile',
		meta: { title: '我的', requireAuth: true },
		beforeEnter: (to, from) => (isLoggedIn() ? true : { name: 'login' })
	},

	// 路由独享守卫：数组形式，依次执行，任一拦截即终止
	{
		path: 'pages/admin/admin',
		name: 'admin',
		beforeEnter: [checkLogin, checkAdmin]
	}
]
```

## path 与 name：该用哪个导航

- **`name`**：解耦路径与调用方。页面路径改了，只改 `router.config.ts` 一处即可，推荐在业务代码中统一按 `name` 导航。
- **`path`**：适合临时跳转、页面很少的项目；字符串形式可内联 query，如 `router.push('/pages/detail/detail?id=1')`。

## 路径规范化

配置里的 `path` 不带前导斜杠（与 `pages.json` 一致），路由器内部统一规范化为带前导斜杠、去尾部斜杠的标准形式：

- `pages/index/index` → `/pages/index/index`
- `/pages/about/about/` → `/pages/about/about`

导航输入（`push` 的字符串或对象）带不带前导斜杠均可，解析结果一致。

## 严格模式 strict

- `strict: true`（默认）：按**未注册的 `name`** 导航 → 抛 `ROUTE_NOT_FOUND` 的 `RouterError`，利于尽早发现拼写错误。
- `strict: false`：不抛错，输出警告，并降级为按路径处理（`/` + name）。

```ts
// strict: true 时
router.resolve({ name: 'homee' }) // 抛 RouterError(ROUTE_NOT_FOUND)
```

## 重复 name / path 检测

同名或同路径的配置，**输出警告且后者覆盖前者**：

```
检测到重复路由名称 "home"，后者将覆盖前者。
检测到重复路由路径 "/pages/index/index"，后者将覆盖前者。
```

建议保证 `name` 唯一；发现"跳错页"时先排查是否存在重复 `name` / `path`。

## 命名路由类型 RouteName

`RouteName` 是命名路由名称的导出类型：

- **WEB 端**：`keyof RouteNameMap & string`。`RouteNameMap` 是内置空接口，可通过模块增强扩展，为路由名提供字面量提示：

```ts
// 仅对 TS / 编辑器提示有效
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: 'home'
		about: 'about'
	}
}
```

- **原生端**：UTS 不支持 `keyof` 组合类型，`RouteName` 退化为 `string`（配合 `strict` 模式兜底拼写错误）。

典型用法是在业务封装中标注参数类型：

```ts
import type { RouteName } from '@meng-xi/unix-router'

function go(where: RouteName) {
	router.push({ name: where })
}
// H5 端 where 获得字面量补全；原生端等同 string
```

## 常见坑

- **路径写错 / 未注册**：页面白屏。核对 `pages.json`。
- **`name` 拼写错误**：`strict: true` 时抛错；`false` 时静默降级为路径导航，易被忽视。
- **同名配置**：后者覆盖，若发现跳错页先查是否有重复 `name`。

## 不支持的能力

静态页面模型下不支持（详见[与 vue-router 的差异](./differences)）：

- 动态路由 `addRoute` / `removeRoute`
- 嵌套路由 `children`
- 命名视图 / `RouterView`
- `scrollBehavior`

## 下一步

- [路由导航](./navigation) — 四种导航方式与传参入口
- [路由元信息](./meta) — meta 内置字段与自定义
- [RouteConfig API](../api/type-route-config) — 类型完整定义
