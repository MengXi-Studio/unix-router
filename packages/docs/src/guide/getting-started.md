# 快速开始

用一个可运行的 demo 走通 unix-router 的核心闭环：定义路由 → 创建路由器（含插件）→ 安装应用 → 页面导航 + 守卫。

## 环境要求

- uni-app x 项目（HBuilderX 4.0+ 可运行 web/小程序；各端 addInterceptor 的最低 HBuilderX 版本见[平台兼容性](./compatibility)）
- 推荐 HBuilderX 最新稳定版

## 1. 安装

```bash
npm install @meng-xi/unix-router
# 或
pnpm add @meng-xi/unix-router
```

> uni-app x 中也可通过 `uni_modules` 方式引入 UTS 源码（本库以 `.uts` 源分发，按平台现场编译）。

## 2. 定义路由配置

路径须与 `pages.json` 注册一致：

```ts
// router/routes.ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于' } },
	{ path: 'pages/login/login', name: 'login', meta: { title: '登录' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: '个人中心', requireAuth: true } }
]
```

## 3. 创建路由器（含插件）

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true, // 未匹配的命名路由抛 ROUTE_NOT_FOUND
	plugins: [ParamsPlugin, InterceptorPlugin], // 页面参数 + uni 导航拦截
	interceptUniApi: true // InterceptorPlugin 开关：外部 uni.navigateTo 也走守卫
})
```

## 4. 安装到 Vue 应用

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // provide($router/$route) + 全局 onShow 自动 syncRoute
	return { app }
}
```

## 5. 在页面中使用

```vue
<!-- pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>

<template>
	<view class="page">
		<text class="title">unix-router 演示</text>
		<button class="btn" @click="goAbout">push 到关于页</button>
		<RouterLink to="pages/about/about">RouterLink 导航</RouterLink>
	</view>
</template>
```

::: tip 查看当前路由
用 `useRoute()` 响应式读取：`route.path` / `route.query` / `route.params` / `route.meta`。
:::

## 6. 守卫（权限）

```ts
// router/index.ts
router.beforeEach((to, from) => {
	// 未登录访问受保护页 → 重定向登录页并记录来源
	if (to.meta.requireAuth && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 已登录访问登录页 → 去首页
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})
```

## 7. 参数传递（ParamsPlugin）

```ts
// 发起页
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})

// 目标页
const route = useRoute()
console.log(route.params.get('id')) // '1024'
```

## 完整可运行模板

仓库内 `packages/playground` 是可运行的 uni-app x 工程：
- 覆盖导航（push/replace/relaunch/back）、守卫、详情页、参数、RouterLink、tabBar、404
- `pages/test` 是功能自检页（全量 PASS 用于回归）

## 下一步

- [路由导航](./navigation) — 四种导航方式与传参
- [路由守卫](./guards) — 守卫体系详解
- [插件系统](./plugins) — ParamsPlugin / InterceptorPlugin / 自定义插件
- [组合式 API](./composables) — useRouter / useRoute / useLink