# 快速开始

本篇用一个最简可运行示例，带你**从新建工程到完成第一个跨页跳转**。全程可贴代码、可运行、可验证。

> 想跳过搭建、直接看效果？直接用仓库里的 [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground)——它是已配好的 uni-app x 工程，首页即路由入口，`pages/test` 是功能自检页。

## 0. 准备

- [HBuilderX](https://www.dcloud.io/hbuilderx.html)（uni-app x 版）
- 一个 uni-app x 项目（新建时选 **uni-app x** 模板）
- 已安装 `@meng-xi/unix-router`（[安装](./installation)）

## 1. 写路由配置

创建 `router.config.ts`。**`path` 必须与 `pages.json` 里注册的页面路径完全一致**（本项目 `pages/index/index` 等）：

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于' } }
]
```

- `path`：页面路径（无需带前导 `/`），与 `pages.json` 对应
- `name`：命名路由，可按名导航（推荐，解耦路径）
- `meta`：自由携带的元信息（如 `title`、`isTab`），供守卫 / 页面读取

## 2. 创建路由器

创建 `router.ts`，并加一个最简单的守卫做演示：

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './router.config'

export const router = createRouter({ routes, strict: true })

// 可选：打印每次导航
router.afterEach((to, from) => {
	console.log(`[unix-router] ${from.fullPath} -> ${to.fullPath}`)
})
```

`strict: true` 表示：用未注册的 `name` 导航时**直接抛错**（利于尽早发现拼写错误）。见[路由配置](./route-config)。

## 3. 安装到 Vue 应用

在 `main.ts` 中 `app.use(router)`。它会 `provide` 路由器、挂载 `$router/$route`，并注入全局 mixin——页面每次 `onShow` 自动 `syncRoute()`，让 `currentRoute` 始终反映真实页面：

```ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

## 4. 在首页放一个跳转按钮

在 `pages/index/index.uvue` 中读取 `name` 导航：

```vue
<template>
	<view class="page">
		<text class="title">首页</text>
		<button @tap="goAbout">去「关于」页</button>
	</view>
</template>

<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

function goAbout(): void {
	// 按 name 导航，并携带 query（Map 形式）
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## 5. 在「关于」页读取路由

在 `pages/about/about.uvue` 中读取当前路由，体验响应式 `currentRoute`：

```vue
<template>
	<view class="page">
		<text class="title">关于</text>
		<text>来源: {{ from }}</text>
		<text>当前路径: {{ currentPath }}</text>
	</view>
</template>

<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
// 脚本中可直接访问字段（route 是响应式对象）
const currentPath = ref(route.path)
const from = ref(route.query.get('from') ?? '')
</script>
```

> 别忘在 `pages.json` 注册 `pages/about/about`，否则目标页不会被编译进包。

## 6. 运行验证

1. 点击首页按钮 → 应跳转到「关于」页，标题显示"来源: home"。
2. 控制台出现 `[unix-router] /pages/index/index -> /pages/about/about`（你加的 `afterEach` 日志）。
3. 若没跳转 / 白屏，检查：路径是否与 `pages.json` 完全一致、页面是否注册、`onLoad/setup` 是否有报错（参考[常见问题](./faq#路由跳转白屏)）。

## 完成 ✅

你已经完成了第一条链路：**路由表 → 创建路由器 → 安装 → name 导航 → 读取响应式路由**。

## 接下来学什么

- 四种导航（`push / replace / relaunch / back`）与[参数传递](./navigation)
- [组合式 API](./composables)：`useRouter` / `useRoute` / `useLink`
- [路由守卫](./guards)：登录鉴权、`beforeEnter`、冷启动补执行
- 想看完整工程？跳[完整实战](./recipes)

> 注：示例中 `pages/about/about` 仅示意，请按你 `pages.json` 的实际页面调整。