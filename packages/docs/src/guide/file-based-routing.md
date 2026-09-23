# 基于文件的路由生成

unix-router 的页面模型是静态的：一条路由 = `pages.json` 里注册的一个页面。传统做法要在 `pages.json` 与路由表里**各写一遍** path / title / tabBar / isTab，手工双份维护极易漂移。

**文件化路由**把事实源下沉到每个页面文件：在页面里就近声明一段配置，构建期插件自动生成 `pages.json`（含 tabBar）与 `routes.gen.uts` 路由表——**页面新增 / 删除 / 改名，双份配置一处声明、自动同步**。

```ts
// vite.config.ts —— 唯一的接入点
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

export default {
	plugins: [uni(), routeGen({ ... })]
}
```

> 这是**构建期 dev 工具**（vite / webpack 插件），不参与运行时，不增加任何包体积；宏在编译期被剥离。

## 三个插件怎么选

`@meng-xi/unix-router/vite-plugin` 导出三个独立插件，对应流水线的不同切面，可单独或组合注册：

| 插件 | 数据流 | 适用场景 |
| --- | --- | --- |
| `routeGen` | 页面文件 → `pages.json` + 路由表 | **默认推荐**。页面就近声明，全链路自动同步 |
| `pagesGen` | 页面文件 → 仅 `pages.json` | 只要页面注册自动化，路由表自己手写 |
| `routesGen` | `pages.json` → 仅路由表 | `pages.json` 是手写的（或由其他工具维护）；name / meta / beforeEnter 经扩展声明文件注入 |

