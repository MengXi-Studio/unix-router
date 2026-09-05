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
| `path` | `string` | 页面路径，须与 `pages.json` 一致，如 `pages/index/index` |
| `name` | `string?` | 命名路由，用于按名导航（推荐） |
| `meta` | `RouteMeta?` | 路由元信息（见[路由元信息](./meta)） |
| `beforeEnter` | `NavigationGuard \| NavigationGuard[]?` | 路由独享前置守卫（仅本路由生效） |
| `redirect` | `RouteLocationRaw?` | 重定向目标 |

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	// 最简：只有 path
	{ path: 'pages/index/index' },

	// 常用：path + name + meta（isTab 决定使用 switchTab）
	{ path: 'pages/home/home', name: 'home', meta: { title: '首页', isTab: true } },

	// 带路由独享守卫 beforeEnter（仅访问本路由时执行）
	{
		path: 'pages/profile/profile',
		name: 'profile',
		meta: { title: '我的', requireAuth: true },
		beforeEnter: (to, from) => (isLoggedIn() ? true : { name: 'login' })
	},

	// 重定向：访问旧路径直接跳到新路径
	{ path: 'pages/old/home', redirect: 'pages/home/home' }
]
```

## path 与 name：该用哪个导航

- **`name`**：解耦路径与调用方。页面路径改了，只改 `router.config.ts` 一处即可，推荐在业务代码中统一按 `name` 导航。
- **`path`**：适合临时跳转、页面很少的项目；字符串形式可内联 query，如 `router.push('/pages/detail/detail?id=1')`。

## 路径规范化

路径自动补前导斜杠、去尾部斜杠：

- `pages/index/index` → `/pages/index/index`
- `'/pages/about/about/'` → `/pages/about/about`

## 严格模式 strict

- `strict: true`（默认）：按**未注册的 `name`** 导航 → 抛 `ROUTE_NOT_FOUND`，利于尽早发现拼写错误。
- `strict: false`：不抛错，降级为按路径处理（`/` + name），并给出警告。

```ts
// strict: true 时
router.resolve({ name: 'homee' }) // 抛 RouterError(ROUTE_NOT_FOUND)
```

## 重复命名的行为

同名 / 同路径的配置，**后者覆盖前者**并输出警告。建议在配置里通过 `name` 保证唯一，便于排查。

## 关于「命名路由类型提示」

仓库内置 `RouteNameMap` 空接口。想获得 `name` 的类型提示，可通过模块增强扩展它：

```ts
// 仅对 TS / 编辑器提示有效
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: void
		about: void
	}
}
```

> ⚠️ **UTS 限制**：uni-app x 原生端（Kotlin/Swift）**不支持接口声明合并**，上述模块增强在 App 原生编译时不会生效，仅用于 H5/编辑器（TypeScript）补全。原生端请以字符串 `name` 配合 `strict` 模式兜底。

## 常见坑

- **路径写错 / 未注册**：页面白屏。核对 `pages.json`。
- **`name` 拼写错误**：`strict: true` 时抛错；`false` 时静默降级，易被忽视。
- **同名配置**：后者覆盖，若发现跳错页先查是否有重复 `name`。

## 不支持的能力

静态页面模型下不支持（详见[与 vue-router 的差异](./differences)）：

- 动态路由 `addRoute` / `removeRoute`
- 嵌套路由 `children`
- 命名视图 / `RouterView`
- `scrollBehavior`