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

> uni-app x 中也可通过 `uni_modules` 方式引入 UTS 源码（本库以 `.uts` 源分发，按平台现场编译），详见[安装](./installation)。

## 2. 定义路由配置

路径须与 `pages.json` 注册一致（不带前导 `/`）：

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

插件是 `RouterPlugin` 抽象类的子类，注册时**必须实例化**：

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true, // 未匹配的命名路由抛 ROUTE_NOT_FOUND
	plugins: [new ParamsPlugin(), new InterceptorPlugin()], // 页面参数 + uni 导航拦截
	interceptUniApi: true // InterceptorPlugin 开关：外部 uni.navigateTo 也走守卫
})
```

::: warning 旧写法已废弃
`plugins: [ParamsPlugin]`（直接传 class）已不再支持——插件为 abstract class，必须 `new` 出实例后注册。
:::

## 4. 安装到 Vue 应用

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // H5 端注册全局 mixin（onShow 自动 syncRoute）；App/小程序端建议在页面 onShow 自行调用 router.syncRoute()
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
	router.push({ name: 'about', query: new Map<string, string>([['from', 'home']]) })
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

守卫为**返回值风格**：返回 `true`（或 `null`）放行；返回位置对象表示重定向，`{ location, mode }` 可指定重定向的导航方式：

```ts
// router/index.ts
router.beforeEach((to, from) => {
	// 未登录访问受保护页 → 重定向登录页并记录来源
	if (to.meta.requireAuth == true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 已登录访问登录页 → 去首页
	if (to.name == 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})
```

## 7. 参数传递（ParamsPlugin）

params 经 `__params__` 内部 key 通道跨页传递（不出现在用户可见的 URL query 中），值为 `Map<string, string>`：

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

::: tip
`params` 需注册 `ParamsPlugin`（上方第 3 步已注册）；未注册却使用会 reject `PLUGIN_REQUIRED`。详见[插件系统](./plugins)与[参数传递](./params)。
:::

## 完整可运行模板

仓库内 `packages/playground` 是可运行的 uni-app x 工程：
- 覆盖导航（push/replace/relaunch/back）、守卫、详情页、参数、RouterLink、tabBar、404
- `pages/test` 是功能自检页（全量 PASS 用于回归）

### 体验版安卓 App

不想自己搭工程？直接安装体验版安卓 App（示例项目打包，含 tabBar、导航守卫、参数传递、RouterLink 导航动画等全部示例）：

[⬇️ 下载体验版安卓 App（.apk）](https://mp-b8b8347a-48e9-434d-8302-3e9d99c2cb01.cdn.bspapp.com/cloudstorage/app-build-pkg/1789882378601-__UNI__B6A50A8_1789882365595.apk)

> 说明：体验版主要用于快速预览功能，正式集成仍建议参考上方步骤在你的 uni-app x 项目中安装使用。

## 下一步

- [路由配置](./route-config) — RouteConfig / RouteMeta / 命名路由
- [路由导航](./navigation) — 四种导航方式与传参
- [参数传递](./params) — params 跨页传递与 query 解析工具
- [组合式 API](./composables) — useRouter / useRoute / useLink