- 三个插件互不依赖：`pagesGen` 不触碰路由文件，`routesGen` 不触碰 `pages.json`
- `pagesGen` + `routesGen` 组合可替代 `routeGen`，两阶段解耦——此时页面级声明的 name / meta / beforeEnter 改走 `routes.ext.uts`（见[扩展声明文件](#扩展声明文件routesextuts)）

用法一致（都是 unplugin 工厂 + vite 适配器），只是选项拆分不同：`pagesGen` 接收顶层公共选项 + `pages`，`routesGen` 接收顶层公共选项 + `router`（见[选项参考](#选项参考)）。

## defineUniPage 宏

在每个页面的 `<script setup>` 顶部声明（无需 import，编译期剥离、等行数注释占位不影响行号）：

```ts
<!-- pages/mine/mine.uvue -->
<script setup>
	// 页面声明宏：编译期由 @meng-xi/unix-router/vite-plugin 剥离
	defineUniPage({
		title: '我的',
		name: 'mine',
		isTab: true,
		tab: { order: 2, text: '我的', iconPath: 'static/tabbar/mine.png', selectedIconPath: 'static/tabbar/mine-active.png' }
	})
	// ...页面逻辑
</script>
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `title` | `string?` | 页面标题 → `pages.json` 的 `navigationBarTitleText` 与路由 `meta.title` |
| `name` | `string?` | 命名路由；缺省按 camelCase 规范化自动生成 |
| `isTab` | `boolean?` | 是否 tabBar 页 → `meta.isTab` + `pages.json` `tabBar.list` |
| `tab` | `{ order?, text?, iconPath?, selectedIconPath? }` | tabBar 附属信息，`order` 决定 tab 顺序 |
| `meta` | `Record<string, any>?` | 路由 meta 扩展字段（如 `requireAuth`），与 `RouteMeta` 定义匹配 |
| `redirect` | `string?` | 路由重定向目标（页面路径） |
| `beforeEnter` | `(to, from) => any?` | 路由独享前置守卫，**须自包含**（生成文件内不引用页面作用域变量） |

**规范**：每页至多一次，位于 script 顶层。`beforeEnter` 等函数字段按原文注入生成文件（JSON 表达不了函数，UTS 表达式可以），类型安全由 UTS 编译链兜底：

```ts
<!-- pages-sub/setting/setting.uvue —— 分包页 + 独享守卫 -->
<script setup>
	defineUniPage({
		title: '设置',
		name: 'setting',
		beforeEnter: (to: any, from: any): any => {
			// 注意：必须自包含，不能引用页面内变量（生成文件与页面文件是两个作用域）
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	})
</script>
```

## &lt;route-config&gt; 自定义块

不习惯宏或声明内容较长时，可用 SFC 自定义块（与宏同效，优先级低于宏）：

```vue
<route-config lang="jsonc">
{
	// jsonc：支持注释
	"title": "商品详情",
	"name": "goods-detail",
	"meta": { "requireAuth": true }
}
</route-config>

<route-config lang="uts">
{
	title: '确认订单',
	meta: { requireAuth: true },
	// lang="uts" 支持函数字段
	beforeEnter: (to, from) => true
}
</route-config>
```

块内容在编译期被虚拟模块拦截，不会进入页面 JS，对编译器透明。

## 优先级

同一字段多处声明时按优先级链取值：

```
defineUniPage 宏  >  <route-config> 块  >  插件推导（titleFallback / tabBar 配置兜底）
```

字段级合并：宏里写了 `title`、块里写了 `meta`，两者都生效。

## name 自动规范化

| 场景 | 结果 |
| --- | --- |
| 宏/块声明了 `name` | 使用声明值 |
| 缺省，末段 `mine` 不冲突 | 末段 camelCase：`pages/mine/mine` → `mine` |
| 末段撞名（如 `pages/a/index` 与 `pages/b/index`） | 全路径 camelCase 回退：`aIndex` / `bIndex` |
| 极端情况仍冲突 | 按 `errorStrategy` 处理：`strict`（默认）抛错终止并列出清单 / `warn` 告警跳过 |

生成 `route-name.gen.d.ts`（WEB 端 `RouteNameMap` 模块增强），按名导航获得字面量补全（见[命名路由类型](./route-config#命名路由类型-routename)）。

## 生成产物

| 文件 | 说明 |
| --- | --- |
| `pages.json` | 自动生成页面条目 + tabBar + subPackages；**手写的非页面字段（globalStyle、uniIdRouter 等）合并保留** |
| `routes.gen.uts` | 路由表 `RouteConfig[]`，在 `router.uts` 中 `import { routes } from './routes.gen.uts'` |
| `route-name.gen.d.ts` | `RouteNameMap` 字面量类型（WEB 端编辑器提示，可关） |
| `define-uni-page.d.ts` | 宏类型声明（编辑器提示，可关） |

生成文件**一并提交 git**：HBuilderX CLI 编译链不保证插件先行执行，watch/HMR 只是开发期辅助。

### routes.gen.uts 示例

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

`routes.gen.uts` 由构建期生成、不宜手改，而 `name` / `meta` 扩展 / `beforeEnter` 在 `pages.json` 里又表达不了。`routesGen` 为此提供扩展声明文件（默认 `routes.ext.uts`），构建期解析后合并进生成的路由表：

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

`routeGen` / `pagesGen` 不读取扩展声明文件——它们的 name / meta / beforeEnter 由页面内宏 / 块就近声明。

## 接入配置

### uni-app x CLI 项目

项目 `vite.config.ts` 已有 `uni()`，直接追加即可：

```ts
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

export default defineConfig({
	plugins: [
		uni(),
		routeGen({
			pages: {
				pagesDir: 'pages',
				subPackages: [{ root: 'pages-sub', dir: 'pages-sub' }],
				entryPage: 'pages/index/index',
				tabBar: { color: '#7A7E83', selectedColor: '#007AFF', backgroundColor: '#FFFFFF' }
			},
			router: { dts: true }
		})
	]
})
```

### HBuilderX 项目

**HBuilderX 下项目根存在 `vite.config.ts` 时会整体替换内置配置**（内置配置里的 `uni()` 不再生效），因此必须自行引入 `uni()` 插件；`@dcloudio/vite-plugin-uni` 不在项目依赖中，从 HBuilderX 内置目录解析：

```ts
// vite.config.ts（HBuilderX）
import { createRequire } from 'node:module'
import { routeGen } from '@meng-xi/unix-router/vite-plugin'

const hbxRequire = createRequire('<HBuilderX安装目录>/plugins/uniapp-cli-vite/package.json')
const uniMod = hbxRequire('@dcloudio/vite-plugin-uni')

export default {
	plugins: [uniMod.default ?? uniMod, routeGen({ ... })]
}
```

### 选项参考

**顶层**：

| 选项 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `pagesJsonPath` | `string?` | `'pages.json'` | pages.json 路径（相对项目根） |
| `watch` | `boolean?` | `true` | 页面目录变更自动重跑（200ms 去抖串行） |
| `verbose` | `boolean?` | `false` | 输出生成日志 |
| `errorStrategy` | `'strict' \| 'warn'?` | `'strict'` | 解析冲突 / name 终极冲突处理策略 |
| `pages` | `object?` | — | 阶段一（页面生成）选项 |
| `router` | `object?` | — | 阶段二（路由生成）选项 |

**`pages`**：

| 选项 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `pagesDir` | `string?` | `'pages'` | 主包页面源码目录 |
| `subPackages` | `{ root, dir }[]?` | `[]` | 分包：`root` 为页面路径前缀，`dir` 为源码目录 |
| `entryPage` | `string?` | — | 启动页，移到 `pages` 数组首位 |
| `titleFallback` | `string?` | — | 宏/块未声明 title 时的兜底标题 |
| `tabBar` | `object?` | — | tabBar 外观字段（color / selectedColor / backgroundColor / borderStyle） |
| `includeExtensions` | `string[]?` | `['.uvue']` | 参与扫描的页面扩展名 |
| `excludePatterns` | `(string \| RegExp)[]?` | `[]` | 排除规则 |
| `dts` | `string \| false?` | — | 宏类型声明输出路径，`false` 关闭 |

**`router`**：

| 选项 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `outputPath` | `string?` | `'routes.gen.uts'` | 路由表文件路径 |
| `exportName` | `string?` | `'routes'` | 路由数组导出变量名 |
| `nameStrategy` | `'camelCase' \| 'fullPath'?` | `'camelCase'` | name 规范化策略（末段 / 全路径） |
| `importFrom` | `string?` | `'@meng-xi/unix-router'` | `RouteConfig` 类型导入来源；uni_modules 用法指向 utssdk 入口 |
| `dts` | `string \| false?` | — | `RouteNameMap` 声明文件路径，`false` 关闭 |
| `preserveRouteChanges` | `boolean?` | `true` | 重生成时保留你对路由文件的修改 |

**`router`（`routesGen` 专属追加）**：

| 选项 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `extensions` | `string \| false?` | `'routes.ext.uts'` | 扩展声明文件路径（相对项目根），`false` 关闭扩展合并 |

> 三个插件的选项拆分：**公共选项**（`pagesJsonPath` / `watch` / `verbose` / `errorStrategy`）三者皆有；`pages` 段归 `routeGen` / `pagesGen`，`router` 段归 `routeGen` / `routesGen`（`extensions` 仅 `routesGen`）。

## 与 @meng-xi/vite-plugin（generateUni）的关系

`defineUniPage` 宏 / `<route-config>` 块语法与 MengXi Studio 的 `generateUni` 保持一致，两插件可平滑互迁。差异点：

- `generateUni` 面向「非 unix-router 路由方案」的项目；本插件为 unix-router 用户提供同风格流水线，且随路由库同包分发（无需额外安装）
- 本插件扩展了 `name` / `meta` 扩展 / `beforeEnter` 等 unix-router 特有字段

## 常见坑

- **HBuilderX 忘记引入 `uni()`**：dev server 启动后页面空白 / `/main` 404。项目根的 `vite.config.ts` 会整体替换内置配置，`uni()` 必须自行引入（见上）。
- **`beforeEnter` 引用页面内变量**：宏函数被原样注入 `routes.gen.uts`，与页面是两个作用域，引用页面变量会编译失败。守卫逻辑须自包含（storage / 全局状态）。
- **宏写了两次**：每页至多一次，`strict` 下直接终止构建。
- **改了页面没生效**：确认插件 `watch` 开启（默认开启）；若通过 HBuilderX 直接改 `pages.json`，下次构建会被重新生成覆盖——页面级声明请回页面文件里改（该坑仅适用于 `routeGen` / `pagesGen`）。
- **`routesGen` 模式下改页面文件没生效**：`routesGen` 以 `pages.json` 为事实源，不扫描页面目录——新增页面先注册进 `pages.json`，路由表随之重生成；name / meta / beforeEnter 走扩展声明文件。

## 下一步

- [路由配置](./route-config) — 手写路由表的字段与规范（本插件的生成目标）
- [路由元信息](./meta) — `meta.requireAuth` 等扩展字段的应用
- [路由守卫](./guards) — `beforeEnter` 在守卫链中的位置
