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
- `pagesGen` + `routesGen` 组合可替代 `routeGen`，两阶段解耦——此时页面级声明的 name / meta / beforeEnter 改走 `routes.ext.uts`（见[扩展路由](./extending-routes)）

用法一致（都是 unplugin 工厂 + vite 适配器），只是选项拆分不同：`pagesGen` 接收顶层公共选项 + `pages`，`routesGen` 接收顶层公共选项 + `router`（见[选项参考](#选项参考)）。

## 设置

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

::: tip 忘记引入 uni()
HBuilderX 下 dev server 启动后页面空白 / `/main` 404，多半是项目根的 `vite.config.ts` 替换了内置配置而未自行引入 `uni()`。
:::

注册后**启动开发服务器**，插件即扫描页面目录并产出 `pages.json` 条目与路由表；页面声明语法见[文件约定](./file-conventions)。

## 迁移现有项目

项目里已有一份手写的 `pages.json` 与路由表？按现状选一条路径，均可渐进迁移。

### 路径 A：routeGen 全自动 —— 双份配置归一（推荐）

现状（path / title 在 pages.json 与路由表各写一遍）：

```ts
// router/routes.ts —— 手写路由表
export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于' } }
]
```

```jsonc
// pages.json —— 手写页面条目（title 与上面 meta.title 重复维护）
{
	"pages": [
		{ "path": "pages/index/index", "style": { "navigationBarTitleText": "首页" } },
		{ "path": "pages/about/about", "style": { "navigationBarTitleText": "关于" } }
	]
}
```

迁移步骤：

1. [设置](#设置)中注册 `routeGen`；
2. 把每个页面的 title / name / isTab / meta 搬进页面宏：

```vue
<!-- pages/index/index.uvue -->
<script setup>
	defineUniPage({ title: '首页', name: 'home', isTab: true })
</script>
```

3. 手写路由表替换为生成产物：

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes.gen.uts' // 构建期自动生成

export const router = createRouter({ routes })
```

4. 删除 `pages.json` 中手写的页面条目（`globalStyle` 等非页面字段**保留**，插件合并生成时不会丢弃），重启 dev server。

- 将 `router/routes.ts` 的手写数组删除，改为 `import { routes } from './routes.gen.uts'`
- 将每页的 `title / name / isTab / meta` 迁入页面顶部的 `defineUniPage`（字段对照见[文件约定](./file-conventions)）

### 路径 B：routesGen 渐进 —— pages.json 保持手写

`pages.json` 由其他工具维护、或暂时不想动页面文件时，只把**路由表**交给插件：

1. [设置](#设置)中把 `routeGen` 换成 `routesGen`；
2. `pages.json` 原样保留，构建期由它生成路由表；
3. `name` / `meta` 扩展 / `beforeEnter` 写进扩展声明文件 `routes.ext.uts`（见[扩展路由](./extending-routes)）。

后续想切换到路径 A 时，`pagesGen` + `routesGen` 的组合可平滑过渡（两阶段解耦）。

## 从头开始

新项目从零接入的最小流程：

**1. 安装**

```bash
npm install @meng-xi/unix-router
```

**2. 注册插件**（见[设置](#设置)，以 CLI 项目为例）

**3. 页面就近声明**

```vue
<!-- pages/index/index.uvue -->
<script setup lang="uts">
	// 启动页：entryPage 已在插件选项中声明为 pages/index/index
	defineUniPage({ title: '首页', name: 'home', isTab: true })
</script>

<template>
	<view class="page">
		<text class="title">首页</text>
	</view>
</template>
```

**4. 创建路由器**：路由表不再手写，直接引用生成产物

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes.gen.uts' // 构建期自动生成

export const router = createRouter({ routes })
```

**5. 安装到应用**

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

**6. 启动 dev server**：插件扫描 `pages` 目录，产出 `pages.json` 页面条目、`routes.gen.uts` 路由表与类型声明（[生成产物](./file-conventions#生成产物)）。产物**一并提交 git**——HBuilderX CLI 编译链不保证插件先行执行。

## 修改路由

需要对生成结果做手工调整时：

- `routeGen` / `pagesGen` 模式：直接改页面里的 `defineUniPage` 声明；对既有条目追加的字段、手工添加的整条自定义路由，靠 `preserveRouteChanges`（默认开启）在重生成时自动保留；
- `routesGen` 模式：通过 `routes.ext.uts` 扩展声明文件注入 name / meta / beforeEnter。

两者详见[扩展路由](./extending-routes)。注意：**手工修改生成文件里"由页面声明推导"的字段没有意义**——下次重生成会被刷新；请改声明源头。

## 选项参考

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

## 下一步

- [文件约定](./file-conventions) — defineUniPage 宏 / &lt;route-config&gt; 块 / name 规范化
- [扩展路由](./extending-routes) — preserveRouteChanges / routes.ext.uts 扩展声明
- [路由配置](./route-config) — 手写路由表的字段与规范（本插件的生成目标）
