# 扩展路由

`routes.gen.uts` 由构建期生成、不宜手改，但生成结果并不意味着"不可定制"。本篇覆盖两种扩展方式：重生成时**保留手工修改**（`preserveRouteChanges`），以及为 `pages.json` 模式提供**扩展声明文件**（`routes.ext.uts`）。

## routes.gen.uts 示例

```ts
/** 由 @meng-xi/unix-router/vite-plugin 自动生成 */
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{
		path: 'pages-sub/setting/setting',
		name: 'setting',
		meta: { title: '设置' },
		beforeEnter: (to, from) => { /* 宏函数原样注入 */ }
	},
	{ path: 'pages/mine/mine', name: 'mine', meta: { title: '我的', isTab: true } },
	// ...
]
```

### preserveRouteChanges

重新生成时自动 diff 现有 `routes.gen.uts`：你对既有条目追加的字段、手工添加的整条自定义路由（如虚拟占位路由）**全部保留**，仅刷新由页面声明推导的字段。单文件即可安全重生成。

## 扩展声明文件（routes.ext.uts）

`routesGen` 以手写的 `pages.json` 为事实源生成路由表，而 `name` / `meta` 扩展 / `beforeEnter` 在 `pages.json` 里表达不了。`routesGen` 为此提供扩展声明文件（默认 `routes.ext.uts`），构建期解析后合并进生成的路由表：

```ts
// routes.ext.uts —— 须导出一个数组字面量
export const routeExtensions = [
	{
		path: 'pages/goods/detail', // 匹配键：path 或 name 二选一
		meta: { requireAuth: true }, // 追加 meta 扩展字段
		beforeEnter: (to, from) => {
			// 函数原文注入生成文件，须自包含（与宏注入机制同构）
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	},
	{ name: 'home', meta: { keepAlive: true } }
]
```

**合并规则**：

| 场景 | 行为 |
| --- | --- |
| 按 `path`（优先）或 `name` 匹配路由 | 未匹配的条目告警忽略 |
| 声明显式 `name` | 覆盖自动推导名；与其他路由 name 冲突时告警跳过 |
| `meta.title` / `meta.isTab` | 已由 `pages.json`（style / tabBar）推导时告警忽略，仅缺失时补位 |
| `meta` 其余字段 | 同 key 去重后追加（声明值覆盖） |
| `beforeEnter` | 后声明覆盖先前值（覆盖时告警） |
| 声明了 `path` / `name` 之外的字段 | 该条目告警丢弃（可用字段：path / name / meta / beforeEnter） |

`routeGen` / `pagesGen` 不读取扩展声明文件——它们的 name / meta / beforeEnter 由页面内宏 / 块就近声明（见[文件约定](./file-conventions)）。

## 常见坑

- **`routesGen` 模式下改页面文件没生效**：`routesGen` 以 `pages.json` 为事实源，不扫描页面目录——新增页面先注册进 `pages.json`，路由表随之重生成；name / meta / beforeEnter 走扩展声明文件。
- **改了页面没生效（`routeGen` / `pagesGen`）**：确认插件 `watch` 开启（默认开启）；若通过 HBuilderX 直接改 `pages.json`，下次构建会被重新生成覆盖——页面级声明请回页面文件里改。

## 下一步

- [基于文件的路由生成](./file-based-routing) — 插件选择、接入设置与选项参考
- [文件约定](./file-conventions) — defineUniPage 宏 / &lt;route-config&gt; 块语法
- [路由守卫](./guards) — `beforeEnter` 在守卫链中的位置
