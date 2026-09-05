**中文** | [English](./README-en.md)

<div align="center">
	<a href="https://github.com/MengXi-Studio/unix-router">
		<img alt="梦曦工作室 Logo" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/logo.png">
	</a>
	<a href="https://github.com/MengXi-Studio/unix-router">
		<img alt="微信公众号 二维码" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
	</a>
	<br>
	<h1>@meng-xi/unix-router</h1>
	<p>为 uni-app x 提供类似 vue-router 风格的路由管理系统（UTS 编写，双模式兼容）</p>

[![license](https://img.shields.io/github/license/MengXi-Studio/unix-router.svg)](LICENSE) [![npm](https://img.shields.io/npm/v/@meng-xi/unix-router?color=blue)](https://www.npmjs.com/package/@meng-xi/unix-router)
![npm](https://img.shields.io/npm/dt/@meng-xi/unix-router?color=green)

</div>

## 特性

- **vue-router 风格 API** - `createRouter`、`push` / `replace` / `relaunch` / `back`，自动根据 `meta.isTab` 切换 `switchTab`，重复导航自动拒绝（`DUPLICATED`），并发导航自动排队
- **路由守卫** - `beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / `onBeforeRouteLeave` / `onBeforeRouteEnter` / `onBeforeRouteUpdate`，支持可控重定向、守卫超时保护（`guardTimeout`）与重定向深度上限
- **冷启动守卫** - `guardRoute()` 对 H5 直达 / 场景值 / deeplink 等场景补执行守卫链，支持重定向与中止回调（`onAbort`）
- **命名路由 & 路由元信息** - 通过 `name` 导航，`meta` 携带自定义数据（含 `isTab`），严格模式（`strict`）下未匹配命名路由抛出 `RouterError`
- **页面参数传递** - `params`（`Map<string,string>`）经查询编码（`__unixr_p_` 保留前缀）跨页传递，目标页 `route.params` 读回，不暴露明文键名
- **查询参数增强** - `route.query` 为 `Map`，配合 `queryInt()` / `queryNumber()` / `queryBool()` 便捷解析（由库内置工具函数提供）
- **声明式导航** - `useLink()` 组合式 API，返回响应式目标路由、激活态（`isActive` / `isExactActive`）与导航函数，便于自定义链接 / 菜单组件
- **路由状态自动同步** - `app.use(router)` 注入全局 Mixin，页面 `onShow` 自动 `syncRoute()`，`currentRoute` 响应式，非路由器导航（返回键 / TabBar 切换）自动对齐
- **错误处理** - `RouterError` / `NavigationFailure` / `UniNavigationApiError`，`RouterErrorCode` 错误码，`isNavigationFailure()` 精准判断，`onError` 全局捕获
- **组合式 API** - `useRouter()` / `useRoute()` / `useLink()` / `onBeforeRouteLeave()`，`currentRoute` 响应式、`isReady` / `onRouteChange` 状态订阅

## 安装

```bash
pnpm add @meng-xi/unix-router
```

> `packages/core` 为 **UTS 源分发**，web / 小程序 → JS，Android → Kotlin，iOS → Swift，由 uni-app x 编译链现场编译。App 原生推荐以 `uni_modules/<name>/utssdk` 形式分发。

## 快速开始

### 1. 创建路由器

```typescript
// src/router.ts
import { createRouter } from '@meng-xi/unix-router'
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于', requireAuth: true } }
]

export const router = createRouter({ routes, strict: true })

// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // 注入全局 mixin，onShow 时自动 syncRoute()
	return { app }
}
```

### 2. 路由导航

```typescript
const router = useRouter()

// 返回目标路由位置（NavigationResult = RouteLocation）
await router.push({ name: 'about', query: new Map([['a', '1']]) })
await router.push('/pages/detail/detail')
await router.replace({ path: '/pages/detail/detail', params: new Map([['from', '首页']]) })
await router.relaunch('/pages/index/index')
await router.back()      // 返回上一页
await router.back(2)     // 返回两级
```

> 目标路由若在 `meta.isTab` 中声明为 TabBar 页面，`push` / `replace` / `relaunch` 会自动改用 `uni.switchTab`。

### 3. 路由守卫

```typescript
router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !isLoggedIn()) {
		return { name: 'login' } // 重定向
	}
	return true // null / true 表示放行
})

// 组件内离开守卫
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
	if (hasUnsavedChanges) {
		return false // 中止导航
	}
})

// 冷启动守卫（guardRoute）：H5 直达 / 场景值 / deeplink 页面补执行守卫链
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: () => router.relaunch('/pages/index/index')
	})
})
```

### 4. 声明式导航与响应式路由

```typescript
import { useLink, useRoute } from '@meng-xi/unix-router'

// useLink：声明式导航的响应式状态与触发函数
const link = useLink({ to: '/pages/about/about' })
link.isActive.value   // 是否当前激活
await link.navigate() // 触发导航

// useRoute：响应式当前路由，脚本/模板中可直接访问字段
const route = useRoute()
route.path       // /pages/about/about
route.query.get('id') // '1'
```

## 路由选项

`createRouter` 支持以下常用选项：

| 选项          | 类型            | 默认值   | 说明                                                       |
| ------------- | --------------- | -------- | ---------------------------------------------------------- |
| `routes`      | `RouteConfig[]` | -        | 路由配置，需与 `pages.json` 声明一致                       |
| `strict`      | `boolean`       | `true`   | 严格模式，未匹配的命名路由抛出 `RouterError`               |
| `guardTimeout`| `number`        | `10000`  | 守卫超时（ms），超时警告并自动中止导航，设 `0` 关闭        |
| `readyTimeout`| `number`        | `0`      | 就绪超时（ms），防止 `await router.isReady()` 挂起          |

## 文档

📖 **[https://github.com/MengXi-Studio/unix-router/tree/master/packages/docs](https://github.com/MengXi-Studio/unix-router/tree/master/packages/docs)**

## 更新日志

📝 **[https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md)**

## License

[MIT](LICENSE)